package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.protocol.Packet;

public class PacketReceivedEvent extends Event {
    public final Packet<?> packet;
    @Setter @Getter
    private boolean cancelled;

    public PacketReceivedEvent(Packet<?> packet) {
        this.packet = packet;
        this.cancelled = false;
    }
}
