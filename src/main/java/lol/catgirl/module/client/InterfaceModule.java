package lol.catgirl.module.client;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;

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

    public final EnumProperty<FontMode> fontMode = new EnumProperty<>("Font Mode", FontMode.ProductSans);
    public final EnumProperty<NamingStyle> namingStyle = new EnumProperty<>("Naming Style", NamingStyle.Normal);
    public final EnumProperty<ColorMode> colorMode = new EnumProperty<>("Color Mode", ColorMode.Wave);
    public final SliderProperty rgbSpeed = new SliderProperty("RGB speed", 2000f, 0f, 10000f, 100f);

    public InterfaceModule() {
        super("Interface", "Settings on how the client should look.", ModuleCategory.Client);
        addSettings(fontMode, namingStyle, colorMode, rgbSpeed);
    }

}