package lol.catgirl.module.combat.velocity.impl;

import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.combat.velocity.VelocityMode;
import lol.catgirl.utils.player.MoveUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public final class MatrixVelocityMode implements VelocityMode {
    @Override
    public void onPacketRecieved(PacketReceivedEvent event) {
        if (event.packet instanceof ClientboundSetEntityMotionPacket packet) {
            double motionX = packet.movement.x;
            double motionZ = packet.movement.z;
            motionZ *= 0.06;
            motionX *= 0.06;
            MoveUtils.setMotionX(motionX);
            MoveUtils.setMotionZ(motionZ);
        }
    }
}
