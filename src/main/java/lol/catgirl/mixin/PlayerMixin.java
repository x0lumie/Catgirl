package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.PlayerInteractionRangeEvent;
import lol.catgirl.module.ghost.ReachModule;
import lol.catgirl.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin implements IMinecraft {

    @Inject(method = "entityInteractionRange()D", at = @At("HEAD"), cancellable = true)
    private void onEntityInteractionRange(CallbackInfoReturnable<Double> cir) {
        final var reachModule = ReachModule.INSTANCE;

        if (reachModule == null || !reachModule.isEnabled()) {
            return;
        }

        PlayerInteractionRangeEvent.Entity event = new PlayerInteractionRangeEvent.Entity(
                reachModule.getAmount());
        Catgirl.INSTANCE.eventBus.post(event);

        if (event.isCancelled()) {
            cir.setReturnValue(event.getReach());
        }
    }

    @Inject(method = "blockInteractionRange()D", at = @At("HEAD"), cancellable = true)
    private void onBlockInteractionRange(CallbackInfoReturnable<Double> cir) {
        final var reachModule = ReachModule.INSTANCE;

        if (reachModule == null || !reachModule.isEnabled()) {
            return;
        }
        PlayerInteractionRangeEvent.Block event = new PlayerInteractionRangeEvent.Block(
                ReachModule.INSTANCE.getAmount());
        Catgirl.INSTANCE.eventBus.post(event);

        if (event.isCancelled()) {
            cir.setReturnValue(event.getReach());
        }
    }
}
