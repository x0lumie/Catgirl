package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.OpenScreenEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import net.minecraft.client.gui.screens.DeathScreen;

public final class AutoRespawnModule extends Module {
    public static final AutoRespawnModule INSTANCE = new AutoRespawnModule();

    public AutoRespawnModule() {
        super("AutoRespawn", "Automatically makes you respawn.", ModuleCategory.Player);
    }

    @EventHook
    public void onScreen(OpenScreenEvent event) {
        if (!(event.screen instanceof DeathScreen)) return;

        mc.player.respawn();
        event.setCancelled(true);
    }
}
