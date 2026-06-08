package lol.catgirl.utils.player;

import lol.catgirl.utils.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.*;

import java.util.Arrays;

@UtilityClass
public class PacketUtils implements IMinecraft {
    public static void queue(final Packet<?> packet) {
        if (packet == null) {
            System.out.println("Packet is null");
            return;
        }

        if (Arrays.stream(serverbound).anyMatch(clazz -> clazz == packet.getClass())) {
            mc.getConnection().send(packet);
        } else {
            ((Packet<ClientGamePacketListener>) packet).handle(mc.getConnection());
        }
    }

    public static void sendSilentPacket(Packet<?> packet) {
        final var nh = mc.getConnection();
        if (nh == null) return;
        nh.getConnection().send(packet, null, true);
    }

    @SuppressWarnings("unchecked")
    public static void handlePacketSilently(Packet<?> packet) {
        ((Packet<ClientboundPacketListener>)packet).handle(mc.getConnection());
    }


    public static void sendPacket(Packet<?> packet) {
        final var nh = mc.getConnection();
        if (nh == null) return;
        nh.send(packet);
    }

    public static class TimedPacket {
        private final Packet<?> packet;
        private final long time;

        public TimedPacket(final Packet<?> packet, final long time) {
            this.packet = packet;
            this.time = time;
        }

        public TimedPacket(final Packet<?> packet) {
            this.packet = packet;
            this.time = System.currentTimeMillis();
        }

        public Packet<?> getPacket() {
            return packet;
        }

        public long getTime() {
            return time;
        }
    }

    public final static Class<?>[] serverbound = new Class[]{
            ServerboundKeepAlivePacket.class,
            ServerboundChatPacket.class,
            ServerboundInteractPacket.class,
            ServerboundMovePlayerPacket.class,
            ServerboundMovePlayerPacket.Pos.class,
            ServerboundMovePlayerPacket.Rot.class,
            ServerboundMovePlayerPacket.PosRot.class,
            ServerboundMovePlayerPacket.StatusOnly.class,
            ServerboundPlayerActionPacket.class,
            ServerboundUseItemOnPacket.class,
            ServerboundSetCarriedItemPacket.class,
            ServerboundSwingPacket.class,
            ServerboundPlayerCommandPacket.class,
            ServerboundMoveVehiclePacket.class,
            ServerboundContainerClosePacket.class,
            ServerboundContainerClickPacket.class,
            ServerboundSetCreativeModeSlotPacket.class,
            ServerboundSignUpdatePacket.class,
            ServerboundPlayerAbilitiesPacket.class,
            ServerboundCommandSuggestionPacket.class,
            ServerboundClientInformationPacket.class,
            ServerboundClientCommandPacket.class,
            ServerboundCustomPayloadPacket.class,
            ServerboundTeleportToEntityPacket.class,
            ServerboundResourcePackPacket.class,
    };
}

