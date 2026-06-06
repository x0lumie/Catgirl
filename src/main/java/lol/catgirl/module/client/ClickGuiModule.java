package lol.catgirl.module.client;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.ui.click.dropdown.CatgirlDropdown;
import lol.catgirl.ui.click.imgui.ClickGui;
import org.lwjgl.glfw.GLFW;

public final class ClickGuiModule extends Module {
    public static final ClickGuiModule INSTANCE = new ClickGuiModule();

    private boolean didInitImgui = false;

    public enum Mode {
        Menu,
        Dropdown
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Menu);

    public ClickGuiModule() {
        super("ClickGUI",
                "Renders the interface to customize and toggle modules.",
                ModuleCategory.Movement
        );
        setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
        addSetting(mode);
    }

    @Override
    public void onEnable() {
        if (mode.getValue() == Mode.Menu) {
            if (mc.getWindow() != null && !didInitImgui) {
                didInitImgui = true;
            }
            mc.setScreen(new ClickGui());
        } else {
            mc.setScreen(new CatgirlDropdown());
        }

        this.toggle();
        super.onEnable();
    }
}
