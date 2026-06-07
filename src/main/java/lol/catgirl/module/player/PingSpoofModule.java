package lol.catgirl.module.player;


import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.player.PacketUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public class PingSpoofModule extends Module {
    public static final PingSpoofModule INSTANCE = new PingSpoofModule();

    private static final Queue<DelayedPacket> packetQueue = new ConcurrentLinkedQueue<>();

    public final SliderProperty spoofDelay = new SliderProperty("Delay", 100, 0, 1000000, 1);

    public PingSpoofModule() {
        super("PingSpoof", "Spoofs your ping.", ModuleCategory.Player);
    }

    @EventHook
    public void onPacketReceive(PacketReceivedEvent event) {

        if (event.packet instanceof ClientboundKeepAlivePacket packet) {
            event.setCancelled(true);

            packetQueue.add(new DelayedPacket(
                    new ServerboundKeepAlivePacket(packet.getId()),
                    System.currentTimeMillis() + spoofDelay.getValue().intValue()
            ));
        }

        if (event.packet instanceof ClientboundPingPacket packet) {
            event.setCancelled(true);

            packetQueue.add(new DelayedPacket(
                    new ServerboundPongPacket(packet.getId()),
                    System.currentTimeMillis() + spoofDelay.getValue().intValue()
            ));
        }
    }

    @EventHook
    public void onTick() {
        while (!packetQueue.isEmpty()) {
            DelayedPacket delayed = packetQueue.peek();

            if (System.currentTimeMillis() >= delayed.time) {
                PacketUtils.sendPacket(delayed.packet);
                packetQueue.poll();
            } else {
                break;
            }
        }
    }

    private record DelayedPacket(Packet<?> packet, long time) {
    }
}
