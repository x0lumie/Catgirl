package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;

public final class PortalGodModeModule extends Module {
    public static final PortalGodModeModule INSTANCE = new PortalGodModeModule();

    public PortalGodModeModule() {
        super("PortalGodMode", "Makes you unkillable in portals.", ModuleCategory.Player);
    }

    @EventHook
    public void onPacket(PacketSendEvent event) {

        if(event.getPacket() instanceof ClientboundTeleportEntityPacket) {
            event.setCancelled(true);
        }
    }
}
