package lol.catgirl.manager;

import lol.catgirl.event.Event;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.*;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.client.TickingTimer;
import lol.catgirl.utils.player.PacketUtils;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.*;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LagManager implements IMinecraft {

    public record PacketFilter(Class<?>[] types, boolean[] enabled) {
        public boolean isEnabled() {
            return enabled[0];
        }

        public void setEnabled(boolean v) {
            enabled[0] = v;
        }

        public boolean matches(Class<?> clazz) {
            if (!isEnabled()) return false;
            for (Class<?> t : types) if (t == clazz) return true;
            return false;
        }
    }

    public static ConcurrentLinkedQueue<PacketUtils.TimedPacket> packets = new ConcurrentLinkedQueue<>();
    static TickingTimer enabledTimer = new TickingTimer();
    public static boolean enabled;
    static long amount;

    // regular: confirm transaction, keep alive, entity metadata
    static PacketFilter regular = new PacketFilter(new Class[]{
            ServerboundAcceptTeleportationPacket.class,   // closest to C0F confirm transaction
            ServerboundKeepAlivePacket.class,
            ClientboundSetEntityDataPacket.class
    }, new boolean[]{false});

    // velocity
    static PacketFilter velocity = new PacketFilter(new Class[]{
            ClientboundSetEntityMotionPacket.class,
            ClientboundExplodePacket.class
    }, new boolean[]{false});

    // teleports
    static PacketFilter teleports = new PacketFilter(new Class[]{
            ClientboundPlayerPositionPacket.class,
            ClientboundPlayerAbilitiesPacket.class,
            ClientboundSetHeldSlotPacket.class
    }, new boolean[]{false});

    // players
    static PacketFilter players = new PacketFilter(new Class[]{
            ClientboundRemoveEntitiesPacket.class,
            ClientboundMoveEntityPacket.class,
            ClientboundMoveEntityPacket.Rot.class,
            ClientboundMoveEntityPacket.Pos.class,
            ClientboundMoveEntityPacket.PosRot.class,
            ClientboundTeleportEntityPacket.class,
            ClientboundUpdateAttributesPacket.class,
            ClientboundRotateHeadPacket.class
    }, new boolean[]{false});

    // blink
    static PacketFilter blink = new PacketFilter(new Class[]{
            ServerboundInteractPacket.class,
            ServerboundContainerClosePacket.class,
            ServerboundContainerClickPacket.class,
            ServerboundMovePlayerPacket.class,
            ServerboundMovePlayerPacket.Pos.class,
            ServerboundMovePlayerPacket.Rot.class,
            ServerboundMovePlayerPacket.PosRot.class,
            ServerboundMovePlayerPacket.StatusOnly.class,
            ServerboundUseItemOnPacket.class,
            ServerboundPlayerActionPacket.class,
            ServerboundSetCarriedItemPacket.class,
            ServerboundPlayerAbilitiesPacket.class,
            ServerboundClientInformationPacket.class,
            ServerboundClientCommandPacket.class,
            ServerboundCustomPayloadPacket.class,
            ServerboundPlayerCommandPacket.class,
            ServerboundSwingPacket.class
    }, new boolean[]{false});

    // movement only
    static PacketFilter movement = new PacketFilter(new Class[]{
            ServerboundMovePlayerPacket.class,
            ServerboundMovePlayerPacket.Pos.class,
            ServerboundMovePlayerPacket.Rot.class,
            ServerboundMovePlayerPacket.PosRot.class,
            ServerboundMovePlayerPacket.StatusOnly.class
    }, new boolean[]{false});

    public static PacketFilter[] filters = new PacketFilter[]{regular, velocity, teleports, players, blink, movement};

    private static ServerboundPlayerCommandPacket.Action lastEntityAction = null;

    public static double serverX, serverY, serverZ;
    public static boolean hasServerPosition = false;

    @EventHook
    public void onPacketSend(PacketSendEvent event) {
        event.setCancelled(onPacket(event.getPacket(), event).isCancelled());
    }

    @EventHook
    public void onPacketReceived(PacketReceivedEvent event) {
        event.setCancelled(onPacket(event.packet, event).isCancelled());
    }

    public Event onPacket(Packet<?> packet, Event event) {
        if (!event.isCancelled() && enabled) {
            Class<?> cls = packet.getClass();
            boolean matches = false;
            for (PacketFilter f : filters) {
                if (f.matches(cls)) {
                    matches = true;
                    break;
                }
            }

            if (matches) {
                if (packet instanceof ServerboundPlayerCommandPacket entityAction) {
                    if (entityAction.getAction() == lastEntityAction) {
                        return event;
                    }
                }
                event.setCancelled(true);
                packets.add(new PacketUtils.TimedPacket(packet));
            }
        }

        if (packet instanceof ServerboundMovePlayerPacket move && !event.isCancelled()) {
            if (move.hasPosition()) {
                serverX = move.getX(0);
                serverY = move.getY(0);
                serverZ = move.getZ(0);
                hasServerPosition = true;
            }
        }

        return event;
    }

    public static void dispatch() {
        if (!packets.isEmpty()) {
            boolean wasEnabled = LagManager.enabled;
            LagManager.enabled = false;
            packets.forEach(timedPacket -> PacketUtils.queue(timedPacket.getPacket()));
            LagManager.enabled = wasEnabled;
            packets.clear();
            lastEntityAction = null;
        }
    }

    public static void disable() {
        enabled = false;
        enabledTimer.setTime(enabledTimer.getTime() - 999999999);
        lastEntityAction = null;
    }

    @EventHook
    public void onWorldJoin(WorldJoinEvent event) {
        dispatch();
    }

    @EventHook
    public void onWorldJoin(PostMotionEvent event) {
        if (!(enabled = !enabledTimer.hasTimeElapsed(100) && !(mc.screen instanceof LevelLoadingScreen))) {
            dispatch();
        } else {
            enabled = false;

            Iterator<PacketUtils.TimedPacket> iterator = packets.iterator();
            while (iterator.hasNext()) {
                PacketUtils.TimedPacket timedPacket = iterator.next();
                if (timedPacket.getTime() + amount < System.currentTimeMillis()) {
                    Packet<?> p = timedPacket.getPacket();

                    if (p instanceof ServerboundPlayerCommandPacket cmd) {
                        lastEntityAction = cmd.getAction();
                    }

                    PacketUtils.queue(p);
                    iterator.remove();
                }
            }

            enabled = true;
        }
    }

    public static void spoof(int amount, boolean regular, boolean velocity, boolean teleports, boolean players) {
        spoof(amount, regular, velocity, teleports, players, false);
    }

    public static void spoof(int amount, boolean regular, boolean velocity, boolean teleports, boolean players, boolean blink) {
        spoof(amount, regular, velocity, teleports, players, blink, true);
    }

    public static void spoof(int amount, boolean regular, boolean velocity, boolean teleports, boolean players, boolean blink, boolean movement) {
        enabledTimer.reset();
        LagManager.regular.setEnabled(regular);
        LagManager.velocity.setEnabled(velocity);
        LagManager.teleports.setEnabled(teleports);
        LagManager.players.setEnabled(players);
        LagManager.blink.setEnabled(blink);
        LagManager.movement.setEnabled(movement);
        LagManager.amount = amount;
    }

    public static void blink() {
        spoof(9999999, true, false, false, false, true);
    }
}