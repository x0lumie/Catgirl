package lol.catgirl.module.render.nametags;

import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.event.impl.RenderWorldEvent;
import lol.catgirl.utils.IMinecraft;

public interface NametagsMode extends IMinecraft {
    default void onRenderTick(RenderTickEvent event) {}
    default void onRenderWorld(RenderWorldEvent event) {}
}
