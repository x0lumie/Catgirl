package lol.catgirl.module.render.nametags.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lol.catgirl.event.impl.RenderWorldEvent;
import lol.catgirl.module.render.NametagsModule;
import lol.catgirl.module.render.nametags.NametagsMode;
import lol.catgirl.utils.client.ColorUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class MinecraftNametagsMode implements NametagsMode {
    public final NametagsModule parent;

    public MinecraftNametagsMode(NametagsModule parent) {
        this.parent = parent;
    }

    private static final float SCALE = 0.035f;

    @Override
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null || mc.level == null) return;

        float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var camera = event.renderState.cameraRenderState;
        var cameraPos = camera.pos;
        var dispatcher = mc.getEntityRenderDispatcher();
        MultiBufferSource.BufferSource provider = mc.levelRenderer.renderBuffers.bufferSource();
        PoseStack matrices = new PoseStack();
        int backgroundColor = ((int) (mc.options.getBackgroundOpacity(0.25f) * 255.0f)) << 24;

        for (Player player : mc.level.players()) {
            if (player == mc.player && mc.options.getCameraType().isFirstPerson()) continue;

            String name = player.getName().getString();
            float x = -mc.font.width(name) / 2.0f;
            int light = dispatcher.getPackedLightCoords(player, tickDelta);

            var pos = player.getPosition(tickDelta);
            double y = pos.y + player.getBbHeight() + (player.isShiftKeyDown() ? 0.5f : 0.6f);
            float scale = getScale(pos, cameraPos);

            matrices.pushPose();
            matrices.translate(
                    pos.x - cameraPos.x,
                    y - cameraPos.y,
                    pos.z - cameraPos.z
            );
            matrices.mulPose(camera.orientation);
            matrices.scale(scale, -scale, scale);

            mc.font.drawInBatch(
                    name,
                    x,
                    0.0f,
                    -1,
                    false,
                    matrices.last().pose(),
                    provider,
                    Font.DisplayMode.SEE_THROUGH,
                    backgroundColor,
                    light
            );

            drawGradientText(provider, matrices, name, x, light);
            matrices.popPose();
        }

        provider.endBatch();
    }

    private float getScale(Vec3 entityPos, Vec3 cameraPos) {
        if (!parent.scaleByDistance.getValue()) return SCALE;

        float distance = (float) entityPos.distanceTo(cameraPos);
        return Math.clamp(SCALE * Math.max(1.0f, distance / 8.0f), SCALE, SCALE * 4.0f);
    }

    private void drawGradientText(MultiBufferSource provider, PoseStack matrices, String text, float x, int light) {
        for (int i = 0; i < text.length(); i++) {
            float t = text.length() == 1 ? 0.0f : (float) i / (text.length() - 1);
            int color =
                    interpolateColor(
                    ColorUtils.getClientTheme().getRGB(),
                    ColorUtils.getClientTheme().darker().darker().darker().getRGB(),
                    t
            );
            String character = String.valueOf(text.charAt(i));

            mc.font.drawInBatch(
                    character,
                    x,
                    0.0f,
                    color,
                    false,
                    matrices.last().pose(),
                    provider,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    light
            );

            x += mc.font.width(character);
        }
    }

    private int interpolateColor(int startColor, int endColor, float t) {
        int r = (int) (((startColor >> 16) & 255) * (1.0f - t) + ((endColor >> 16) & 255) * t);
        int g = (int) (((startColor >> 8) & 255) * (1.0f - t) + ((endColor >> 8) & 255) * t);
        int b = (int) ((startColor & 255) * (1.0f - t) + (endColor & 255) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
