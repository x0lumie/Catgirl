package lol.catgirl.utils.client;

import lol.catgirl.module.hud.WatermarkModule;

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
}