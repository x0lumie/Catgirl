package lol.catgirl.module.hud;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.hud.modulelist.CatgirlModuleList;
import lol.catgirl.module.hud.modulelist.ClassicModuleList;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;

public final class ModuleListModule extends Module {
    public static final ModuleListModule INSTANCE = new ModuleListModule();

    public enum Mode {
        Catgirl,
        Classic
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Catgirl);
    public final BoolProperty background = new BoolProperty("Background", true);
    public final BoolProperty bar = new BoolProperty("Bar", true);
    public final BoolProperty isLeft = new BoolProperty("Position Left", false).hide(() -> mode.getValue() == Mode.Classic);
    public final BoolProperty suffix = new BoolProperty("Suffix", true);
    public final BoolProperty shadow = new BoolProperty("Shadow", false).hide(() -> mode.getValue() == Mode.Classic);
    public final SliderProperty textSize = new SliderProperty("Text Size", 9, 6, 20, 1).hide(() -> mode.getValue() == Mode.Classic);
    public final SliderProperty suffixTextSize = new SliderProperty("Suffix Text Size", 9, 6, 20, 1).hide(() -> !suffix.getValue() || mode.getValue() == Mode.Classic);
    public final SliderProperty paddingX = new SliderProperty("Padding X", 2, 0, 10, 1).hide(() -> mode.getValue() == Mode.Classic);
    public final SliderProperty paddingY = new SliderProperty("Padding Y", 1, 0, 10, 1).hide(() -> mode.getValue() == Mode.Classic);
    public final SliderProperty spacing = new SliderProperty("Spacing", 11, 5, 25, 1).hide(() -> mode.getValue() == Mode.Classic);
    public final SliderProperty cornerRadius = new SliderProperty("Corner Radius", 2, 0, 15, 1).hide(() -> mode.getValue() == Mode.Classic);
    public final SliderProperty sidebarWidth = new SliderProperty("Sidebar Width", 1.2f, 0.5f, 6.0f, 0.1f).hide(() -> !bar.getValue() || mode.getValue() == Mode.Classic);
    public final SliderProperty animSpeed = new SliderProperty("Animation Speed", 0.2f, 0.05f, 1.0f, 0.01f).hide(() -> mode.getValue() == Mode.Classic);
    public final SliderProperty xPositioningOffset = new SliderProperty("X Offset", 3, 0, 50, 1).hide(() -> mode.getValue() == Mode.Classic);
    public final BoolProperty excludeCombat = new BoolProperty("Exclude Combat", false);
    public final BoolProperty excludeMovement = new BoolProperty("Exclude Movement", false);
    public final BoolProperty excludePlayer = new BoolProperty("Exclude Player", false);
    public final BoolProperty excludeRender = new BoolProperty("Exclude Render", false);
    public final BoolProperty excludeHud = new BoolProperty("Exclude Hud", false);
    public final BoolProperty excludeClient = new BoolProperty("Exclude Client", false);
    public final BoolProperty excludeGhost = new BoolProperty("Exclude Ghost", false);

    public ModuleListModule() {
        super("ModuleList", "Shows a hud with currently enabled modules.", ModuleCategory.Hud);
        addSettings(mode,
                background, animSpeed, xPositioningOffset, shadow, bar,
                isLeft, suffix, textSize, suffixTextSize, paddingX,
                paddingY, spacing, cornerRadius, sidebarWidth,
                excludeCombat, excludeMovement, excludePlayer, excludeHud,
                excludeRender, excludeGhost, excludeClient
        );
    }

    @EventHook
    public void onRender(RenderTickEvent event) {
        if (mc.player == null) return;

        if (mode.getValue() == Mode.Catgirl) {
            CatgirlModuleList.onRender(event, this);
        }
    }

    @EventHook
    public void onRender(Render2DEvent event) {
        if (mc.player == null) return;

        if (mode.getValue() == Mode.Classic) {
            ClassicModuleList.onRender(event, this);
        }
    }
}
