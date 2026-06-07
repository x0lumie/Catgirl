package lol.catgirl.module.player.nofall;

import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PlayerRotationEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.utils.IMinecraft;

public interface NoFallMode extends IMinecraft {
    default void onRotation(PlayerRotationEvent event) {}
    default void onPreMotion(PreMotionEvent event) {}
    default void onPacketSend(PacketSendEvent event) {}
}
