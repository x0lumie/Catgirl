package lol.catgirl.module.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.PoseStack;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.RenderWorldEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.ColorUtils;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.awt.*;

import static net.minecraft.client.renderer.RenderPipelines.DEBUG_FILLED_SNIPPET;

public final class ChinaHatModule extends Module {
    public final static ChinaHatModule INSTANCE = new ChinaHatModule();

    public final SliderProperty radius = new SliderProperty("Radius", 0.5f, 0.5f, 1.0f, 0.01f);
    public final SliderProperty height = new SliderProperty("Height", 0.3f, 0.1f, 0.7f, 0.01f);
    public final SliderProperty position = new SliderProperty("Position", 0.1f, -0.5f, 0.5f, 0.01f);
    public final SliderProperty rotation = new SliderProperty("Rotation", 5.0f, 0.0f, 5.0f, 0.1f);
    public final SliderProperty angles = new SliderProperty("Angles", 32f, 4f, 90f, 1f);
    public final BoolProperty firstPerson = new BoolProperty("First Person", false);
    public final BoolProperty shade = new BoolProperty("Shade", true);
    public final EnumProperty<ColorMode> colorMode = new EnumProperty<>("Color Mode", ColorMode.Rainbow);

    public enum ColorMode {
        Rainbow, Theme
    }

    public ChinaHatModule() {
        super("ChinaHat",
                "Shows a custom render of a hat.",
                ModuleCategory.Render
        );
        addSettings(
                radius, height, position, rotation,
                angles, firstPerson, shade, colorMode
        );
    }

    private static final RenderPipeline DEBUG_FILLED_BOX =
            RenderPipelines.register(
                    RenderPipeline.builder(DEBUG_FILLED_SNIPPET)
                            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                            .withDepthWrite(false)
                            .withLocation("pipeline/debug_filled_box")
                            .build()
            );

    private static final RenderType FILLED =
            RenderType.create(
                    "filled_box",
                    RenderSetup.builder(DEBUG_FILLED_BOX)
                            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                            .setOutputTarget(OutputTarget.MAIN_TARGET)
                            .sortOnUpload()
                            .createRenderSetup()
            );

    @EventHook
    public void onWorldRenderer(RenderWorldEvent event) {
        if (mc.player == null) return;

        if (mc.options.getCameraType().isFirstPerson() && !firstPerson.getValue()) {
            return;
        }

        float pTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        var provider = mc.levelRenderer.renderBuffers.bufferSource();
        var buffer = provider.getBuffer(FILLED);

        var camera = event.renderState.cameraRenderState.pos;
        var stack = new PoseStack();

        var player = mc.player;
        var posVec = player.getPosition(pTick);

        double x = posVec.x - camera.x;
        double y = posVec.y - camera.y;
        double z = posVec.z - camera.z;

        double yOffset = player.getBbHeight() + position.getValue() - (player.isCrouching() ? 0.23 : 0.0);

        stack.pushPose();
        stack.translate(x, y + yOffset, z);

        float yaw = (player.tickCount + pTick) * rotation.getValue();

        float camYaw = -mc.player.yBodyRot;
        stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw + camYaw));

        PoseStack.Pose pose = stack.last();

        int n = angles.getValue().intValue();
        float r = radius.getValue();
        float h = height.getValue();

        float apexAlpha = shade.getValue() ? 0.8f : 0.5f;
        float edgeAlpha = shade.getValue() ? 0.3f : 0.5f;

        for (int i = 0; i < n; i++) {
            double a0 = i * Math.PI * 2.0 / n;
            double a1 = (i + 1) * Math.PI * 2.0 / n;

            float x0 = (float) (Math.cos(a0) * r);
            float z0 = (float) (Math.sin(a0) * r);
            float x1 = (float) (Math.cos(a1) * r);
            float z1 = (float) (Math.sin(a1) * r);

            int cApex = getColor(0, n);
            int c0 = getColor(i, n);
            int c1 = getColor(i + 1, n);

            buffer.addVertex(pose, 0f, h, 0f).setColor(applyAlpha(cApex, apexAlpha));
            buffer.addVertex(pose, x0, 0f, z0).setColor(applyAlpha(c0, edgeAlpha));
            buffer.addVertex(pose, x1, 0f, z1).setColor(applyAlpha(c1, edgeAlpha));
            buffer.addVertex(pose, x1, 0f, z1).setColor(applyAlpha(c1, edgeAlpha));

            buffer.addVertex(pose, 0f, h, 0f).setColor(applyAlpha(cApex, apexAlpha));
            buffer.addVertex(pose, x1, 0f, z1).setColor(applyAlpha(c1, edgeAlpha));
            buffer.addVertex(pose, x0, 0f, z0).setColor(applyAlpha(c0, edgeAlpha));
            buffer.addVertex(pose, x0, 0f, z0).setColor(applyAlpha(c0, edgeAlpha));
        }

        stack.popPose();

        provider.endBatch();
    }

    private int getColor(int i, int max) {
        if (colorMode.getValue() == ColorMode.Rainbow) {
            float hue = (float) i / max;
            return Color.HSBtoRGB(hue, 0.65f, 1.0f);
        }

        Color c1 = ColorUtils.getClientTheme();

        float t = (float) i / max;

        int r = (int) (((c1.getRGB() >> 16) & 255) * (1 - t) +
                ((c1.darker().darker().getRGB() >> 16) & 255) * t);
        int g = (int) (((c1.getRGB() >> 8) & 255) * (1 - t) +
                ((c1.darker().darker().getRGB() >> 8) & 255) * t);
        int b = (int) ((c1.getRGB() & 255) * (1 - t) +
                (c1.darker().darker().getRGB() & 255) * t);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int applyAlpha(int color, float alpha) {
        int a = (int) (alpha * 255.0f) & 0xFF;
        return (a << 24) | (color & 0x00FFFFFF);
    }

    @Override
    public String getFinalSuffix() {
        return colorMode.getValue().toString();
    }
}
