package lol.catgirl.module.player;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class PortalsModule extends Module {
    public static final PortalsModule INSTANCE = new PortalsModule();

    public PortalsModule() {
        super("Portals", "Keeps your inventory open in portals.", ModuleCategory.Player);
    }

    // done in localplayermixin
}
