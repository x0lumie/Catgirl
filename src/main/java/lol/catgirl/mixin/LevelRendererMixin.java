package lol.catgirl.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.ResourceHandle;
import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.RenderWorldEvent;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Final
    private LevelRenderState levelRenderState;


    @Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V", shift = At.Shift.AFTER), remap = false)
    private void injectRenderMain(GpuBufferSlice gpuBufferSlice,
                                  LevelRenderState worldRenderState, ProfilerFiller profiler,
                                  Matrix4f matrix4f, @SuppressWarnings("rawtypes") ResourceHandle handle,
                                  @SuppressWarnings("rawtypes") ResourceHandle handle2, boolean bl,
                                  @SuppressWarnings("rawtypes") ResourceHandle handle3, @SuppressWarnings("rawtypes")
                                  ResourceHandle handle4, CallbackInfo ci) {

        final var event = new RenderWorldEvent(gpuBufferSlice, worldRenderState, matrix4f);
        Catgirl.INSTANCE.eventBus.post(event);
    }
}
