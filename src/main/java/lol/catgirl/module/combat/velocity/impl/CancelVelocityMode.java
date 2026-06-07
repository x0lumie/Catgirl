package lol.catgirl.module.combat.velocity.impl;

import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.combat.VelocityModule;
import lol.catgirl.module.combat.velocity.VelocityMode;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

public final class CancelVelocityMode implements VelocityMode {
    @Override
    public void onPacketRecieved(PacketReceivedEvent event) {
        if (event.packet instanceof ClientboundSetEntityMotionPacket packet) {
            event.setCancelled(true);
        }
    }
}
