package lol.catgirl.module.movement;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class AntiPowderSnowModule extends Module {
    public static final AntiPowderSnowModule INSTANCE = new AntiPowderSnowModule();

    public AntiPowderSnowModule() {
        super("AntiPowderSnow", "Stops you falling through powdered snow.", ModuleCategory.Movement);
    }
}
