package lol.catgirl.module.movement.noweb;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.utils.IMinecraft;

public interface NoWebMode extends IMinecraft {
    default void onTick(PreUpdateEvent event) {}
}
