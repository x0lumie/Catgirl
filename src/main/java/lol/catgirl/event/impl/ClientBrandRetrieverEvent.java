package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import lombok.AllArgsConstructor;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@AllArgsConstructor
public class ClientBrandRetrieverEvent extends Event {
    public CallbackInfoReturnable<String> callback;
}
