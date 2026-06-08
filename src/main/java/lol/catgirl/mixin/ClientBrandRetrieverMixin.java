package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.ClientBrandRetrieverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverMixin {
    @Inject(method = "getClientModName", at = @At("RETURN"), cancellable = true, remap = false)
    private static void getClientModName(CallbackInfoReturnable<String> info) {
//        info.setReturnValue("lunarclient:v2.21.5-2540");
        ClientBrandRetrieverEvent event = new ClientBrandRetrieverEvent(info);
        Catgirl.INSTANCE.eventBus.post(event);
    }
}

