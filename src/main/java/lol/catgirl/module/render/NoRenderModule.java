package lol.catgirl.module.render;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;

public final class NoRenderModule extends Module {
    public static final NoRenderModule INSTANCE = new NoRenderModule();

    public final BoolProperty fire = new BoolProperty("Fire", false);
    public final BoolProperty scoreboard = new BoolProperty("Scoreboard", false);
    public final BoolProperty activeEffects = new BoolProperty("Active effects", false);

    public NoRenderModule() {
        super("NoRender",
                "Disables certain events from rendering.",
                ModuleCategory.Render
        );
        addSettings(fire, scoreboard, activeEffects);
    }
}
