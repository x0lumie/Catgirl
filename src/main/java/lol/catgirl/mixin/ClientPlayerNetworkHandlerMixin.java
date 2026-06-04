package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.WorldJoinEvent;
import lol.catgirl.utils.IMinecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayerNetworkHandlerMixin implements IMinecraft {

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void onWorldJoin(ClientboundLoginPacket packet, CallbackInfo info) {

        if (mc.level != null) {
            WorldJoinEvent event = new WorldJoinEvent(packet.playerId());
            Catgirl.INSTANCE.eventBus.post(event);
        }
    }
}