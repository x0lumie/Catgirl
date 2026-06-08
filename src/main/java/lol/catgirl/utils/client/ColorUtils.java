package lol.catgirl.utils.client;

import lol.catgirl.module.client.InterfaceModule;
import lol.catgirl.module.hud.WatermarkModule;
import lol.catgirl.utils.render.nanovg.DrawUtil;

import java.awt.Color;

public class ColorUtils {

    public static Color changeOpacity(Color color, int opacity) {
        if (opacity < 1 || opacity > 255) {
            throw new IllegalArgumentException("Opacity must be between 1 and 255");
        }

        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    public static Color getClientTheme() {
        return WatermarkModule.INSTANCE.PURPLE;
    }

    public static Color getClientTheme(int index) {
        return getAnimatedColor(index, 1.0f);
    }

    public static Color getAnimatedColor(int index, float alpha) {
        long time = System.currentTimeMillis();

        Color PINK = new Color(255, 105, 180);
        Color PURPLE = new Color(155, 89, 255);

        Color color;

        switch (InterfaceModule.INSTANCE.colorMode.getValue()) {
            case Static -> {
                color = PINK;
            }

            case Wave -> {
                float wave = (float)
                        ((Math.sin((time / 350.0) + (index * 0.30)) + 1.0) / 2.0);

                color = DrawUtil.interpolate(PINK, PURPLE, wave);
            }

            case Pulse -> {
                float pulse = (float)
                        ((Math.sin(time / 350.0) + 1.0) / 2.0);

                color = DrawUtil.interpolate(PINK, PURPLE, pulse);
            }

            default -> {
                color = PINK;
            }
        }

        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int) (255 * alpha)
        );
    }
}