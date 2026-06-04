package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.*;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {


    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attackEntity(Player player, Entity target, CallbackInfo ci) {

        PlayerAttackPreEvent event = new PlayerAttackPreEvent(target);
        Catgirl.INSTANCE.eventBus.post(event);

        if (event.isCancelled()) { ci.cancel(); }
    }


        @Inject(
                method = "attack",
                at = @At("TAIL")
        )
        private void attackEntityPost(Player player, Entity target, CallbackInfo ci) {
            PlayerAttackPostEvent event = new PlayerAttackPostEvent(target);
            Catgirl.INSTANCE.eventBus.post(event);
        }
}
