package lol.catgirl.module.player.nofall.impl;

import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.module.player.nofall.NoFallMode;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public final class OnGroundNoFallMode implements NoFallMode {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacket() instanceof ServerboundMovePlayerPacket
                packet && mc.player.fallDistance >= 2.5) {
            packet.onGround = true;
            mc.player.fallDistance = 0;
        }
    }
}
