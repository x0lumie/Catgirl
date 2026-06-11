package lol.catgirl.module.client;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.ui.click.dropdown.CatgirlDropdown;
import lol.catgirl.ui.click.imgui.ClickGui;
import lol.catgirl.ui.click.menu.MenuClickGui;
import org.lwjgl.glfw.GLFW;

public final class ClickGuiModule extends Module {
    public static final ClickGuiModule INSTANCE = new ClickGuiModule();

    private boolean didInitImgui = false;

    public enum Mode {
        ImGui,
        Dropdown,
        Menu
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.ImGui);

    public ClickGuiModule() {
        super("ClickGUI",
                "Renders the interface to customize and toggle modules.",
                ModuleCategory.Client
        );
        setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
        addSetting(mode);
    }

    @Override
    public void onEnable() {
        if (mode.getValue() == Mode.ImGui) {
            if (mc.getWindow() != null && !didInitImgui) {
                didInitImgui = true;
            }
            mc.setScreen(new ClickGui());
        } else if (mode.getValue() == Mode.Dropdown) {
            mc.setScreen(new CatgirlDropdown());
        } else {
            mc.setScreen(new MenuClickGui());
        }

        this.toggle();
        super.onEnable();
    }
}
