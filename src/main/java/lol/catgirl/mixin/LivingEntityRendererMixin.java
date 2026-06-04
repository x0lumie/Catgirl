package lol.catgirl.mixin;

import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.player.RotationUtil;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;


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

            if (RotationUtil.yawChanged) {
                float g = Mth.rotLerp(
                        f,
                        RotationUtil.getLastRotationYaw(),
                        RotationUtil.getRotationYaw()
                );

                RotationUtil.turnHead(g, livingEntityRenderState);

            }

            livingEntityRenderState.xRot = RotationUtil.getLerpedPitch(f, livingEntity);
        }
    }
}