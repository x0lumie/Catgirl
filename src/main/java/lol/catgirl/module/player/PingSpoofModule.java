package lol.catgirl.module.player;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class PingSpoofModule extends Module {
    public static final PingSpoofModule INSTANCE = new PingSpoofModule();

    public PingSpoofModule() {
        super("PingSpoof", "Spoofs your ping.", ModuleCategory.Player);
    }


}

