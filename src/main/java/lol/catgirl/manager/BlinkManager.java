package lol.catgirl.manager;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PostMotionEvent;
import lol.catgirl.event.impl.WorldJoinEvent;
import lol.catgirl.utils.player.PacketUtils;
import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;
import java.util.List;

public class BlinkManager {

    public static boolean enabled;
    private static boolean disable = false;
    public static final List<Packet<?>> blinkedSendPackets = new ArrayList<>();
    public static final List<Packet<?>> blinkedReceivePackets = new ArrayList<>();

    @EventHook
    public void onPacketSend(PacketSendEvent event) {
        if (enabled) {
            blinkedSendPackets.add(event.getPacket());
            event.setCancelled(true);
        }
    }

    @EventHook
    public void onPacketReceived(PacketReceivedEvent event) {
        if (enabled) {
            blinkedReceivePackets.add(event.packet);
            event.setCancelled(true);
        }
    }

    @EventHook
    public void onWorldJoin(WorldJoinEvent event) {
        if (enabled) disable();
    }

    @EventHook
    public void onPostMotion(PostMotionEvent event) {
        if (!disable) return;
        enabled = false;
        disable = false;

        List<Packet<?>> sendCopy = new ArrayList<>(blinkedSendPackets);
        List<Packet<?>> receiveCopy = new ArrayList<>(blinkedReceivePackets);
        blinkedSendPackets.clear();
        blinkedReceivePackets.clear();

        for (Packet<?> packet : sendCopy) PacketUtils.queue(packet);
        for (Packet<?> packet : receiveCopy) PacketUtils.queue(packet);
    }

    public static void enable() {
        blinkedSendPackets.clear();
        blinkedReceivePackets.clear();
        enabled = true;
    }

    public static void disable() {
        disable = true;
    }
}