package lol.catgirl.event.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lol.catgirl.event.Event;
import net.minecraft.world.phys.Vec3;

public class Render3DEvent extends Event {

    public float partialTicks;
    public PoseStack matrixStack;
    public Vec3 cameraPos;

    public Render3DEvent(float partialTicks, PoseStack matrixStack, Vec3 cameraPos) {
        this.partialTicks = partialTicks;
        this.matrixStack = matrixStack;
        this.cameraPos = cameraPos;
    }
}