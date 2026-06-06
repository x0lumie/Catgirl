package lol.catgirl.utils.render;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.*;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.utils.IMinecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;

public class RenderUtils implements IMinecraft {
    public static final int[] lastViewport = new int[4];
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    private static final ByteBufferBuilder ALLOC_QUADS = new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE);
    private static final ByteBufferBuilder ALLOC_LINES = new ByteBufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE);


    private static final RenderPipeline PIPELINE_QUADS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/bytes_quads")
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withCull(false)
                    .build()
    );

    private static final RenderType LAYER_QUADS = RenderType.create(
            "bytes_quads",
            RenderSetup.builder(PIPELINE_QUADS).createRenderSetup()
    );

    private static final RenderPipeline PIPELINE_LINES = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/bytes_lines")
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withCull(false)
                    .build()
    );

    public static void drawImage(GuiGraphics guiGraphics, Identifier identifier, int x, int y, int imgWidth, int imgHeight) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED, identifier,
                x, y,
                0, 0,
                imgWidth, imgHeight,
                imgWidth, imgHeight
        );
    }

    public static void renderBlockOutline(BlockPos pos, Render3DEvent event, Color color) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        float minX = (float) (pos.getX() - camX);
        float minY = (float) (pos.getY() - camY);
        float minZ = (float) (pos.getZ() - camZ);

        float maxX = minX + 1f;
        float maxY = minY + 1f;
        float maxZ = minZ + 1f;

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        PoseStack ms = event.matrixStack;
        ms.pushPose();

        Matrix4f mat = ms.last().pose();
        BufferBuilder vb = beginLines();

        vb.addVertex(mat, minX, minY, minZ).setColor(r,g,b,a);
        vb.addVertex(mat, maxX, minY, minZ).setColor(r,g,b,a);

        vb.addVertex(mat, maxX, minY, minZ).setColor(r,g,b,a);
        vb.addVertex(mat, maxX, minY, maxZ).setColor(r,g,b,a);

        vb.addVertex(mat, maxX, minY, maxZ).setColor(r,g,b,a);
        vb.addVertex(mat, minX, minY, maxZ).setColor(r,g,b,a);

        vb.addVertex(mat, minX, minY, maxZ).setColor(r,g,b,a);
        vb.addVertex(mat, minX, minY, minZ).setColor(r,g,b,a);

        vb.addVertex(mat, minX, maxY, minZ).setColor(r,g,b,a);
        vb.addVertex(mat, maxX, maxY, minZ).setColor(r,g,b,a);

        vb.addVertex(mat, maxX, maxY, minZ).setColor(r,g,b,a);
        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r,g,b,a);

        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r,g,b,a);
        vb.addVertex(mat, minX, maxY, maxZ).setColor(r,g,b,a);

        vb.addVertex(mat, minX, maxY, maxZ).setColor(r,g,b,a);
        vb.addVertex(mat, minX, maxY, minZ).setColor(r,g,b,a);

        vb.addVertex(mat, minX, minY, minZ).setColor(r,g,b,a);
        vb.addVertex(mat, minX, maxY, minZ).setColor(r,g,b,a);

        vb.addVertex(mat, maxX, minY, minZ).setColor(r,g,b,a);
        vb.addVertex(mat, maxX, maxY, minZ).setColor(r,g,b,a);

        vb.addVertex(mat, maxX, minY, maxZ).setColor(r,g,b,a);
        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r,g,b,a);

        vb.addVertex(mat, minX, minY, maxZ).setColor(r,g,b,a);
        vb.addVertex(mat, minX, maxY, maxZ).setColor(r,g,b,a);

        LAYER_LINES.draw(vb.buildOrThrow());

        ms.popPose();
    }

    public static Vec3 worldToScreen(Vec3 pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getScreenHeight();
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.position().x;
        double deltaY = pos.y - camera.position().y;
        double deltaZ = pos.z - camera.position().z;

        Vector4f transformed = new Vector4f(
                (float) deltaX, (float) deltaY, (float) deltaZ, 1f
        ).mul(lastWorldSpaceMatrix);

        new Matrix4f(lastProjMat).mul(new Matrix4f(lastModMat)).project(
                transformed.x(), transformed.y(), transformed.z(),
                lastViewport, target
        );

        return new Vec3(
                target.x / mc.getWindow().getGuiScale(),
                (displayHeight - target.y) / mc.getWindow().getGuiScale(),
                target.z
        );
    }

    public static void renderBox(Vec3 pos, Vec3 l, Entity entity, Render3DEvent event, float delta) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        AABB box = entity.getBoundingBox();
        float offMinX = (float)(box.minX - entity.getX()) - 0.12f;
        float offMaxX = (float)(box.maxX - entity.getX()) + 0.12f;
        float offMinY = (float)(box.minY - entity.getY()) - 0.12f;
        float offMaxY = (float)(box.maxY - entity.getY()) + 0.12f;
        float offMinZ = (float)(box.minZ - entity.getZ()) - 0.12f;
        float offMaxZ = (float)(box.maxZ - entity.getZ()) + 0.12f;

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(pos.x - camX, pos.y - camY, pos.z - camZ);
        Matrix4f mat = ms.last().pose();

        BufferBuilder vb = beginQuads();
        fillBox(vb, mat, offMinX, offMaxX, offMinY, offMaxY, offMinZ, offMaxZ, 1f, 1f, 1f, 75 / 255f);
        LAYER_QUADS.draw(vb.buildOrThrow());

        ms.popPose();
    }

    public static void renderBox(Entity e, Render3DEvent event, float delta) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        float ix = (float)(e.xOld + (e.getX() - e.xOld) * delta - camX);
        float iy = (float)(e.yOld + (e.getY() - e.yOld) * delta - camY);
        float iz = (float)(e.zOld + (e.getZ() - e.zOld) * delta - camZ);

        AABB box = e.getBoundingBox();
        float minX = (float)(box.minX - e.getX()) - 0.12f;
        float maxX = (float)(box.maxX - e.getX()) + 0.12f;
        float minY = (float)(box.minY - e.getY()) - 0.12f;
        float maxY = (float)(box.maxY - e.getY()) + 0.12f;
        float minZ = (float)(box.minZ - e.getZ()) - 0.12f;
        float maxZ = (float)(box.maxZ - e.getZ()) + 0.12f;

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(ix, iy, iz);

        Matrix4f mat = ms.last().pose();
        BufferBuilder vb = beginQuads();
        fillBox(vb, mat, minX, maxX, minY, maxY, minZ, maxZ, 1f, 1f, 1f, 75 / 255f);
        LAYER_QUADS.draw(vb.buildOrThrow());

        ms.popPose();
    }

    public static void renderBoxC(Entity e, Render3DEvent event, float delta, Color color) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        float ix = (float)(e.xOld + (e.getX() - e.xOld) * delta - camX);
        float iy = (float)(e.yOld + (e.getY() - e.yOld) * delta - camY);
        float iz = (float)(e.zOld + (e.getZ() - e.zOld) * delta - camZ);

        AABB box = e.getBoundingBox();
        float minX = (float)(box.minX - e.getX()) - 0.12f;
        float maxX = (float)(box.maxX - e.getX()) + 0.12f;
        float minY = (float)(box.minY - e.getY()) - 0.12f;
        float maxY = (float)(box.maxY - e.getY()) + 0.12f;
        float minZ = (float)(box.minZ - e.getZ()) - 0.12f;
        float maxZ = (float)(box.maxZ - e.getZ()) + 0.12f;

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(ix, iy, iz);

        Matrix4f mat = ms.last().pose();
        BufferBuilder vb = beginQuads();

        fillBox(vb, mat, minX, maxX, minY, maxY, minZ, maxZ, r, g, b, a);

        LAYER_QUADS.draw(vb.buildOrThrow());

        ms.popPose();
    }

    public static void fillBox(BufferBuilder vb, Matrix4f mat,
                                float minX, float maxX,
                                float minY, float maxY,
                                float minZ, float maxZ,
                                float r, float g, float b, float a) {
        // Bottom
        vb.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        // Top
        vb.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        // North (z-)
        vb.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        // South (z+)
        vb.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        // West (x-)
        vb.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        // East (x+)
        vb.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        vb.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
    }

    private static BufferBuilder beginQuads() {
        return new BufferBuilder(ALLOC_QUADS,
                PIPELINE_QUADS.getVertexFormatMode(),
                PIPELINE_QUADS.getVertexFormat());
    }

    private static BufferBuilder beginLines() {
        return new BufferBuilder(ALLOC_LINES,
                PIPELINE_LINES.getVertexFormatMode(),
                PIPELINE_LINES.getVertexFormat());
    }

    public static void renderBlock(BlockPos pos, Render3DEvent event, Color color) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        float minX = (float)(pos.getX() - camX);
        float maxX = minX + 1f;
        float minY = (float)(pos.getY() - camY);
        float maxY = minY + 1f;
        float minZ = (float)(pos.getZ() - camZ);
        float maxZ = minZ + 1f;

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        Matrix4f mat = ms.last().pose();

        BufferBuilder vb = beginQuads();
        fillBox(vb, mat, minX, maxX, minY, maxY, minZ, maxZ,
                color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 25 / 255f);
        LAYER_QUADS.draw(vb.buildOrThrow());

        ms.popPose();
    }

    public static int getTextureId(Identifier id) {
        var texture = mc.getTextureManager().getTexture(id);
        if (texture.getTexture() instanceof GlTexture glTexture) {
            return ((GlTexture) texture.getTexture()).glId();
        }

        return -1;
    }

    public static int getPlayerSkin(Player player) {
        if (player instanceof AbstractClientPlayer clientPlayer) {
            var textures = clientPlayer.getSkin();

            Identifier skinId = textures.body().texturePath();

            return getTextureId(skinId);
        }

        return -1;
    }

    public static Identifier getPlayerSkinViaIdentifier(Player player) {
        if (player instanceof AbstractClientPlayer clientPlayer) {
            return clientPlayer.getSkin().body().texturePath();
        }
        return null;
    }

    private static final RenderType LAYER_LINES = RenderType.create(
            "bytes_lines",
            RenderSetup.builder(PIPELINE_LINES).createRenderSetup()
    );

    public static void drawLine(Vec3 start, Vec3 end, Color color, Render3DEvent event) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(-camX, -camY, -camZ);
        Matrix4f mat = ms.last().pose();

        BufferBuilder vb = beginLines();

        vb.addVertex(mat, (float) start.x, (float) start.y, (float) start.z).setColor(r, g, b, a);
        vb.addVertex(mat, (float) end.x, (float) end.y, (float) end.z).setColor(r, g, b, a);

        LAYER_LINES.draw(vb.buildOrThrow());
        ms.popPose();
    }

    public static void renderTracers(Entity target, Color color, Render3DEvent event) {
        Camera camera = mc.gameRenderer.getMainCamera();

        Vector3f look = new Vector3f(0, 0, 1);
        look.rotate(camera.rotation());

        Vec3 start = event.cameraPos.add(look.x() * 0.1, look.y() * 0.1, look.z() * 0.1);

        double x = target.xOld + (target.getX() - target.xOld) * event.partialTicks;
        double y = target.yOld + (target.getY() - target.yOld) * event.partialTicks + (target.getBbHeight() / 2);
        double z = target.zOld + (target.getZ() - target.zOld) * event.partialTicks;
        Vec3 end = new Vec3(x, y, z);

        drawLine(start, end, color, event);
    }

    public static void renderTracers(Vec3 target, Color color, Render3DEvent event) {
        Camera camera = mc.gameRenderer.getMainCamera();

        Vector3f look = new Vector3f(0, 0, 1);
        look.rotate(camera.rotation());

        Vec3 start = event.cameraPos.add(look.x() * 0.1, look.y() * 0.1, look.z() * 0.1);

        drawLine(start, target, color, event);
    }

    public static void renderCircle(Vec3 center, float radius, Color color, Render3DEvent event, float oscillationAmount, float oscillationSpeed) {
        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        float oscillation = (float) Math.sin(System.currentTimeMillis() * oscillationSpeed * 0.001f) * oscillationAmount;

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(-camX, -camY, -camZ);
        Matrix4f mat = ms.last().pose();

        BufferBuilder vb = beginLines();

        int segments = 32;
        for (int i = 0; i <= segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float x1 = (float) (center.x + Math.cos(angle1) * radius);
            float y1 = (float) (center.y + oscillation);
            float z1 = (float) (center.z + Math.sin(angle1) * radius);

            float x2 = (float) (center.x + Math.cos(angle2) * radius);
            float y2 = (float) (center.y + oscillation);
            float z2 = (float) (center.z + Math.sin(angle2) * radius);

            vb.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
            vb.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        }

        LAYER_LINES.draw(vb.buildOrThrow());
        ms.popPose();
    }

    public static void renderCircle(Vec3 center, float radius, Color color, PoseStack poseStack, Matrix4f matrix, float oscillationAmount, float oscillationSpeed) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        float oscillation = (float) Math.sin(System.currentTimeMillis() * oscillationSpeed * 0.001f) * oscillationAmount;

        BufferBuilder vb = beginLines();

        int segments = 32;
        for (int i = 0; i <= segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float x1 = (float) (center.x + Math.cos(angle1) * radius);
            float y1 = (float) (center.y + oscillation);
            float z1 = (float) (center.z + Math.sin(angle1) * radius);

            float x2 = (float) (center.x + Math.cos(angle2) * radius);
            float y2 = (float) (center.y + oscillation);
            float z2 = (float) (center.z + Math.sin(angle2) * radius);

            vb.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
            vb.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        }

        LAYER_LINES.draw(vb.buildOrThrow());
    }

    public static void renderCircleAdvanced(Vec3 center, float radius, Color color, PoseStack poseStack, Matrix4f matrix, float oscillationAmount, float oscillationSpeed, int segments) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        // Multi-directional oscillation
        double time = System.currentTimeMillis() * oscillationSpeed * 0.001f;
        float oscillationY = (float) Math.sin(time) * oscillationAmount;
        float oscillationX = (float) Math.sin(time * 0.7f) * (oscillationAmount * 0.5f);
        float oscillationZ = (float) Math.cos(time * 0.7f) * (oscillationAmount * 0.5f);

        BufferBuilder vb = beginLines();

        for (int i = 0; i <= segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float x1 = (float) (center.x + Math.cos(angle1) * radius + oscillationX);
            float y1 = (float) (center.y + oscillationY);
            float z1 = (float) (center.z + Math.sin(angle1) * radius + oscillationZ);

            float x2 = (float) (center.x + Math.cos(angle2) * radius + oscillationX);
            float y2 = (float) (center.y + oscillationY);
            float z2 = (float) (center.z + Math.sin(angle2) * radius + oscillationZ);

            vb.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
            vb.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        }

        LAYER_LINES.draw(vb.buildOrThrow());
    }


}
