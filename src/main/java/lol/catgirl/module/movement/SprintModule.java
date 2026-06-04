package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class SprintModule extends Module {
    public static final SprintModule INSTANCE = new SprintModule();

    public SprintModule() {
        super("Sprint", "Automatically sprints for you.", ModuleCategory.Movement);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        mc.options.keySprint.setDown(true);
    }

    @Override
    public void onDisable() {
        mc.options.keySprint.setDown(false);
    }
}
