package lol.catgirl.module.player.noslow.impl;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.module.player.ScaffoldModule;
import lol.catgirl.module.player.noslow.NoSlowMode;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PacketUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

public final class JumpNoSlowMode implements NoSlowMode {
    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;

        if (mc.options.keyUse.isDown() && PlayerUtils.isHoldingWeapon()) {
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

    @Override
    public void onTick(ClientTickEvent event) {

        if (mc.player.onGround() && MoveUtils.isMoving() && mc.player.isUsingItem()) {
            PlayerUtils.jump();
        }
    }

    @Override
    public void onUsingItem(PlayerUseMultiplierEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;

        if (!mc.player.onGround() && MoveUtils.isMoving()) {
            if (ScaffoldModule.INSTANCE.isEnabled()) return;
            event.setCancelled(true);
        }
    }
}
