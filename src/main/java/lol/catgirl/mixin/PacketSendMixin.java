package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.PacketSendEvent;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class PacketSendMixin {

    @Final
    @Shadow
    protected Connection connection;

    @Unique
    public void sendPacketDirect(Packet<?> packet) {
        this.connection.send(packet);
    }


    @Inject(at = @At("HEAD"), method = "send", cancellable = true)
    public void packetEvent(Packet<?> packet, CallbackInfo ci) {
        PacketSendEvent event = new PacketSendEvent(packet);
        Catgirl.INSTANCE.eventBus.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}