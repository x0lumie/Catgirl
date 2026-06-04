package lol.catgirl.module.client;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.EnumSetting;

public class InterfaceModule extends Module {
    public static final InterfaceModule INSTANCE = new InterfaceModule();

    public InterfaceModule() {
        super("Interface", "Settings on how the client should look.", ModuleCategory.Client);
        addSetting(fontMode);
    }

    public final EnumSetting<FontMode> fontMode = new EnumSetting<>("Font Mode", FontMode.ProductSans);

    public enum FontMode {
        ProductSans,
        Minecraft,
        Comfortaa,
        ComfortaaBold,
        SFProDisplayBold
    }
}