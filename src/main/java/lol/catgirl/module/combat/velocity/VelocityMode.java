package lol.catgirl.module.combat.velocity;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.utils.IMinecraft;

public interface VelocityMode extends IMinecraft {
    default void onPacketRecieved(PacketReceivedEvent event) {}
    default void onTick(ClientTickEvent event) {}
}
