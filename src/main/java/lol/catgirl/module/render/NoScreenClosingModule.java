package lol.catgirl.module.render;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;

public final class NoScreenClosingModule extends Module {
    public static final NoScreenClosingModule INSTANCE = new NoScreenClosingModule();

    public NoScreenClosingModule() {
        super("NoScreenClosing",
                "Prevents the server from closing the current screen.",
                ModuleCategory.Render
        );
    }

    @EventHook
    public void onPacketReceive(PacketReceivedEvent event) {
        if (event.packet instanceof ClientboundContainerClosePacket) {
            event.setCancelled(true);
        }
    }
}
