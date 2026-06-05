package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PacketUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class NoSlowModule extends Module {
    public static final NoSlowModule INSTANCE = new NoSlowModule();

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Motion);
    public final SliderProperty speed = new SliderProperty("Motion Speed", 0.42f, 0.1f, 5.0f, 0.01f).hide(() -> mode.getValue() != Mode.Motion);
    public final BoolProperty waitFirstTick = new BoolProperty("Wait First Tick", false).hide(() -> mode.getValue() != Mode.Polar);

    public final BoolProperty matrixFood = new BoolProperty("Matrix Food", true).hide(()-> !(mode.getValue() == Mode.Matrix));
    public final BoolProperty matrixPotion = new BoolProperty("Matrix Potion", true).hide(()-> !(mode.getValue() == Mode.Matrix));
    public final BoolProperty matrixSword = new BoolProperty("Matrix Sword", true).hide(()-> !(mode.getValue() == Mode.Matrix));
    public final BoolProperty matrixBow = new BoolProperty("Matrix Bow", true).hide(()-> !(mode.getValue() == Mode.Matrix));
    public final SliderProperty matrixStrafeSpeed = new SliderProperty("Matrix Strafe Speed", 0.0265f, 0.01f, 0.1f, 0.0005f).hide(()-> !(mode.getValue() == Mode.Matrix));

    public NoSlowModule() {
        super("NoSlow", "Removes the slow-down when using certain items.", ModuleCategory.Movement);
        addSettings(mode,
                waitFirstTick,
                speed,
                matrixFood,
                matrixPotion,
                matrixSword,
                matrixBow,
                matrixStrafeSpeed
        );
    }

    public enum Mode {
        Motion,
        NCP,
        Jump,
        Intave,
        Polar,
        Matrix
    }

    private boolean shouldApplyMatrixForItem(Item item) {
        ItemStack stack = new ItemStack(item);
        if (stack.has(DataComponents.FOOD) && matrixFood.getValue()) return true;
        if (item instanceof PotionItem && matrixPotion.getValue()) return true;
        if (PlayerUtils.isHoldingWeapon() && matrixSword.getValue()) return true;
        if (item instanceof BowItem && matrixBow.getValue()) return true;
        return false;
    }

    private boolean isUsingRelevantItem() {
        if (mc.player == null) return false;
        ItemStack heldItem = mc.player.getMainHandItem();
        if (heldItem.isEmpty()) return false;
        return shouldApplyMatrixForItem(heldItem.getItem());
    }

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        if (!this.isEnabled()) return;
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;

        if (mode.getValue() == Mode.Jump && mc.options.keyUse.isDown() && PlayerUtils.isHoldingWeapon()) {
            PacketUtils.sendPacket(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                    BlockPos.ZERO,
                    Direction.DOWN
            ));
            PacketUtils.sendPacket(new ServerboundUseItemPacket(
                    InteractionHand.MAIN_HAND, 0, mc.player.getXRot(), mc.player.getYRot()
            ));
        }

        if (mode.getValue() == Mode.NCP) {
            if (!PlayerUtils.isHoldingWeapon()) return;
            if (mc.options.keyUse.isDown()) {
                PacketUtils.sendPacket(new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                        BlockPos.ZERO,
                        Direction.DOWN
                ));
                PacketUtils.sendPacket(new ServerboundUseItemPacket(
                        InteractionHand.MAIN_HAND, 0, mc.player.getXRot(), mc.player.getYRot()
                ));
            }
        }
    }

    @EventHook
    public void onPacketSend(PacketSendEvent event) {
        if (!this.isEnabled()) return;
        if (mode.getValue() != Mode.Intave) return;
        if (mc.player == null) return;
        if (!PlayerUtils.isHoldingWeapon()) return;
        if (!(event.getPacket() instanceof ServerboundUseItemOnPacket packet)) return;
        if (!mc.player.isUsingItem() || !MoveUtils.isMoving()) return;

        BlockHitResult original = packet.getHitResult();
        BlockHitResult replaced = new BlockHitResult(
                Vec3.atCenterOf(original.getBlockPos()),
                original.getDirection(),
                original.getBlockPos(),
                original.isInside()
        );

        PacketUtils.sendPacket(new ServerboundUseItemOnPacket(
                packet.getHand(), replaced, packet.getSequence()
        ));
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (!this.isEnabled() || mc.player == null || mc.level == null) return;

        if (mode.getValue() == Mode.Jump && mc.player.onGround()
                && MoveUtils.isMoving() && mc.player.isUsingItem()) {
            PlayerUtils.jump();
        }
    }

    @EventHook
    public void onUsingItem(PlayerUseMultiplierEvent event) {
        if (!this.isEnabled()) return;
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;

        switch (mode.getValue()) {
            case NCP -> event.setCancelled(true);

            case Jump -> {
                if (!mc.player.onGround() && MoveUtils.isMoving()) {
                    if (ScaffoldModule.INSTANCE.isEnabled()) return;
                    event.setCancelled(true);
                }
            }

            case Intave -> {
                if (mc.player.isUsingItem() && PlayerUtils.isHoldingWeapon()) {
                    event.setCancelled(true);
                }
            }

            case Polar -> {
                event.setCancelled(true);
                if (mc.player.isUsingItem()) {
                    if (mc.player.getUseItemRemainingTicks() <= 1 && waitFirstTick.getValue()) return;

                    int slot = mc.player.getInventory().getSelectedSlot();
                    PacketUtils.sendPacket(new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                            BlockPos.ZERO,
                            Direction.DOWN
                    ));
                    PacketUtils.sendPacket(new ServerboundSetCarriedItemPacket((slot + 1) % 9));
                    PacketUtils.sendPacket(new ServerboundSetCarriedItemPacket(slot));
                    PacketUtils.sendPacket(new ServerboundUseItemPacket(
                            InteractionHand.MAIN_HAND, 0, mc.player.getYRot(), mc.player.getXRot()
                    ));
                }
            }

            case Matrix -> {
                if (mc.player.isUsingItem() && isUsingRelevantItem()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHook
    public void onBruhTick(ClientTickEvent event) {
        if (!this.isEnabled()) return;
        if (mode.getValue() != Mode.Matrix) return;
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.isUsingItem() || !isUsingRelevantItem()) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;

        int ticksSinceUse = mc.player.getUseItemRemainingTicks();
        float itemUseTime = mc.player.getTicksUsingItem();

        if (itemUseTime > 1) {
            float strafeValue = matrixStrafeSpeed.getValue().floatValue();
            MoveUtils.setMotion(strafeValue);
        } else {
            boolean speedEnabled = lol.catgirl.module.movement.SpeedModule.INSTANCE.isEnabled();

            if (!speedEnabled) {
                mc.player.setDeltaMovement(
                        mc.player.getDeltaMovement().x * 0.992,
                        mc.player.getDeltaMovement().y,
                        mc.player.getDeltaMovement().z * 0.992
                );
            } else {
                mc.player.setDeltaMovement(
                        mc.player.getDeltaMovement().x * 0.99,
                        mc.player.getDeltaMovement().y,
                        mc.player.getDeltaMovement().z * 0.99
                );
            }
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}