package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import lol.catgirl.event.EventHook;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.screens.Screen;

@AllArgsConstructor
public class OpenScreenEvent extends Event {
    public Screen screen;
}
