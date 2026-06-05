package lol.catgirl.module.movement;

import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.Module;

public class NoPushModule extends Module {
    public static final NoPushModule INSTANCE = new NoPushModule();

    public NoPushModule() {
        super("NoPush", "Stop entities from displacing your collision.",
                ModuleCategory.Movement);
    }
}
