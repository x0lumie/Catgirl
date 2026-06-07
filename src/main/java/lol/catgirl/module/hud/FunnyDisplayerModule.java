package lol.catgirl.module.hud;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class FunnyDisplayerModule extends Module {

    public enum Funny {
        Catgirl, Catgirl2, Catgrill, Lumie, Zesty, Alan, Miguel, BigRat, Nep, Gamer, Cat
    }

    public final EnumProperty<Funny> funny = new EnumProperty<>("Funny", Funny.Catgirl);
    public static SliderProperty size = new SliderProperty("Size", 100, 100, 1000, 50);

    public static final FunnyDisplayerModule INSTANCE = new FunnyDisplayerModule();

    private float dragX, dragY;
    private boolean dragging;

    private float x = -1, y = -1;

    public FunnyDisplayerModule() {
        super("FunnyDisplayer", "Shows the funnies.", ModuleCategory.Hud);
        addSettings(funny, size);
    }

    @EventHook
    public void onRender(Render2DEvent event) {
        if (x == -1 && y == -1 && mc.getWindow() != null) {
            x = mc.getWindow().getGuiScaledWidth() - size.getValue().intValue();
            y = mc.getWindow().getGuiScaledHeight() - size.getValue().intValue();
        }

        handleDragging();

        Identifier imageLocation = getTexturePath(funny.getValue());

        if (imageLocation == null) return;

        event.context.blit(
                RenderPipelines.GUI_TEXTURED, imageLocation,
                (int) x, (int) y,
                0, 0,
                size.getValue().intValue(), size.getValue().intValue(),
                size.getValue().intValue(), size.getValue().intValue(),
                size.getValue().intValue(), size.getValue().intValue()
        );
    }

    private Identifier getTexturePath(Funny mode) {
        return switch (mode) {
            case Catgirl -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/catgirl.png");
            case Catgirl2 -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/catgirl2.png");
            case Catgrill -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/catgrill.png");
            case Lumie -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/lumie.png");
            case Zesty -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/zesty.png");
            case Alan -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/alan.png");
            case Miguel -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/miguel.png");
            case BigRat -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/bigrat.png");
            case Nep -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/nep.png");
            case Gamer -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/gamer.png");
            case Cat -> Identifier.fromNamespaceAndPath("catgirl", "images/funny/cat.png");
        };
    }

    private void handleDragging() {
        if (mc.screen instanceof ChatScreen) {

            double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();

            boolean isMouseDown = GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;

            if (isMouseDown) {
                if (!dragging) {
                    if (mouseX >= x && mouseX <= x + size.getValue().intValue() && mouseY >= y && mouseY <= y + size.getValue().intValue()) {
                        dragging = true;
                        dragX = (float) (mouseX - x);
                        dragY = (float) (mouseY - y);
                    }
                } else {
                    x = (float) (mouseX - dragX);
                    y = (float) (mouseY - dragY);
                }
            } else {
                dragging = false;
            }
        } else {
            dragging = false;
        }
    }
}