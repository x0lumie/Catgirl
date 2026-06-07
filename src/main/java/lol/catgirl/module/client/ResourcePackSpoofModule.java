package lol.catgirl.module.client;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.utils.player.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;

public class ResourcePackSpoofModule extends Module {
    public static final ResourcePackSpoofModule INSTANCE = new ResourcePackSpoofModule();

    public ResourcePackSpoofModule() {
        super("ResourcePackSpoof",
                "Spoofs resource packs. so you dont need to apply them!",
                ModuleCategory.Client
        );
    }

    @EventHook
    public void onPacket(PacketReceivedEvent event) {
        if(!this.isEnabled() || mc.player == null) {
            return;
        }

        if(event.packet instanceof ClientboundResourcePackPushPacket packet) {
            PacketUtils.sendPacket(new ServerboundResourcePackPacket(packet.id(), ServerboundResourcePackPacket.Action.ACCEPTED));
            PacketUtils.sendPacket(new ServerboundResourcePackPacket(packet.id(), ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
            event.setCancelled(true);
        }
    }
}
