package lol.catgirl.module.ghost;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class NoJumpDelayModule extends Module {
    public static final NoJumpDelayModule INSTANCE = new NoJumpDelayModule();

    public NoJumpDelayModule() {
        super("NoJumpDelay", "Removes the jump delay", ModuleCategory.Ghost);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        mc.player.noJumpDelay = 0;
    }
}
