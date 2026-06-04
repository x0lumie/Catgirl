package lol.catgirl.module.player;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class ScaffoldModule extends Module {
    public static final ScaffoldModule INSTANCE = new ScaffoldModule();

    public ScaffoldModule() {
        super("Scaffold", "Places blocks under you creating a bridge.", ModuleCategory.Player);
    }
}
