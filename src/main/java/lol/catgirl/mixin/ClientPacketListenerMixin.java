package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.ChatEvent;
import lol.catgirl.utils.IMinecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin implements IMinecraft {

    @Inject(at = @At("HEAD"), method = "sendChat", cancellable = true)
    public void sendChatMessage(String content, CallbackInfo ci) {
        ChatEvent e = new ChatEvent(content);
        Catgirl.INSTANCE.eventBus.post(e);
        if (e.isCancelled()) ci.cancel();
    }
}
