package lol.catgirl.module.client;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.EnumSetting;
import lol.catgirl.setting.impl.SliderSetting;

public class InterfaceModule extends Module {
    public static final InterfaceModule INSTANCE = new InterfaceModule();

    public enum FontMode {
        ProductSans,
        Minecraft,
        Comfortaa,
        ComfortaaBold,
        SFProDisplayBold
    }

    public enum NamingStyle {
        Lowercase, // killaura
        LowercaseSpaced, // kill aura
        Normal, // KillAura
        NormalSpaced // Kill Aura
    }

    public enum ColorMode {
        Wave, Static, Pulse
    }

    public final EnumSetting<FontMode> fontMode = new EnumSetting<>("Font Mode", FontMode.ProductSans);
    public final EnumSetting<NamingStyle> namingStyle = new EnumSetting<>("Naming Style", NamingStyle.Normal);
    public final EnumSetting<ColorMode> colorMode = new EnumSetting<>("Color Mode", ColorMode.Wave);
    public final SliderSetting rgbSpeed = new SliderSetting("RGB speed", 2000f, 0f, 10000f, 100f);

    public InterfaceModule() {
        super("Interface", "Settings on how the client should look.", ModuleCategory.Client);
        addSettings(fontMode, namingStyle, colorMode, rgbSpeed);
    }

}