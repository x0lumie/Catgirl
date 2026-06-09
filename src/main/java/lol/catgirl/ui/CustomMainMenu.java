package lol.catgirl.ui;

import lol.catgirl.Catgirl;
import lol.catgirl.module.hud.WatermarkModule;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

import java.awt.*;

public class CustomMainMenu extends Screen {

    public CustomMainMenu() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        float centerX = width / 2f;
        float startY = height / 2f - 20f;

        this.clearWidgets();
        int btnW = 150;
        int btnH = 20;

        addRenderableWidget(Button.builder(Component.literal("Singleplayer"), b -> minecraft.setScreen(new SelectWorldScreen(this))).bounds((int)(centerX - btnW / 2f), (int) startY, btnW, btnH).build());
        addRenderableWidget(Button.builder(Component.literal("Multiplayer"), b -> minecraft.setScreen(new JoinMultiplayerScreen(this))).bounds((int)(centerX - btnW / 2f), (int) startY + 24, btnW, btnH).build());
        addRenderableWidget(Button.builder(Component.literal("Options"), b -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))).bounds((int)(centerX - btnW / 2f), (int) startY + 48, btnW, btnH).build());
//        addRenderableWidget(Button.builder(Component.literal("Alt Repository"), b -> minecraft.setScreen(new AltManagerScreen())).bounds((int)(centerX - btnW / 2f), (int) startY + 72, btnW, btnH).build());
        addRenderableWidget(Button.builder(Component.literal("Quit Game"), b -> minecraft.stop()).bounds((int)(centerX - btnW / 2f), (int) startY + 72, btnW, btnH).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);

        super.render(graphics, mouseX, mouseY, delta);

        DrawUtil.begin();

        String title = Catgirl.NAME;

        drawTitle(graphics, title, width / 2f, height / 2f - 180f);


        DrawUtil.end();
    }

    private void drawTitle(GuiGraphics gg, String text, float centerX, float y) {
        long time = System.currentTimeMillis();

        Color start = WatermarkModule.PINK;
        Color end = start.darker().darker().darker();

        float size = 56f;
        float scale = size / 18f;

        float width = font.width(text) * scale;
        float x = centerX - (width / 2f);
        var pose = gg.pose();

        pose.pushMatrix();
        pose.scale(scale, scale);

        float scaledX = x / scale;
        float scaledY = y / scale;

        for (int i = 0; i < text.length(); i++) {
            String s = String.valueOf(text.charAt(i));

            float wave = (float) ((Math.sin((time / 500.0) + (i * 0.15)) + 1.0) / 2.0);
            Color color = DrawUtil.interpolate(start, end, wave);

            int rgb = color.getRGB();

            gg.drawString(font, s, (int) scaledX, (int) (scaledY + 27), rgb, true);

            scaledX += font.width(s);
        }

        pose.popMatrix();
    }

    @Override
    public void renderBackground(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        long time = System.currentTimeMillis();

        int w = this.width;
        int h = this.height;

        DrawUtil.begin();

        float t = time * 0.003f;

        try (NVGColor c1 = NVGColor.calloc();
             NVGColor c2 = NVGColor.calloc();
             NVGPaint paint = NVGPaint.calloc()) {

            NanoVG.nvgRGBAf(0.05f, 0.20f, 0.18f, 1f, c1);
            NanoVG.nvgRGBAf(0.02f, 0.08f, 0.15f, 1f, c2);

            NanoVG.nvgLinearGradient(
                    DrawUtil.context, 0, 0, w, h, c1, c2, paint
            );

            NanoVG.nvgBeginPath(DrawUtil.context);
            NanoVG.nvgRect(DrawUtil.context, 0, 0, w, h);
            NanoVG.nvgFillPaint(DrawUtil.context, paint);
            NanoVG.nvgFill(DrawUtil.context);
        }

        for (int i = 0; i < 10; i++) {

            float speed = 0.25f + i * 0.03f;
            float seed = i * 1000f;

            float baseT = t * speed;

            float nx = (float) Math.sin(baseT + seed)
                    + (float) Math.sin(baseT * 0.5f + seed * 2.3f) * 0.6f;

            float ny = (float) Math.cos(baseT + seed * 1.7f)
                    + (float) Math.cos(baseT * 0.4f + seed * 3.1f) * 0.6f;

            float driftX = (float) Math.sin(t * 0.2f + i) * 0.4f;
            float driftY = (float) Math.cos(t * 0.18f + i) * 0.4f;

            float x = (nx + driftX) * 0.5f + 0.5f;
            float y = (ny + driftY) * 0.5f + 0.5f;

            x *= w;
            y *= h;

            float size = 120f + (i * 25f);

            float pulse = (float) Math.sin(t * 2.0 + seed) * 0.5f + 0.5f;
            float finalSize = size * (0.92f + pulse * 0.15f);

            try (NVGColor blob = NVGColor.calloc()) {

                NanoVG.nvgRGBAf(
                        0.0f,
                        0.7f + pulse * 0.2f,
                        0.5f + pulse * 0.25f,
                        0.10f,
                        blob
                );

                NanoVG.nvgBeginPath(DrawUtil.context);
                NanoVG.nvgCircle(DrawUtil.context, x, y, finalSize);
                NanoVG.nvgFillColor(DrawUtil.context, blob);
                NanoVG.nvgFill(DrawUtil.context);
            }
        }

        try (NVGColor overlay = NVGColor.calloc()) {

            NanoVG.nvgRGBAf(0f, 0f, 0f, 0.25f, overlay);

            NanoVG.nvgBeginPath(DrawUtil.context);
            NanoVG.nvgRect(DrawUtil.context, 0, 0, w, h);
            NanoVG.nvgFillColor(DrawUtil.context, overlay);
            NanoVG.nvgFill(DrawUtil.context);
        }

        DrawUtil.end();
    }


    @Override
    protected void renderBlurredBackground(@NonNull GuiGraphics graphics) {
    }
}