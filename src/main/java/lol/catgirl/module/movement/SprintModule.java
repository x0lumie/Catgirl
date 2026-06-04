package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.BoolSetting;

public final class SprintModule extends Module {
    public static final SprintModule INSTANCE = new SprintModule();

    public final BoolSetting cancelInvis = new BoolSetting("Cancel Invis", false);

    public SprintModule() {
        super("Sprint", "Automatically sprints for you.", ModuleCategory.Movement);
        addSetting(cancelInvis);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if(mc.player == null) return;
        if(mc.player.isInvisible() && cancelInvis.getValue()) {
            return;
        }

        mc.options.keySprint.setDown(true);
    }

    @Override
    public void onDisable() {
        mc.options.keySprint.setDown(false);
    }
}
