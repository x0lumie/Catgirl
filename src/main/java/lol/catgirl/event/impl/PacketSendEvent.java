package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;

@Getter
public class PacketSendEvent extends Event {
    private final Packet<?> packet;

    public PacketSendEvent(Packet<?> packet) {
        this.packet = packet;
    }

}
