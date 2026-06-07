package lol.catgirl.module.player.noslow;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.utils.IMinecraft;

public interface NoSlowMode extends IMinecraft {
    default void onPreMotion(PreMotionEvent event) {}
    default void onPacketSend(PacketSendEvent event) {}
    default void onTick(ClientTickEvent event) {}
    default void onUsingItem(PlayerUseMultiplierEvent event) {}
    default void onBruhTick(ClientTickEvent event) {}
}
