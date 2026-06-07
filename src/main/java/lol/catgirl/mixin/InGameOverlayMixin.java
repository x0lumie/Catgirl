package lol.catgirl.mixin;

import lol.catgirl.module.render.NoRenderModule;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenEffectRenderer.class)
public class InGameOverlayMixin {

    @Inject(method={"renderFire"}, at={@At(value="INVOKE", target= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V")}, cancellable=true)
    private static void renderFireOverlay(PoseStack matrices, MultiBufferSource vertexConsumers, TextureAtlasSprite sprite, CallbackInfo info) {
        if(NoRenderModule.INSTANCE.fire.getValue() && NoRenderModule.INSTANCE.isEnabled()) {
            info.cancel();
        }
    }
}
