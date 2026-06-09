package lol.catgirl.ui;

import lol.catgirl.Catgirl;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import lombok.NonNull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// TODO: when first opening the game and you resize the window, the context in the main menu does not update the scale
// TODO: theme manager please :3

public class CustomMainMenu extends Screen {

    private static final Color PINK        = new Color(255, 105, 180);
    private static final Color PURPLE      = new Color(140, 60,  220);
    private static final Color PINK_DIM    = new Color(255, 105, 180, 180);
    private static final Color PURPLE_DIM  = new Color(140, 60,  220, 180);

    private record MenuButton(String label, Runnable action) {}

    private final List<MenuButton> buttons = new ArrayList<>();
    private int hoveredIndex = -1;

    private static final float REF_W = 854f;

    private float scale()       { return width / REF_W; }
    private float btnW()        { return 180f * scale(); }
    private float btnH()        { return 32f  * scale(); }
    private float btnGap()      { return 10f  * scale(); }
    private float btnRadius()   { return 8f   * scale(); }
    private float labelSize()   { return 13f  * scale(); }
    private float titleSize()   { return 52f  * scale(); }
    private float subSize()     { return 13f  * scale(); }

    public CustomMainMenu() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        buttons.clear();
        buttons.add(new MenuButton("Singleplayer", () -> minecraft.setScreen(new SelectWorldScreen(this))));
        buttons.add(new MenuButton("Multiplayer",  () -> minecraft.setScreen(new JoinMultiplayerScreen(this))));
        buttons.add(new MenuButton("Options",      () -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))));
        buttons.add(new MenuButton("Quit Game",    () -> minecraft.stop()));
    }

    private float btnStartY() {
        float totalH = buttons.size() * btnH() + (buttons.size() - 1) * btnGap();
        return height * 0.58f - totalH / 2f;
    }

    private float btnX() {
        return width / 2f - btnW() / 2f;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        float x = btnX();
        float y = btnStartY();
        for (MenuButton btn : buttons) {
            if (event.x() >= x && event.x() <= x + btnW() && event.y() >= y && event.y() <= y + btnH()) {
                btn.action().run();
                return true;
            }
            y += btnH() + btnGap();
        }
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        float x = btnX();
        float y = btnStartY();
        hoveredIndex = -1;
        for (int i = 0; i < buttons.size(); i++) {
            if (mouseX >= x && mouseX <= x + btnW() && mouseY >= y && mouseY <= y + btnH()) {
                hoveredIndex = i;
            }
            y += btnH() + btnGap();
        }

        DrawUtil.begin();
        drawBackground();
        drawTitle();
        drawButtons();
        DrawUtil.end();
    }

    private void drawTitle() {
        String text = Catgirl.NAME;
        float size = titleSize();
        float time = System.currentTimeMillis() / 1000f;

        float textWidth  = (float) DrawUtil.getStringWidth(text, size, ResourceManager.FontResources.regular);
        float fontHeight = DrawUtil.getFontHeight(size, ResourceManager.FontResources.regular);
        float baseY = btnStartY() - fontHeight - 40f * scale();
        float x = width / 2f - textWidth / 2f;

        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            float charWidth = (float) DrawUtil.getStringWidth(ch, size, ResourceManager.FontResources.regular);

            float wave = (float) Math.sin(time * 2.2f + i * 0.45f) * 4f * scale();
            float t    = ((float) Math.sin(time * 1.1f + i * 0.3f) + 1f) / 2f;
            Color color = DrawUtil.interpolate(PINK, PURPLE, t);

            DrawUtil.drawString(ch, x, baseY + 20f * scale() + wave, size, color, ResourceManager.FontResources.regular);
            x += charWidth;
        }

        float sw    = subSize();
        String subtitle = "the only client that gets you catgirls";
        float subWidth  = (float) DrawUtil.getStringWidth(subtitle, sw, ResourceManager.FontResources.regular);
        float subX      = width / 2f - subWidth / 2f;
        float subAlpha  = ((float) Math.sin(time * 1.4f) + 1f) / 2f;
        Color subColor  = new Color(210, 160, 255, (int)(120 + subAlpha * 80));
        DrawUtil.drawString(subtitle, subX, baseY + fontHeight + 8f * scale(), sw, subColor, ResourceManager.FontResources.regular);
    }

    private void drawButtons() {
        float x = btnX();
        float y = btnStartY();

        for (int i = 0; i < buttons.size(); i++) {
            MenuButton btn     = buttons.get(i);
            boolean    hovered = (hoveredIndex == i);
            float      hoverT  = hovered ? 1f : 0f;

            Color gradL = DrawUtil.interpolate(PURPLE_DIM, PURPLE, hoverT);
            Color gradR = DrawUtil.interpolate(PINK_DIM,   PINK,   hoverT);

            DrawUtil.drawShadow(x, y, btnW(), btnH(), btnRadius(),
                    hovered ? 18f * scale() : 10f * scale(),
                    new Color(PINK.getRed(), PINK.getGreen(), PINK.getBlue(), hovered ? 90 : 40));

            DrawUtil.roundedRectGradientVarying(
                    x, y, x + btnW(), y + btnH(),
                    btnRadius(), btnRadius(), btnRadius(), btnRadius(),
                    gradL, gradR);

            float borderAlpha = hovered ? 0.9f : 0.45f;
            DrawUtil.drawOutline(x, y, btnW(), btnH(), btnRadius(), 1f * scale(),
                    new Color(255, 160, 230, (int)(255 * borderAlpha)));

            float labelWidth = (float) DrawUtil.getStringWidth(btn.label(), labelSize(), ResourceManager.FontResources.regular);
            float fontHeight = DrawUtil.getFontHeight(labelSize(), ResourceManager.FontResources.regular);
            float lx = x + btnW() / 2f - labelWidth / 2f;
            float ly = y + btnH() / 2f - fontHeight / 2f + fontHeight;

            Color labelColor = hovered
                    ? new Color(255, 255, 255, 255)
                    : new Color(230, 200, 255, 220);

            DrawUtil.drawString(btn.label(), lx, ly, labelSize(), labelColor, ResourceManager.FontResources.regular);

            y += btnH() + btnGap();
        }
    }

    private void drawBackground() {
        float time = System.currentTimeMillis() * 0.001f;
        int w = width;
        int h = height;

        try (NVGColor c1 = NVGColor.calloc();
             NVGColor c2 = NVGColor.calloc();
             NVGPaint paint = NVGPaint.calloc()) {

            NanoVG.nvgRGBAf(0.10f, 0.03f, 0.14f, 1f, c1);
            NanoVG.nvgRGBAf(0.05f, 0.01f, 0.10f, 1f, c2);
            NanoVG.nvgLinearGradient(DrawUtil.context, 0, 0, w, h, c1, c2, paint);
            NanoVG.nvgBeginPath(DrawUtil.context);
            NanoVG.nvgRect(DrawUtil.context, 0, 0, w, h);
            NanoVG.nvgFillPaint(DrawUtil.context, paint);
            NanoVG.nvgFill(DrawUtil.context);
        }

        float[][] blobDefs = {
                {0.20f, 0.25f, 220f, 0.0f,  0.90f, 0.25f, 0.60f},
                {0.80f, 0.20f, 180f, 1.1f,  0.55f, 0.15f, 0.75f},
                {0.50f, 0.60f, 240f, 2.0f,  0.85f, 0.20f, 0.55f},
                {0.15f, 0.75f, 150f, 2.9f,  0.60f, 0.10f, 0.80f},
                {0.85f, 0.65f, 170f, 3.8f,  0.90f, 0.30f, 0.65f},
                {0.65f, 0.15f, 140f, 4.7f,  0.50f, 0.10f, 0.70f},
        };

        for (float[] b : blobDefs) {
            float bx     = b[0] * w + (float) Math.sin(time * 0.28f + b[3]) * 65f;
            float by     = b[1] * h + (float) Math.cos(time * 0.22f + b[3]) * 55f;
            float pulse  = ((float) Math.sin(time * 1.7f + b[3]) + 1f) / 2f;
            float radius = b[2] * (0.88f + pulse * 0.20f);

            try (NVGColor blob        = NVGColor.calloc();
                 NVGPaint radPaint    = NVGPaint.calloc();
                 NVGColor transparent = NVGColor.calloc()) {

                NanoVG.nvgRGBAf(b[4], b[5], b[6] + pulse * 0.08f, 0.50f, blob);
                NanoVG.nvgRGBAf(0f, 0f, 0f, 0f, transparent);
                NanoVG.nvgRadialGradient(DrawUtil.context, bx, by, 0f, radius, blob, transparent, radPaint);

                NanoVG.nvgBeginPath(DrawUtil.context);
                NanoVG.nvgCircle(DrawUtil.context, bx, by, radius);
                NanoVG.nvgFillPaint(DrawUtil.context, radPaint);
                NanoVG.nvgFill(DrawUtil.context);
            }
        }

        try (NVGColor inner   = NVGColor.calloc();
             NVGColor outer   = NVGColor.calloc();
             NVGPaint vignette = NVGPaint.calloc()) {

            NanoVG.nvgRGBAf(0f, 0f, 0f, 0f, inner);
            NanoVG.nvgRGBAf(0f, 0f, 0f, 0.75f, outer);
            NanoVG.nvgRadialGradient(DrawUtil.context,
                    w / 2f, h / 2f,
                    Math.min(w, h) * 0.28f,
                    Math.max(w, h) * 0.82f,
                    inner, outer, vignette);
            NanoVG.nvgBeginPath(DrawUtil.context);
            NanoVG.nvgRect(DrawUtil.context, 0, 0, w, h);
            NanoVG.nvgFillPaint(DrawUtil.context, vignette);
            NanoVG.nvgFill(DrawUtil.context);
        }
    }

    @Override
    public void renderBackground(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected void renderBlurredBackground(@NonNull GuiGraphics graphics) {}
}