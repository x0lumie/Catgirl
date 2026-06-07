package lol.catgirl.module.movement.speed;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.utils.IMinecraft;

public interface SpeedMode extends IMinecraft {
    default void onEnable() {}
    default void onDisable() {}
    default void onTick(ClientTickEvent event) {}
}
