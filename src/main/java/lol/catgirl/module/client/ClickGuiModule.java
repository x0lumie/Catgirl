package lol.catgirl.module.client;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.ui.click.imgui.ClickGui;
import org.lwjgl.glfw.GLFW;

public final class ClickGuiModule extends Module {
    public static final ClickGuiModule INSTANCE = new ClickGuiModule();

    private boolean didInitImgui = false;

    public ClickGuiModule() {
        super("ClickGUI", "Renders the interface to customize and toggle modules.", ModuleCategory.Movement);
        setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        if (mc.getWindow() != null && !didInitImgui) {
            didInitImgui = true;
        }
        mc.setScreen(new ClickGui());
        this.toggle();
        super.onEnable();
    }

}
