package lol.catgirl.mixin;

import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<
        T extends LivingEntity,
        S extends LivingEntityRenderState> implements IMinecraft {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;isEntityUpsideDown(Lnet/minecraft/world/entity/LivingEntity;)Z",
                    shift = At.Shift.AFTER
            )
    )
    public void updateRenderState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
        if (livingEntity == mc.player) {

            if (RotationUtils.yawChanged) {
                float g = Mth.rotLerp(
                        f,
                        RotationUtils.getLastRotationYaw(),
                        RotationUtils.getRotationYaw()
                );

                RotationUtils.turnHead(g, livingEntityRenderState);

            }

            livingEntityRenderState.xRot = RotationUtils.getLerpedPitch(f, livingEntity);
        }
    }
}