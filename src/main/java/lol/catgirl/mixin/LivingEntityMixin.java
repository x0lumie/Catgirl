package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.PlayerJumpFactorEvent;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.player.PlayerUtil;
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
            PlayerUtil.jumpAge = mc.player.tickCount;
            PlayerUtil.lastModTime = System.currentTimeMillis();
        }
    }
    @Inject(at = @At("HEAD"), method = "handleDamageEvent")
    private void onDamage(CallbackInfo info) {
        if((Object) this == mc.player) {
            PlayerUtil.hurtAge = mc.player.tickCount;
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
}
