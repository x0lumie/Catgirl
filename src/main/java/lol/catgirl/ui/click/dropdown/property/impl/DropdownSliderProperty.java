package lol.catgirl.ui.click.dropdown.property.impl;

import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.ui.click.dropdown.property.DropdownProperty;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;

import java.awt.*;

public class DropdownSliderProperty implements DropdownProperty {

    private final SliderProperty setting;
    private boolean dragging = false;

    private static final Color BG = new Color(45, 45, 45, 255);
    private static final Color FILL = new Color(120, 60, 160, 220);
    private static final Color TEXT = new Color(220, 220, 220);

    // Dynamic height constants for easy hotswapping
    public static final float TOTAL_HEIGHT = 20f;
    private static final float BAR_HEIGHT = 6f;

    public DropdownSliderProperty(SliderProperty setting) {
        this.setting = setting;
    }

    @Override
    public void draw(float x, float y, float mouseX, float mouseY, float partialTick) {
        float sliderX = x;
        float sliderY = y + 11;
        float sliderW = 95f;

        float min = setting.getMin();
        float max = setting.getMax();
        float value = setting.getValue();

        float percent = (value - min) / (max - min);
        percent = Math.max(0f, Math.min(1f, percent));

        String text = setting.getName() + " " + String.format("%.2f", value);

        DrawUtil.drawString(
                text, x, y + 8, 8f, TEXT,
                ResourceManager.getSelectedFont()
        );

        DrawUtil.roundedRect(
                sliderX, sliderY,
                sliderX + sliderW,
                sliderY + BAR_HEIGHT, 2f,
                BG
        );

        if (percent > 0) {
            DrawUtil.roundedRect(
                    sliderX, sliderY,
                    sliderX + (sliderW * percent),
                    sliderY + BAR_HEIGHT, 2f,
                    FILL
            );
        }

        if (dragging) {
            float newPercent = (mouseX - sliderX) / sliderW;
            newPercent = Math.max(0f, Math.min(1f, newPercent));

            float newValue = min + (max - min) * newPercent;
            setting.setValue(newValue);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY) {
        if (mouseY >= 0 && mouseY <= 20) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased() {
        dragging = false;
    }
}