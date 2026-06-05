package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PlayerRotationEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.TargetsModule;
import lol.catgirl.setting.impl.BoolProperty;
import lol.catgirl.setting.impl.EnumProperty;
import lol.catgirl.setting.impl.SliderProperty;
import lol.catgirl.utils.client.ItemAnimationUtil;
import lol.catgirl.utils.player.PlayerUtils;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class AuraModule extends Module {

    public enum Rotations {
        Regular,
        Polar,
        Puhfy
    }

    public enum AutoBlock {
        None,
        Fake,
        Vanilla,
        Polar,
        Legit
    }

    public enum TargetPriority {
        Distance,
        Health,
        Angle,
        HurtTime
    }

    public static SliderProperty killRange = new SliderProperty("Kill Range", 3, 3, 6, 0.1f);
    public final EnumProperty<Rotations> rotations = new EnumProperty<>("Rotations", Rotations.Regular);

    public static SliderProperty minRotationSpeed = new SliderProperty("Min Rot Speed", 1.5f, 0.5f, 5, 0.1f);
    public static SliderProperty maxRotationSpeed = new SliderProperty("Max Rot Speed", 2.5f, 0.5f, 5, 0.1f);

    public static BoolProperty rayCast = new BoolProperty("Ray Cast", true);
    public static BoolProperty oldCombat = new BoolProperty("Old Combat", false);
    public static SliderProperty minCps = new SliderProperty("Min CPS", 9, 1, 20, 1)
            .hide(() -> !oldCombat.getValue());
    public static SliderProperty maxCps = new SliderProperty("Max CPS", 13, 1, 20, 1)
            .hide(() -> !oldCombat.getValue());
    public final EnumProperty<AutoBlock> autoBlock = new EnumProperty<>("Auto Block", AutoBlock.None)
            .hide(() -> !oldCombat.getValue());
    public final EnumProperty<TargetPriority> targetPriority = new EnumProperty<>("Target Priority", TargetPriority.Distance);

    public static final AuraModule INSTANCE = new AuraModule();

    private long lastAttackTime = 0L;
    private long nextAttackDelay = 0L;
    private boolean canAttack = true;
    public int hitTicks;
    private boolean realBlocking;
    int blockTicks = 0;

    public AuraModule() {
        super("Aura", "Automatically kills enemies in a specified vicinity.", ModuleCategory.Combat);
        addSettings(killRange, rotations, minRotationSpeed, maxRotationSpeed, rayCast,
                oldCombat, minCps, maxCps, autoBlock, targetPriority);
    }

    @Override
    public void onEnable() {
        nextAttackDelay = 0L;
        lastAttackTime = 0L;
        canAttack = true;
        blockTicks = -1;
        hitTicks = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        unblock();
        ItemAnimationUtil.setBlocking(false);
        super.onDisable();
    }

    public static LivingEntity target;

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null) return;

        if (maxCps.getValue() < minCps.getValue()) maxCps.setValue(minCps.getValue());
        if (minCps.getValue() > maxCps.getValue()) minCps.setValue(maxCps.getValue());

        if (maxRotationSpeed.getValue() < minRotationSpeed.getValue())
            maxRotationSpeed.setValue(minRotationSpeed.getValue());
        if (minRotationSpeed.getValue() > maxRotationSpeed.getValue())
            minRotationSpeed.setValue(maxRotationSpeed.getValue());

        target = getBestTarget();

        if (target == null) return;

        if (mc.player.distanceTo(target) > killRange.getValue() && realBlocking) {
            unblock();
        }

        attack();
        autoblock();
    }

    private LivingEntity getBestTarget() {
        List<LivingEntity> candidates = TargetsModule.getTargetList();
        if (candidates == null || candidates.isEmpty()) return null;

        Comparator<LivingEntity> comparator = switch (targetPriority.getValue()) {
            case Distance -> Comparator.comparingDouble(e -> mc.player.distanceTo(e));
            case Health   -> Comparator.comparingDouble(LivingEntity::getHealth);
            case Angle    -> Comparator.comparingDouble(e -> getAngleTo(e));
            case HurtTime -> Comparator.comparingInt(e -> -e.hurtTime);
        };

        return candidates.stream()
                .filter(e -> mc.player.distanceTo(e) <= killRange.getValue())
                .min(comparator)
                .orElse(null);
    }

    private double getAngleTo(LivingEntity entity) {
        if (mc.player == null) return Double.MAX_VALUE;
        double dx = entity.getX() - mc.player.getX();
        double dz = entity.getZ() - mc.player.getZ();
        double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double delta = Math.abs(mc.player.getYRot() - targetYaw) % 360.0;
        if (delta > 180.0) delta = 360.0 - delta;
        return delta;
    }

    @EventHook
    public void onPlayerRotation(PlayerRotationEvent event) {
        if (mc.player == null || target == null) return;

        float[] finalRotations;
        float[] currentRotations = new float[]{event.yaw, event.pitch};

        float speed = randomRotationSpeed();

        finalRotations = switch (rotations.getValue()) {
            case Regular -> RotationUtils.regularAuraRotations(currentRotations, target, speed);
            case Polar   -> RotationUtils.polarAuraRotations(currentRotations, target);
            case Puhfy   -> RotationUtils.puhfyAuraRotations(currentRotations, target, speed);
        };

        event.yaw   = finalRotations[0];
        event.pitch = finalRotations[1];

        if (rayCast.getValue()) {
            HitResult hitResult = PlayerUtils.raycast(event.yaw, event.pitch, killRange.getValue(), false);
            canAttack = hitResult != null && hitResult.getType() == HitResult.Type.ENTITY;
        }
    }

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        hitTicks++;
    }

    private float randomRotationSpeed() {
        float min = minRotationSpeed.getValue();
        float max = maxRotationSpeed.getValue();
        if (min >= max) return min;
        return (float) ThreadLocalRandom.current().nextDouble(min, max);
    }

    private void attack() {
        if (mc.player == null || mc.gameMode == null || target == null || !canAttack) return;
        if (mc.player.distanceTo(target) > killRange.getValue()) return;

        if (oldCombat.getValue()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAttackTime >= nextAttackDelay) {
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
                hitTicks = 0;
                lastAttackTime = currentTime;
                nextAttackDelay = calculateCpsDelay(minCps.getValue(), maxCps.getValue());
            }
        } else {
            if (mc.player.getAttackStrengthScale(0.5f) < 1.0f) return;
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
            hitTicks = 0;
        }
    }

    private void autoblock() {
        if (mc.player == null || target == null || !oldCombat.getValue()) return;
        if (mc.player.distanceTo(target) > killRange.getValue()) return;

        switch (autoBlock.getValue()) {
            case Vanilla -> {
                ItemAnimationUtil.setBlocking(true);
                mc.player.connection.send(new ServerboundUseItemPacket(
                        InteractionHand.MAIN_HAND, 0,
                        mc.player.getYRot(), mc.player.getXRot()));
                realBlocking = true;
            }
            case Fake -> ItemAnimationUtil.setBlocking(true);
            case Polar -> {
                ItemAnimationUtil.setBlocking(true);
                int slot = mc.player.getInventory().getSelectedSlot();
                mc.player.connection.send(new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                        mc.player.blockPosition(), Direction.DOWN));
                realBlocking = false;
                mc.player.connection.send(new ServerboundSetCarriedItemPacket((slot + 1) % 9));
                mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
                mc.player.connection.send(new ServerboundUseItemPacket(
                        InteractionHand.MAIN_HAND, 0,
                        mc.player.getYRot(), mc.player.getXRot()));
                realBlocking = true;
            }
            case Legit -> {
                boolean shouldBlock = mc.player.distanceTo(target) < 3
                        && hitTicks <= 5
                        && mc.player.hurtTime >= 5;
                mc.options.keyUse.setDown(shouldBlock);
                realBlocking = shouldBlock;
                blockTicks++;
                if (mc.options.keyUse.isDown() || mc.player.isUsingItem()) blockTicks = 0;
                canAttack = blockTicks >= 2;
            }
        }
    }

    private void unblock() {
        if (mc.player == null || !realBlocking) return;
        if (autoBlock.getValue() == AutoBlock.Legit && mc.options.keyUse.isDown()) {
            mc.options.keyUse.setDown(false);
        }
        if (autoBlock.getValue() != AutoBlock.Legit) {
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                    mc.player.blockPosition(), Direction.DOWN));
        }
        realBlocking = false;
    }

    private long calculateCpsDelay(double min, double max) {
        if (min >= max) return (long) (1000.0 / min);
        double randomCps = ThreadLocalRandom.current().nextDouble(min, max);
        return (long) (1000.0 / randomCps);
    }
}