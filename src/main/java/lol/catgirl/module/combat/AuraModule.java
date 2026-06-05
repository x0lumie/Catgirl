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
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;

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

    public static SliderProperty killRange = new SliderProperty("Kill Range", 3, 3, 6, 0.1f);
    public final EnumProperty<Rotations> rotations = new EnumProperty<>("Rotations", Rotations.Regular);
    public static SliderProperty rotationSpeed = new SliderProperty("Rotation Speed", 2, 1, 5, 0.1f);
    public static BoolProperty rayCast = new BoolProperty("Ray Cast", true);
    public static BoolProperty oldCombat = new BoolProperty("Old Combat", false);
    public static SliderProperty minCps = new SliderProperty("Min CPS", 9, 1, 20, 1)
            .hide(() -> !oldCombat.getValue());
    public static SliderProperty maxCps = new SliderProperty("Max CPS", 13, 1, 20, 1)
            .hide(() -> !oldCombat.getValue());
    public final EnumProperty<AutoBlock> autoBlock = new EnumProperty<>("Auto Block", AutoBlock.None).hide(() -> !oldCombat.getValue());

    public static final AuraModule INSTANCE = new AuraModule();

    private long lastAttackTime = 0L;
    private long nextAttackDelay = 0L;
    private boolean canAttack = true;
    public int hitTicks;
    // boi that's so tuff
    // fakeBlocking isnt needed small bub
    private boolean realBlocking;
    int blockTicks = 0;

    public AuraModule() {
        super("Aura", "Automatically kills enemies in a specified vicinity.", ModuleCategory.Combat);
        addSettings(killRange, rotations, rotationSpeed, rayCast, oldCombat, minCps, maxCps, autoBlock);
    }

    @Override
    public void onEnable() {
        // grrr I hate the world
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
    };

    public static LivingEntity target;

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null) return;

        if (maxCps.getValue() < minCps.getValue()) {
            maxCps.setValue(minCps.getValue());
        }

        if (minCps.getValue() > maxCps.getValue()) {
            minCps.setValue(maxCps.getValue());
        }

        target = TargetsModule.getTarget();

        if (target == null) return;

        if (mc.player.distanceTo(target) > killRange.getValue() && realBlocking) {
            unblock();
        }

        attack();
        autoblock();
    }

    @EventHook
    public void onPlayerRotation(PlayerRotationEvent event) {
        if (mc.player == null || target == null) return;

        float[] finalRotations = new float[0];

        float[] currentRotations = new float[]{event.yaw, event.pitch};

        switch (rotations.getValue()) {

            case Regular ->
                    finalRotations = RotationUtils.regularAuraRotations(currentRotations, target, rotationSpeed.getValue());
            case Polar -> finalRotations = RotationUtils.polarAuraRotations(currentRotations, target);
            case Puhfy ->
                    finalRotations = RotationUtils.puhfyAuraRotations(currentRotations, target, rotationSpeed.getValue());
        }

        event.yaw = finalRotations[0];
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
                mc.player.connection.send(
                        new ServerboundUseItemPacket(
                                InteractionHand.MAIN_HAND,
                                0,
                                mc.player.getYRot(),
                                mc.player.getXRot()
                        )
                );
                realBlocking = true;
            }
            case Fake -> {
                ItemAnimationUtil.setBlocking(target != null);
            }
            case Polar -> {
                int slot = mc.player.getInventory().getSelectedSlot();

                mc.player.connection.send(
                        new ServerboundPlayerActionPacket(
                                ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                                mc.player.blockPosition(),
                                mc.player.getDirection()
                        )
                );

                realBlocking = false;

                mc.player.connection.send(new ServerboundSetCarriedItemPacket((slot + 1) % 9));
                mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));

                mc.player.connection.send(
                        new ServerboundUseItemPacket(
                                InteractionHand.MAIN_HAND,
                                0,
                                mc.player.getYRot(),
                                mc.player.getXRot()
                        )
                );
                realBlocking = true;
            }
            case Legit -> {
                mc.options.keyUse.setDown(mc.player.distanceTo(target) < 3 && hitTicks <= 5 && mc.player.hurtTime >= 5);
                realBlocking = mc.player.distanceTo(target) < 3 && hitTicks <= 5 && mc.player.hurtTime >= 5;
                blockTicks++;
                if (mc.options.keyUse.isDown() || mc.player.isUsingItem()) {
                    blockTicks = 0;
                }
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
            mc.player.connection.send(
                    new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                            mc.player.blockPosition(),
                            mc.player.getDirection()
                    )
            );
        }
        realBlocking = false;
    }

    private long calculateCpsDelay(double min, double max) {
        if (min >= max) {
            return (long) (1000.0 / min);
        }

        double randomCps = ThreadLocalRandom.current().nextDouble(min, max);

        return (long) (1000.0 / randomCps);
    }
}