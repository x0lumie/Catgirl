package lol.catgirl.mixin;

import lol.catgirl.module.movement.MovementFixModule;
import lol.catgirl.module.movement.NoPushModule;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static lol.catgirl.utils.IMinecraft.mc;


@Mixin(Entity.class)
public abstract class EntityMixin {


    @Shadow public abstract float getYRot();

    @Shadow public abstract void setDeltaMovement(Vec3 velocity);

    @Shadow public abstract Vec3 getDeltaMovement();

    @ModifyArgs(
            method = "moveRelative",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private void mf(Args args) {

    }

    @Inject(method = "push", at = @At("HEAD"), cancellable = true)
    private void noPush(Entity other, CallbackInfo ci) {
        if ((Object) this == mc.player && NoPushModule.INSTANCE.isEnabled()) {
            ci.cancel();
        }
    }
}
