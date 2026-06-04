package lol.catgirl.utils.player;

import lol.catgirl.utils.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.protocol.Packet;

@UtilityClass
public class PacketUtil implements IMinecraft {
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
}

