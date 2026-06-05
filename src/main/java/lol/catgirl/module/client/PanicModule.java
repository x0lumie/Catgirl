package lol.catgirl.module.client;

import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.Module;

public final class PanicModule extends Module {
    public static final PanicModule INSTANCE = new PanicModule();

    public PanicModule() {
        super("Panic",
                "Panic - Disables all modules incase a admin teleports to you.",
                ModuleCategory.Client
        );
    }

    @Override
    public void onEnable() {

        for (Module module : ModuleManager.modules) {
            if (module.isEnabled() && module != this) {
                module.toggle();
            }
        }

        toggle();
    }
}
