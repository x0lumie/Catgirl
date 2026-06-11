package lol.catgirl.module.combat;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PlayerRotationEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.event.impl.WorldJoinEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.NotificationsModule;
import lol.catgirl.module.client.TargetsModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.ui.notification.Notification;
import lol.catgirl.ui.notification.NotificationManager;
import lol.catgirl.utils.client.ItemAnimationUtils;
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

    public enum Rotations {Normal, Polar, Legit, Perfect}

    public enum AutoBlock {None, Fake, Vanilla, Polar, Legit}

    public enum TargetPriority {Distance, Health, Angle, HurtTime}

    public static final SliderProperty killRange = new SliderProperty("Kill Range", 3, 3, 6, 0.1f);
    public final EnumProperty<Rotations> rotations = new EnumProperty<>("Rotations", Rotations.Normal);
    public final EnumProperty<TargetPriority> targetPriority = new EnumProperty<>("Target Priority", TargetPriority.Distance);
    public static final SliderProperty minRotationSpeed = new SliderProperty("Min Rot Speed", 30, 1, 180, 1f);
    public static final SliderProperty maxRotationSpeed = new SliderProperty("Max Rot Speed", 30, 1, 180, 1f);
    public static final BoolProperty rayCast = new BoolProperty("Ray Cast", true);
    public static final BoolProperty useMouseClick = new BoolProperty("Use Mouse Click", true);
    public static final BoolProperty rotateOnAttack = new BoolProperty("Rotate On Attack", false);
    public static final BoolProperty oldCombat = new BoolProperty("Old Combat", false);
    public static final SliderProperty minCps = new SliderProperty("Min CPS", 9, 1, 20, 1)
            .hide(() -> !oldCombat.getValue());
    public static final SliderProperty maxCps = new SliderProperty("Max CPS", 13, 1, 20, 1)
            .hide(() -> !oldCombat.getValue());
    public final EnumProperty<AutoBlock> autoBlock = new EnumProperty<>("Auto Block", AutoBlock.None)
            .hide(() -> !oldCombat.getValue());
    public static final BoolProperty smartAttacking = new BoolProperty("Smart Attacking", true);
    public static final SliderProperty failRate = new SliderProperty("Miss Chance (%)", 0, 0, 40, 1);
    public static final BoolProperty autoDisable = new BoolProperty("Auto Disable", true);

    public static final AuraModule INSTANCE = new AuraModule();

    public static LivingEntity target;
    public static boolean canAttack = true;
    public int hitTicks;

    private long lastAttackTime = 0L;
    private long nextAttackDelay = 0L;
    private boolean realBlocking;
    private int blockTicks;
    private boolean attackedThisTick;

    public AuraModule() {
        super("Aura", "Automatically kills enemies in a specified vicinity.", ModuleCategory.Combat);
        addSettings(
                killRange, rotations, targetPriority,
                minRotationSpeed, maxRotationSpeed,
                rayCast, useMouseClick, rotateOnAttack,
                oldCombat, minCps, maxCps, autoBlock,
                smartAttacking,
                failRate, autoDisable
        );
    }

    @Override
    public void onEnable() {
        nextAttackDelay = 0L;
        lastAttackTime = 0L;
        canAttack = true;
        blockTicks = -1;
        hitTicks = 0;
        attackedThisTick = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        unblock();
        ItemAnimationUtils.setBlocking(false);
        super.onDisable();
    }

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        clampSliderPair(minCps, maxCps);
        clampSliderPair(minRotationSpeed, maxRotationSpeed);

        attackedThisTick = false;
        target = getBestTarget();

        if (target == null) {
            if (realBlocking) {
                unblock();
            }
            if (ItemAnimationUtils.isBlocking()) {
                ItemAnimationUtils.setBlocking(false);
            }
            return;
        }

        if (mc.player.distanceTo(target) > killRange.getValue() && realBlocking) {
            unblock();
        }

        attack();
        autoblock();
    }

    @EventHook
    public void onPlayerRotation(PlayerRotationEvent event) {
        if (mc.player == null || target == null) return;
        if (rotateOnAttack.getValue() && !attackedThisTick) return;

        float[] current = {event.yaw, event.pitch};
        float speed = randomRotationSpeed();
        RotationUtils.setRotationSpeed(speed);

        float[] rotated = switch (rotations.getValue()) {
            case Polar -> RotationUtils.polarAuraRotations(current, target, speed);
            case Normal -> RotationUtils.puhfyAuraRotations(current, target, speed);
            case Perfect -> RotationUtils.perfectAuraRotations(current, target, speed);
            case Legit -> RotationUtils.legitAuraRotations(current, target, speed);
        };

        event.yaw = rotated[0];
        event.pitch = rotated[1];

        if (rayCast.getValue()) {
            HitResult hit = PlayerUtils.raycast(event.yaw, event.pitch, killRange.getValue(), false);
            canAttack = hit != null && hit.getType() == HitResult.Type.ENTITY;
        }
    }

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        hitTicks++;
    }

    private LivingEntity getBestTarget() {
        List<LivingEntity> candidates = TargetsModule.getTargetList();
        if (candidates == null || candidates.isEmpty()) return null;

        double range = killRange.getValue();

        Comparator<LivingEntity> comparator = switch (targetPriority.getValue()) {
            case Distance -> Comparator.comparingDouble(mc.player::distanceTo);
            case Health -> Comparator.comparingDouble(LivingEntity::getHealth);
            case Angle -> Comparator.comparingDouble(this::getAngleTo);
            case HurtTime -> Comparator.comparingInt(e -> -e.hurtTime);
        };

        return candidates.stream()
                .filter(e -> mc.player.distanceTo(e) <= range)
                .min(comparator)
                .orElse(null);
    }

    private double getAngleTo(LivingEntity entity) {
        double dx = entity.getX() - mc.player.getX();
        double dz = entity.getZ() - mc.player.getZ();
        double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double delta = Math.abs(mc.player.getYRot() - targetYaw) % 360.0;
        return delta > 180.0 ? 360.0 - delta : delta;
    }

    private void attack() {
        if (!isReadyToAttack()) return;
        if (shouldMiss()) return;

        if (oldCombat.getValue()) {
            long now = System.currentTimeMillis();
            if (now - lastAttackTime >= nextAttackDelay) {
                handleAttack();
                hitTicks = 0;
                lastAttackTime = now;
                nextAttackDelay = calculateCpsDelay(minCps.getValue(), maxCps.getValue());
            }
        } else {
            if (mc.player.getAttackStrengthScale(0.5f) < 1.0f) return;
            handleAttack();
            hitTicks = 0;
        }
    }

    private boolean isReadyToAttack() {
        return mc.player != null
                && mc.gameMode != null
                && target != null
                && canAttack
                && mc.player.distanceTo(target) <= killRange.getValue()
                && (!smartAttacking.getValue() || PlayerUtils.canCrit());
    }

    private boolean shouldMiss() {
        float chance = failRate.getValue();
        return chance > 0 && ThreadLocalRandom.current().nextFloat() * 100f < chance;
    }

    private void handleAttack() {
        attackedThisTick = true;
        if (useMouseClick.getValue()) {
            mc.startAttack();
        } else {
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private void autoblock() {
        if (mc.player == null || target == null || !oldCombat.getValue()) return;
        if (mc.player.distanceTo(target) > killRange.getValue()) return;

        switch (autoBlock.getValue()) {
            case Vanilla -> {
                ItemAnimationUtils.setBlocking(true);
                mc.player.connection.send(new ServerboundUseItemPacket(
                        InteractionHand.MAIN_HAND, 0,
                        mc.player.getYRot(), mc.player.getXRot()));
                realBlocking = true;
            }
            case Fake -> ItemAnimationUtils.setBlocking(true);
            case Polar -> {
                if (mc.level == null) return;

                ItemAnimationUtils.setBlocking(true);
                int slot = mc.player.getInventory().getSelectedSlot();
                int swap = (slot + 1) % 9;

                mc.player.connection.send(new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                        mc.player.blockPosition(), Direction.DOWN));
                mc.player.connection.send(new ServerboundSetCarriedItemPacket(swap));
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

        if (autoBlock.getValue() == AutoBlock.Polar) {
            realBlocking = false;
            return;
        }

        if (autoBlock.getValue() == AutoBlock.Legit && mc.options.keyUse.isDown()) {
            mc.options.keyUse.setDown(false);
        }

        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                mc.player.blockPosition(), Direction.DOWN));

        realBlocking = false;
    }

    private float randomRotationSpeed() {
        float min = minRotationSpeed.getValue();
        float max = maxRotationSpeed.getValue();
        return min >= max ? min : (float) ThreadLocalRandom.current().nextDouble(min, max);
    }

    private long calculateCpsDelay(double min, double max) {
        if (min >= max) return (long) (1000.0 / min);
        long base = (long) (1000.0 / ThreadLocalRandom.current().nextDouble(min, max));
        return base;
    }

    private static void clampSliderPair(SliderProperty lo, SliderProperty hi) {
        if (hi.getValue() < lo.getValue()) hi.setValue(lo.getValue());
        if (lo.getValue() > hi.getValue()) lo.setValue(hi.getValue());
    }

    @Override
    protected String getFinalSuffix() {
        return rotations.getValue().toString();
    }

    @EventHook
    public void onWorldChange(WorldJoinEvent event) {
        if (!this.isEnabled()) return;

        if (autoDisable.getValue()) {

            switch (NotificationsModule.INSTANCE.mode.getValue()) {
                case Chat -> {
                    Catgirl.sendChatMessage(this.getDisplayName() + " has been disabled due to world change.");
                }
                case Exhibition -> {
                    NotificationManager.post(this.getDisplayName(), "Disabled on world change.", Notification.Type.NOTIFY);
                }
                case None -> {
                }
            }

            toggle();
        }
    }
}