package lol.catgirl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.utils.render.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V"))
    void renderer_postWorldRender(LevelRenderer instance, GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, Operation<Void> original) {
        original.call(instance, allocator, tickCounter, renderBlockOutline, camera, positionMatrix, matrix4f, projectionMatrix, fogBuffer, fogColor, renderSky);

        ProfilerFiller prof = Profiler.get();
        prof.popPush("rendererLibWorld");

        PoseStack matrix = new PoseStack();

        matrix.mulPose(positionMatrix);

        RenderUtil.lastProjMat.set(projectionMatrix);
        RenderUtil.lastModMat.set(RenderSystem.getModelViewMatrix());
        RenderUtil.lastWorldSpaceMatrix.set(matrix.last().pose());
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, RenderUtil.lastViewport);

        GlStateManager._depthMask(true);
        GlStateManager._disableBlend();


        Catgirl.INSTANCE.eventBus.post(new Render3DEvent(
                    tickCounter.getGameTimeDeltaPartialTick(true),
                    matrix, camera.position()));

    }

//    @Inject(at = {@At(value = "HEAD")}, method = {"bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"}, cancellable = true)
//    public void tiltWhenHurt(PoseStack stack, float blah, CallbackInfo info) {
//        if(NoHurtCamModule.INSTANCE.isEnabled()) {
//            info.cancel();
//        }
//    }
}