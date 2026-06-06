package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.PlayerJumpEvent;
import lol.catgirl.event.impl.PlayerJumpFactorEvent;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements IMinecraft {

    @Inject(at = @At("HEAD"), method = "jumpFromGround")
    private void jump(CallbackInfo ci) {
        if((Object) this == mc.player) {
            PlayerUtils.jumpAge = mc.player.tickCount;
            PlayerUtils.lastModTime = System.currentTimeMillis();
        }
    }


    @Inject(at = @At("HEAD"), method = "handleDamageEvent")
    private void onDamage(CallbackInfo info) {
        if((Object) this == mc.player) {
            PlayerUtils.hurtAge = mc.player.tickCount;
        }
    }

    @Inject(
            method = "jumpFromGround",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifyJump(CallbackInfo info) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (!(self instanceof Player)) return;
        PlayerJumpFactorEvent event = new PlayerJumpFactorEvent();
        Catgirl.INSTANCE.eventBus.post(event);

        if(event.isCancelled()) {
            double jumpFactor = event.factor;
            self.setDeltaMovement(
                    self.getDeltaMovement().x, jumpFactor,
                    self.getDeltaMovement().z
            );
            self.needsSync = true;
            info.cancel();
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        if ((Object)this == mc.player) {
            Catgirl.INSTANCE.eventBus.post(new PlayerJumpEvent());
        }
    }
}
