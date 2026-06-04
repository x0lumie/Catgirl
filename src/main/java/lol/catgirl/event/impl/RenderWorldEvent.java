package lol.catgirl.event.impl;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import lol.catgirl.event.Event;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.joml.Matrix4f;

@RequiredArgsConstructor
public class RenderWorldEvent extends Event {
    public final GpuBufferSlice slice;
    public final LevelRenderState renderState;
    public final Matrix4f matrix;
}