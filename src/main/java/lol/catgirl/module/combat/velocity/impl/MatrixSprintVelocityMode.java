package lol.catgirl.module.combat.velocity.impl;

import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.combat.velocity.VelocityMode;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

public final class MatrixSprintVelocityMode implements VelocityMode {
    @Override
    public void onPacketRecieved(PacketReceivedEvent event) {
        if (event.packet instanceof ClientboundSetEntityMotionPacket packet) {
            if (mc.player != null && mc.player.hurtTime > 0 && !mc.player.onGround()) {
                double yawRad = mc.player.getYRot() * 0.017453292F;

                Vec3 velocity = mc.player.getDeltaMovement();
                double horizontalSpeed = Math.sqrt(
                        velocity.x * velocity.x +
                                velocity.z * velocity.z
                );

                mc.player.setDeltaMovement(
                        -Math.sin(yawRad) * horizontalSpeed,
                        velocity.y,
                        Math.cos(yawRad) * horizontalSpeed
                );

                mc.player.setSprinting(mc.player.tickCount % 2 != 0);
            }
        }
    }
}
