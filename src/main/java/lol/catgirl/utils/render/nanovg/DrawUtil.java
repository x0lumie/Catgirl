package lol.catgirl.utils.render.nanovg;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.catgirl.utils.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.nanovg.NanoVG.*;

public class DrawUtil implements IMinecraft {
    public static long context = -1L;

    static int previousFramebuffer = -1;
    static int savedSampler = 0;
    private static boolean frameStarted = false;

    public static void init() {
        context = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES);
        if (context == -1L) {
            throw new IllegalStateException("NanoVG Context could not be created.");
        }
        try {
            ResourceManager.init();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static float getFontHeight(Number size, FontResource font) {
        nvgFontFace(context, font.identifier);
        nvgFontSize(context, size.floatValue());

        float[] bounds = new float[4];
        nvgTextBounds(context, 0f, 0f, "Hg", bounds);

        return bounds[3] - bounds[1];
    }

    public static float getFontHeight(Number size) {
        return getFontHeight(size, ResourceManager.FontResources.regular);
    }

    public static void beginUnscaled() {
        preRender();

        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();

        previousFramebuffer = ((GlTexture) renderTarget.getColorTexture()).getFbo(
                ((GlDevice) RenderSystem.getDevice()).directStateAccess(),
                null
        );

        GlStateManager._glBindFramebuffer(GL46.GL_FRAMEBUFFER, previousFramebuffer);

        GlStateManager._viewport(
                0,
                0,
                renderTarget.width,
                renderTarget.height
        );

        savedSampler = GL46.glGetInteger(GL46.GL_SAMPLER_BINDING);
        GL46.glBindSampler(0, 0);

        nvgBeginFrame(
                context,
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                1.0f
        );

        save();
    }

    public static void begin() {
        preRender();


        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();

        previousFramebuffer = ((GlTexture) renderTarget.getColorTexture()).getFbo(
                ((GlDevice)RenderSystem.getDevice()).directStateAccess(),
                null
        );
;
        GlStateManager._glBindFramebuffer(GL46.GL_FRAMEBUFFER, previousFramebuffer);
        GlStateManager._viewport(0, 0, renderTarget.width, renderTarget.height);

        savedSampler = GL46.glGetInteger(GL46.GL_SAMPLER_BINDING);
        GL46.glBindSampler(0, 0);

        nvgBeginFrame(context, mc.getWindow().getWidth(), mc.getWindow().getHeight(), 1.0f);
        save();
        scale(mc.getWindow().getGuiScale());
    }

    public static void end() {
        restore();
        nvgEndFrame(context);
        postRender();
    }

    public static void preRender() {
        GlStateManager._enableBlend();
        GL46.glBlendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager._disableDepthTest();
    }

    public static void postRender() {
        GlStateManager._disableBlend();
        GlStateManager._enableDepthTest();

    }


    public static void scale(float scaleFactor) {
        nvgScale(context, scaleFactor, scaleFactor);
    }

    public static void scale(float xScaleFactor, float yScaleFactor) {
        nvgScale(context, xScaleFactor, yScaleFactor);
    }

    public static void translate(float x, float y) {
        nvgTranslate(context, x, y);
    }


    public static void save() {
        nvgSave(context);
    }

    public static void restore() {
        nvgRestore(context);
    }

    public static void roundedRect(float left, float top, float right, float bottom, float radius, Color color) {
        if (color == null) return;

        nvgBeginPath(context);

        NVGColor nvgColor = NVGColor.calloc();
        try {
            float width = right - left;
            float height = bottom - top;

            nvgRoundedRect(context, left, top, width, height, radius);
            nvgRGBAf(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f, nvgColor);
            nvgFillColor(context, nvgColor);
            nvgFill(context);
        } finally {
            nvgColor.free();
        }

        nvgClosePath(context);
    }

    public static void drawString(String text, Number x, Number y, Number size, Color color, FontResource font) {
        nvgBeginPath(context);
        try (NVGColor nvgColor = NVGColor.calloc()) {
            nvgRGBAf(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f, nvgColor);
            nvgFillColor(context, nvgColor);
            nvgFontFace(context, font.identifier);
            nvgFontSize(context, size.floatValue());
            nvgText(context, x.floatValue(), y.floatValue(), text);
        }
        nvgClosePath(context);
    }

    public static void drawString(String text, Number x, Number y, Number size, Color color) {
        drawString(text, x, y, size, color, ResourceManager.FontResources.regular);
    }

    public static double getStringWidth(String text, Number size, FontResource font) {
        nvgFontFace(context, font.identifier);
        nvgFontSize(context, size.floatValue());
        float[] bounds = new float[]{0f, 0f, 0f, 0f};
        nvgTextBounds(context, 0f, 0f, text, bounds);
        return bounds[2] - bounds[0];
    }

    public static double getStringWidth(String text, Number size) {
        return getStringWidth(text, size, ResourceManager.FontResources.regular);
    }

    public static void roundedRectGradientVarying(float left, float top, float right, float bottom,
                                                  float radiusTL, float radiusTR, float radiusBR, float radiusBL,
                                                  Color c1, Color c2) {
        nvgBeginPath(context);

        float w = right - left;
        float h = bottom - top;

        try (NVGColor start = NVGColor.calloc();
             NVGColor end = NVGColor.calloc();
             NVGPaint paint = NVGPaint.calloc()) {

            nvgRoundedRectVarying(context, left, top, w, h, radiusTL, radiusTR, radiusBR, radiusBL);

            nvgRGBAf(c1.getRed()/255f, c1.getGreen()/255f, c1.getBlue()/255f, c1.getAlpha()/255f, start);
            nvgRGBAf(c2.getRed()/255f, c2.getGreen()/255f, c2.getBlue()/255f, c2.getAlpha()/255f, end);

            nvgLinearGradient(context, left, top, right, top, start, end, paint);

            nvgFillPaint(context, paint);
            nvgFill(context);
        }

        nvgClosePath(context);
    }

    public static void roundedRectVarying(float left, float top, float right, float bottom,
                                          float radiusTL, float radiusTR, float radiusBR, float radiusBL,
                                          Color color) {
        nvgBeginPath(context);

        try (NVGColor nvgColor = NVGColor.calloc()) {
            float width = right - left;
            float height = bottom - top;

            nvgRoundedRectVarying(context,
                    left, top,
                    width, height,
                    radiusTL, radiusTR, radiusBR, radiusBL
            );

            nvgRGBAf(color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    color.getAlpha() / 255f,
                    nvgColor
            );

            nvgFillColor(context, nvgColor);
            nvgFill(context);
        }

        nvgClosePath(context);
    }

    public static void roundedRectGradientAnimated(float left, float top, float right, float bottom,
                                                   float radiusTL, float radiusTR, float radiusBR, float radiusBL,
                                                   Color c1, Color c2,
                                                   float time, float speed) {
        nvgBeginPath(context);

        float w = right - left;
        float h = bottom - top;

        try (NVGColor start = NVGColor.calloc();
             NVGColor end = NVGColor.calloc();
             NVGPaint paint = NVGPaint.calloc()) {

            nvgRoundedRectVarying(context,
                    left, top,
                    w, h,
                    radiusTL, radiusTR, radiusBR, radiusBL
            );

            nvgRGBAf(c1.getRed()/255f, c1.getGreen()/255f, c1.getBlue()/255f, c1.getAlpha()/255f, start);
            nvgRGBAf(c2.getRed()/255f, c2.getGreen()/255f, c2.getBlue()/255f, c2.getAlpha()/255f, end);

            float offset = (time * speed) % (w * 2);

            float startX = left - w + offset;
            float endX   = right + offset;

            nvgLinearGradient(
                    context,
                    startX, top,
                    endX, top,
                    start, end,
                    paint
            );

            nvgFillPaint(context, paint);
            nvgFill(context);
        }

        nvgClosePath(context);
    }

    public static Color interpolate(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));

        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);

        return new Color(r, g, bl, al);
    }

    public static void drawGlowingAnimatedGradientString(String text, float x, float y, float size, float time, float speed, Color... colors) {

        nvgFontSize(context, size);
        nvgFontFace(context, ResourceManager.FontResources.regular.identifier);

        float textWidth = (float) getStringWidth(text, size);

        float offset = (time * speed) % textWidth;

        float progress = (time * 0.5f) % colors.length;
        int index = (int) progress;
        int nextIndex = (index + 1) % colors.length;

        float t = progress - index;

        Color cA = interpolate(colors[index], colors[nextIndex], t);
        Color cB = interpolate(colors[nextIndex], colors[index], t);

        try (NVGPaint paint = NVGPaint.calloc();
             NVGColor nvgA = NVGColor.calloc();
             NVGColor nvgB = NVGColor.calloc()) {

            nvgRGBAf(cA.getRed()/255f, cA.getGreen()/255f, cA.getBlue()/255f, 1f, nvgA);
            nvgRGBAf(cB.getRed()/255f, cB.getGreen()/255f, cB.getBlue()/255f, 1f, nvgB);

            nvgLinearGradient(
                    context,
                    x - textWidth + offset, y,
                    x + offset, y,
                    nvgA, nvgB,
                    paint
            );

            for (int i = 4; i >= 1; i--) {
                float alpha = 0.08f * i;

                nvgBeginPath(context);
                nvgFillPaint(context, paint);
                nvgGlobalAlpha(context, alpha);

                nvgText(context, x - i, y, text);
                nvgText(context, x + i, y, text);
                nvgText(context, x, y - i, text);
                nvgText(context, x, y + i, text);
            }

            nvgBeginPath(context);
            nvgGlobalAlpha(context, 1f);
            nvgFillPaint(context, paint);
            nvgText(context, x, y, text);
        }
    }

    public static void drawShadow(float x, float y, float w, float h, float radius, float feather, Color color) {

        try (NVGPaint paint = NVGPaint.calloc();
             NVGColor inner = NVGColor.calloc();
             NVGColor outer = NVGColor.calloc()) {

            NanoVG.nvgRGBAf(
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    color.getAlpha() / 255f,
                    inner
            );

            NanoVG.nvgRGBAf(0f, 0f, 0f, 0f, outer);

            NanoVG.nvgBoxGradient(
                    context, x, y, w, h, radius,
                    feather, inner, outer,
                    paint
            );

            NanoVG.nvgBeginPath(context);
            NanoVG.nvgRect(context,
                    x - feather,
                    y - feather,
                    w + feather * 2,
                    h + feather * 2
            );

            NanoVG.nvgFillPaint(context, paint);
            NanoVG.nvgFill(context);
        }
    }
    public static void drawOutline(float x, float y, float w, float h, float radius, float thickness, Color color) {
        nvgBeginPath(context);
        try (NVGColor nvgColor = NVGColor.calloc()) {
            nvgRGBAf(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f, nvgColor);
            nvgRoundedRect(context, x, y, w, h, radius);
            nvgStrokeColor(context, nvgColor);
            nvgStrokeWidth(context, thickness);
            nvgStroke(context);
        }
    }

    public static void scissor(float x, float y, float width, float height) {
        nvgScissor(context, x, y, width, height);
    }

    public static void resetScissor() {
        nvgResetScissor(context);
    }

    public static int loadImage(InputStream is) throws IOException {
        if (context == -1L) return -1;

        byte[] data = is.readAllBytes();
        ByteBuffer buffer = MemoryUtil.memAlloc(data.length);
        buffer.put(data).flip();

//        int image = NanoVG.nvgCreateImageMem(
//                context,
//                NanoVG.NVG_IMAGE_GENERATE_MIPMAPS,
//                buffer
//        ); BLUR WTF

        int image = NanoVG.nvgCreateImageMem(
                context,
                NanoVG.NVG_IMAGE_NEAREST | NanoVG.NVG_IMAGE_REPEATX | NanoVG.NVG_IMAGE_REPEATY,
                buffer
        );

        MemoryUtil.memFree(buffer);
        return image;
    }

    public static int loadImage(String path) {
        try (InputStream is = mc
                .getResourceManager()
                .getResource(Identifier.parse(path))
                .orElseThrow()
                .open()) {

            return loadImage(is);

        } catch (Exception e) {
            return -1;
        }
    }

    public static int loadImage(Identifier id) {
        try (InputStream is = mc.getResourceManager()
                .getResource(id)
                .orElseThrow()
                .open()) {
            return loadImage(is);
        } catch (Exception e) {
            return -1;
        }
    }

    public static void drawImage(int imageId, float x, float y, float w, float h, Color tint) {
        if (context == -1L || imageId == -1) return;

        nvgSave(context);

        try (NVGPaint paint = NVGPaint.calloc()) {

            float alpha = tint.getAlpha() / 255f;

            NanoVG.nvgImagePattern(
                    context, x, y,
                    w, h, 0f, imageId,
                    alpha, paint
            );

            nvgBeginPath(context);
            nvgRect(context, x, y, w, h);
            nvgFillPaint(context, paint);
            nvgFill(context);
        }

        nvgRestore(context);
    }

    public static void drawImage(int imageId, float x, float y, float w, float h) {
        drawImage(imageId, x, y, w, h, new Color(255, 255, 255, 255));
    }

    public static void drawPlayerHead(int textureId, float x, float y, float size) {
        if (context == -1L || textureId == -1) return;

        nvgSave(context);

        try (NVGPaint paint = NVGPaint.calloc()) {

            NanoVG.nvgResetTransform(context);

            float uvX = 8f;
            float uvY = 8f;
            float uvW = 8f;
            float uvH = 8f;

            float scale = size / 8f;

            NanoVG.nvgTranslate(context, x, y);
            NanoVG.nvgScale(context, scale, scale);

            NanoVG.nvgImagePattern(
                    context, -uvX, -uvY,
                    64f, 64f,
                    0f, textureId,
                    1f,
                    paint
            );

            nvgBeginPath(context);
            nvgRect(context, 0, 0, 8, 8);
            nvgFillPaint(context, paint);
            nvgFill(context);
        }

        nvgRestore(context);
    }

    public static void drawHeadWithRadius(int textureId, float x, float y, float size, float radius) {
        if (context == -1L || textureId == -1) return;

        nvgSave(context);

        try (NVGPaint paint = NVGPaint.calloc()) {

            float texSize = 64f;

            float uvX = 8f;
            float uvY = 8f;
            float uvSize = 8f;

            float scale = size / uvSize;

            nvgImagePattern(context, x - uvX * scale,
                    y - uvY * scale, texSize * scale, texSize * scale,
                    0f, textureId, 1f,
                    paint
            );

            nvgBeginPath(context);

            if (radius >= size / 2f) {
                nvgCircle(context, x + size / 2f, y + size / 2f, size / 2f);
            } else {
                nvgRoundedRect(context, x, y, size, size, radius);
            }

            nvgFillPaint(context, paint);
            nvgFill(context);

        } finally {
            nvgRestore(context);
        }
    }
}
