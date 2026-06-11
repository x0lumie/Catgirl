package lol.catgirl.module.player;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class AntiCactusModule extends Module {
    public static final AntiCactusModule INSTANCE = new AntiCactusModule();

    public AntiCactusModule() {
        super("AntiCactus", "Prevents taking cactus damage.", ModuleCategory.Player);
    }
    // handled in cactus mixin
}
