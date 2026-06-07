package lol.catgirl.module.player.noslow.impl;

import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.module.player.noslow.NoSlowMode;
import lol.catgirl.utils.player.PacketUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

public final class NCPNoSlowMode implements NoSlowMode {
    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (!PlayerUtils.isHoldingWeapon()) return;
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;

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

    @Override
    public void onUsingItem(PlayerUseMultiplierEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;
        event.setCancelled(true);
    }
}
