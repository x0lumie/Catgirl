package lol.catgirl.mixin;

import io.netty.channel.ChannelHandlerContext;
import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.PacketReceivedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {

    @Shadow
    private PacketListener packetListener;

    @Shadow
    private static <T extends PacketListener> void genericsFtw(Packet<T> packet, PacketListener packetListener) {}


    @Inject(
            method = "channelRead0",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onPacketReceivedRaw(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {

        PacketListener listener = this.packetListener;

        if (listener == null || !(listener instanceof ClientPacketListener)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            return;
        }

        PacketReceivedEvent event = new PacketReceivedEvent(packet);
        Catgirl.INSTANCE.eventBus.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
