package lol.catgirl.module.hud;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.InterfaceModule;
import lol.catgirl.setting.impl.BoolSetting;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;

import java.awt.*;

import static lol.catgirl.utils.render.nanovg.ResourceManager.getSelectedFont;

public class WatermarkModule extends Module {
    public static final WatermarkModule INSTANCE = new WatermarkModule();

    private static final Color PINK = new Color(255, 105, 180);
    private static final Color PURPLE = new Color(155, 89, 255);
    // we need a theme manager

    public WatermarkModule() {
        super("Watermark", "Shows the client watermark.", ModuleCategory.Hud);
        addSettings(watermarkVersion, shadow);
    }

    public final BoolSetting watermarkVersion = new BoolSetting("Show Version", true);
    public final BoolSetting shadow = new BoolSetting("Shadow", false);

    @EventHook
    public void onRender(RenderTickEvent event) {
        DrawUtil.begin();

        float x = 5F;
        float y = 5F;

        long time = System.currentTimeMillis();

        String watermark = Catgirl.NAME;

        float size = 20F;
        float height = size;
        float padding = 4F;

        float offsetX = x + padding;

        boolean drawVersion = watermarkVersion.getValue()
                        && watermark.equalsIgnoreCase("Catgirl");

        for (int i = 0; i < watermark.length(); i++) {

            char c = watermark.charAt(i);
            String s = String.valueOf(c);

            float wave = (float) ((Math.sin((time / 350.0) + (i * 0.25)) + 1.0) / 2.0);
            Color color = DrawUtil.interpolate(PINK, PURPLE, wave);

            float charWidth = (float) DrawUtil.getStringWidth(s, size, getSelectedFont());

            if(shadow.getValue()){
                DrawUtil.drawShadow(
                        offsetX - 1.5F,
                        y + 1F,
                        charWidth + 3.0F,
                        height - 2F,
                        4F,
                        10F,
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 90)
                );
            }

            DrawUtil.drawString(
                    s, offsetX + 0.8F,
                    y + height - 3.2F,
                    size,
                    new Color(0, 0, 0, 120),
                    getSelectedFont()
            );

            DrawUtil.drawString(s, offsetX,
                    y + height - 4F,
                    size,
                    color,
                    getSelectedFont()
            );

            offsetX += charWidth;
        }

        if (drawVersion) {
            DrawUtil.drawString(
                    Catgirl.VERSION,
                    offsetX + 6F,
                    y + height - 11,
                    10,
                    Color.GRAY,
                    getSelectedFont()
            );
        }

        DrawUtil.end();
    }
}
