package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

public final class XCarryModule extends Module {
    public static final XCarryModule INSTANCE = new XCarryModule();

    public XCarryModule() {
        super("XCarry", "Allows you to carry items in crafting.", ModuleCategory.Player);
    }

    @EventHook
    public void onPacket(PacketSendEvent event) {
        if (mc.player == null) return;

        Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundContainerClosePacket closeScreenPacket) {
            if (closeScreenPacket.getContainerId() == mc.player.inventoryMenu.containerId) {
                event.setCancelled(true);
            }
        }
    }
}
