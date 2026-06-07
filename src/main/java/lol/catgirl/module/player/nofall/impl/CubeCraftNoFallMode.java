package lol.catgirl.module.player.nofall.impl;

import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.module.player.nofall.NoFallMode;
import lol.catgirl.utils.player.MoveUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public final class CubeCraftNoFallMode implements NoFallMode {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof ServerboundMovePlayerPacket && mc.player.fallDistance >= 2.5) {
            mc.player.setPosRaw(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            MoveUtils.setMotionY(mc.player.getDeltaMovement().y + 0.1);
            mc.player.fallDistance = 0;
        }
    }
}
