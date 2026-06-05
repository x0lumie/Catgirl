package lol.catgirl.module.hud;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;

import java.awt.*;
import java.util.Objects;

import static lol.catgirl.utils.render.nanovg.ResourceManager.getSelectedFont;

// if i wuz a catgurl wu u date m n stuf?!

public final class WatermarkModule extends Module {
    public static final WatermarkModule INSTANCE = new WatermarkModule();

    private final Color PINK = new Color(255, 105, 180);
    public final Color PURPLE = new Color(155, 89, 255);

    public WatermarkModule() {
        super("Watermark", "Shows the client watermark.", ModuleCategory.Hud);
        addSettings(mode, watermarkVersion, shadow);
    }

    public enum Mode {
        Catgirl,
        Catsense,
        Simple
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Catgirl);
    public final BoolProperty watermarkVersion = new BoolProperty("Show Version", true);
    public final BoolProperty shadow = new BoolProperty("Shadow", false).hide(()->mode.getValue()== Mode.Simple);

    @EventHook
    public void onRender(RenderTickEvent event) {
        DrawUtil.begin();

        switch (mode.getValue()) {
            case Catgirl -> {
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
            }
            case Catsense -> {
                Color DARK = new Color(50, 57, 57);
                Color INNER = new Color(23, 23, 23);

                float x = 5f;
                float y = 5f;

                String serverIp;
                String ping;

                if (mc.getCurrentServer() == null) {
                    serverIp = "local";
                    ping = "0ms";
                } else {
                    serverIp = Objects.requireNonNull(mc.getCurrentServer()).ip;
                    ping = mc.getCurrentServer().ping + "ms";
                }

                String part1 = "cat";
                String part2 = "sense";
                String part3 = " - " + mc.player.getName().getString()
                        + " - " + serverIp + " - " + ping;

                float padding = 4f;
                float textSize = 8f;
                float height = 13F;
                float radius = 0f;

                float textWidth = (float) (DrawUtil.getStringWidth(part1, textSize)
                        + DrawUtil.getStringWidth(part2, textSize)
                        + DrawUtil.getStringWidth(part3, textSize));

                float width = textWidth + (padding * 2f);

                if (shadow.getValue()){
                    DrawUtil.drawShadow(
                            x - 2f,
                            y - 1f,
                            width,
                            height,
                            radius + 1f,
                            8f,
                            new Color(0, 0, 0, 140)
                    );
                }

                DrawUtil.roundedRect(x, y, x + width, y + height, radius, DARK);

                DrawUtil.roundedRect(
                        x + 1.5f, y + 1.5f, x + width - 1.5f,
                        y + height - 1.5f,
                        radius, INNER
                );

                DrawUtil.roundedRect(
                        x + 1.5F,
                        y + height - 2.5F,
                        x + width - 1.5F,
                        y + height - 1.5F,
                        radius,
                        PURPLE
                );

                float textY = y + (height / 2f) + (textSize / 2f) - 2f;
                float currentX = x + padding;

                DrawUtil.drawString(part1, currentX,
                        textY, textSize, Color.WHITE
                );
                currentX += (float) DrawUtil.getStringWidth(part1, textSize);

                DrawUtil.drawString(part2, currentX, textY, textSize,
                        PURPLE
                );
                currentX += (float) DrawUtil.getStringWidth(part2, textSize);

                DrawUtil.drawString(
                        part3,
                        currentX,
                        textY,
                        textSize,
                        Color.WHITE
                );
            }
            case Simple -> {
                float x = 2f;
                float y = 10f;

                String name = Catgirl.NAME;

                DrawUtil.drawString(
                        "v"+Catgirl.VERSION, x + x + 2 + x + 25, y,
                        10, Color.WHITE,
                        ResourceManager.FontResources.roboto
                );

                DrawUtil.drawString(
                        name, x, y, 10, PURPLE,
                        ResourceManager.FontResources.roboto
                );
            }
        }

        DrawUtil.end();
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
