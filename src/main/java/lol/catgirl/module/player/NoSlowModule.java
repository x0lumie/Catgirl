package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.setting.impl.BoolProperty;
import lol.catgirl.setting.impl.EnumProperty;
import lol.catgirl.setting.impl.SliderProperty;
import lol.catgirl.utils.client.GameTimer;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PacketUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    public final EnumProperty<Mode> mode = new EnumProperty<Mode>("Mode", Mode.Motion);
    public final SliderProperty speed = new SliderProperty("Motion Speed", 0.42f, 0.1f, 5.0f, 0.01f).hide(() -> !(mode.getValue() == Mode.Motion));
    public final BoolProperty waitFirstTick = new BoolProperty("Wait First Tick", false).hide(()-> !(mode.getValue() == Mode.Polar));

    public NoSlowModule() {
        super("NoSlow",
                "Removes the slow-down when using certain items.",
                ModuleCategory.Movement
        );
        addSettings(
                mode,
                waitFirstTick,
                speed
        );
    }

    private int tick = 0;

    public enum Mode {
        Motion,
        NCP,
        Jump,
        Intave,
        Polar
    }

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        if (!this.isEnabled()) return;

        if (mc.player == null || mc.level == null) {
            tick = 0;
            return;
        }

        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) {
            tick = 0;
            return;
        }

        if (mode.getValue() == Mode.Polar) {

            event.setCancelled(true);

            if (mc.player.isUsingItem()) {

                if (mc.player.getUseItemRemainingTicks() <= 1 && waitFirstTick.getValue()) {
                    return;
                }

                int slot = mc.player.getInventory().getSelectedSlot();

                PacketUtils.sendPacket(new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                        BlockPos.ZERO,
                        Direction.DOWN
                ));

                PacketUtils.sendPacket(new ServerboundSetCarriedItemPacket((slot + 1) % 9));
                PacketUtils.sendPacket(new ServerboundSetCarriedItemPacket(slot));

                PacketUtils.sendPacket(new ServerboundUseItemPacket(
                        InteractionHand.MAIN_HAND,
                        0,
                        mc.player.getYRot(),
                        mc.player.getXRot()
                ));
            } else {
                tick = 0;
            }
        }

        if (mode.getValue() == Mode.Jump && mc.options.keyUse.isDown()
                && PlayerUtils.isHoldingWeapon()
        ) {
            PacketUtils.sendPacket(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                    BlockPos.ZERO,
                    Direction.DOWN
            ));

            PacketUtils.sendPacket(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND,
                    0, mc.player.getXRot(), mc.player.getYRot()));
        }

//        if (mode.getValue() == Mode.Intave && mc.player.isUsingItem()) {
//            int ticks = mc.player.getTicksUsingItem();
//            int remaining = mc.player.getUseItemRemainingTicks();
//            if (ticks <= 2 || remaining == 0) {
//                PacketUtil.sendPacket(new ServerboundPlayerActionPacket(
//                        ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
//                        mc.player.blockPosition(),
//                        Direction.UP
//                ));
//            }
//        }
//
        if (mode.getValue() == Mode.NCP) {
            if (!PlayerUtils.isHoldingWeapon()) return;

            if (mc.options.keyUse.isDown()) {
                PacketUtils.sendPacket(new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                        BlockPos.ZERO,
                        Direction.DOWN
                ));

                PacketUtils.sendPacket(new ServerboundUseItemPacket(
                        InteractionHand.MAIN_HAND,
                        0, mc.player.getXRot(),
                        mc.player.getYRot())
                );
            }
        }
    }

    @EventHook
    public void onPacketSend(PacketSendEvent event) {
        if (!this.isEnabled()) return;
        if (mode.getValue() != Mode.Intave) return;
        if (mc.player == null) return;
        if(!PlayerUtils.isHoldingWeapon()) return;

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
                packet.getHand(),
                replaced,
                packet.getSequence()
        ));
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (!this.isEnabled() || mc.player == null || mc.level == null) {
            return;
        }

        if (mc.player.onGround() && mode.getValue() == Mode.Jump
                && MoveUtils.isMoving() && mc.player.isUsingItem()
        ) {
            PlayerUtils.jump();
        }
    }

    @EventHook
    public void onUsingItem(PlayerUseMultiplierEvent event) {
        if (!this.isEnabled()) return;

        if (mode.getValue() == Mode.NCP) {
            event.setCancelled(true);
        }


        if (mode.getValue() == Mode.Jump) {
            if (!mc.player.onGround() && MoveUtils.isMoving()) {
                if (ScaffoldModule.INSTANCE.isEnabled()) return;
                event.setCancelled(true);
            }
        }

        if (mode.getValue() == Mode.Intave) {
            if (mc.player.isUsingItem() && PlayerUtils.isHoldingWeapon()) {
                event.setCancelled(true);
            }
            return;
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}