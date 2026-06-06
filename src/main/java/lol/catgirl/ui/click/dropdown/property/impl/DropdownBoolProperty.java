package lol.catgirl.ui.click.dropdown.property.impl;

import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.ui.click.dropdown.property.DropdownProperty;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;

import java.awt.*;

public class DropdownBoolProperty implements DropdownProperty {

    private final BoolProperty setting;

    private static final float TOGGLE_WIDTH = 20;
    private static final float TOGGLE_HEIGHT = 9;
    private static final float TOGGLE_PADDING = 2;

    private static final Color BG_OFF = new Color(100, 100, 100, 200);
    private static final Color BG_ON = new Color(120, 60, 160, 200);
    private static final Color CIRCLE = new Color(255, 255, 255, 255);
    private static final Color TEXT = new Color(220, 220, 220);

    private final float maxOffset;
    private float circleOffsetX;
    private float targetOffsetX;

    public DropdownBoolProperty(BoolProperty setting) {
        this.setting = setting;

        float circleDiameter = TOGGLE_HEIGHT - (TOGGLE_PADDING * 2);
        this.maxOffset = TOGGLE_WIDTH - (TOGGLE_PADDING * 2) - circleDiameter;

        this.targetOffsetX = setting.getValue() ? maxOffset : 0;
        this.circleOffsetX = targetOffsetX;
    }

    @Override
    public void draw(float x, float y, float mouseX, float mouseY, float partialTick) {

        float diff = targetOffsetX - circleOffsetX;
        circleOffsetX += diff * 0.25f;

        if (Math.abs(diff) < 0.001f) {
            circleOffsetX = targetOffsetX;
        }

        DrawUtil.drawString(
                setting.getName(),
                x,
                y + 6,
                8f,
                TEXT,
                ResourceManager.getSelectedFont()
        );

        float toggleX = x + 98 - TOGGLE_WIDTH - 3;
        float toggleY = y;

        Color bgColor = setting.getValue() ? BG_ON : BG_OFF;

        DrawUtil.roundedRect(
                toggleX,
                toggleY,
                toggleX + TOGGLE_WIDTH,
                toggleY + TOGGLE_HEIGHT,
                TOGGLE_HEIGHT / 2f,
                bgColor
        );

        float circleDiameter = TOGGLE_HEIGHT - (TOGGLE_PADDING * 2);

        float circleX = toggleX + TOGGLE_PADDING + circleOffsetX;
        float circleY = toggleY + TOGGLE_PADDING;

        DrawUtil.roundedRect(
                circleX,
                circleY,
                circleX + circleDiameter,
                circleY + circleDiameter,
                circleDiameter / 2f,
                CIRCLE
        );
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY) {

        float toggleX = 100 - TOGGLE_WIDTH - 3;
        float toggleY = 0;

        if (mouseX >= toggleX && mouseX <= toggleX + TOGGLE_WIDTH &&
                mouseY >= toggleY && mouseY <= toggleY + TOGGLE_HEIGHT) {

            setting.toggle();
            targetOffsetX = setting.getValue() ? maxOffset : 0;
        }
    }

    @Override
    public void mouseReleased() {}
}