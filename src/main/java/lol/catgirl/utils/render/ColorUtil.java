package lol.catgirl.utils.render;

import lol.catgirl.utils.IMinecraft;

import java.awt.*;

public class ColorUtil implements IMinecraft {
    public static Color changeOpacity(Color color, int opacity) {
        if (opacity < 1 || opacity > 255) {
            throw new IllegalArgumentException("Opacity must be between 1 and 255");
        }

        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }
}
