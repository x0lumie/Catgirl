package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class Render2DEvent extends Event {

    public GuiGraphics context;
    public DeltaTracker delta;
    public int scaledWidth, scaledHeight;
    public double mouseX, mouseY;
    public float ticks;

    public Render2DEvent(GuiGraphics context, DeltaTracker delta,
                         int scaledWidth, int scaledHeight,
                         double mouseX, double mouseY,
                         float ticks

    ) {
        this.context = context;
        this.delta = delta;
        this.scaledHeight = scaledHeight;
        this.scaledWidth = scaledWidth;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.ticks = ticks;
    }
}
