package lol.catgirl.mixin;

import lol.catgirl.module.movement.NoPushModule;
import lol.catgirl.utils.IMinecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements IMinecraft {
    @Inject(method = "push", at = @At("HEAD"), cancellable = true)
    private void noPush(Entity other, CallbackInfo ci) {
        if ((Object) this == mc.player && NoPushModule.INSTANCE.isEnabled()) {
            ci.cancel();
        }
    }
}
