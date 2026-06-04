package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import net.minecraft.client.gui.GuiGraphics;

public class RenderTickEvent extends Event {

    public float partialTicks;
    public GuiGraphics context;

    public RenderTickEvent(float partialTicks, GuiGraphics context) {
        this.partialTicks = partialTicks;
        this.context = context;
    }

}