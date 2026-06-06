package lol.catgirl.ui.click.dropdown.property.impl;

import lol.catgirl.ui.click.dropdown.property.DropdownProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;

import java.awt.*;

public class DropdownEnumProperty implements DropdownProperty {

    private final EnumProperty<?> setting;

    private static final Color TEXT = new Color(220, 220, 220);
    private static final Color VALUE = new Color(180, 180, 180);

    public DropdownEnumProperty(EnumProperty<?> setting) {
        this.setting = setting;
    }

    @Override
    public void draw(float x, float y, float mouseX, float mouseY, float partialTick) {

        DrawUtil.drawString(
                setting.getName(),
                x,
                y + 6,
                8f,
                TEXT,
                ResourceManager.getSelectedFont()
        );

        String value = String.valueOf(setting.getValue());

        float valueWidth = (float) DrawUtil.getStringWidth(
                value,
                8f,
                ResourceManager.getSelectedFont()
        );

        DrawUtil.drawString(
                value,
                x + 98 - valueWidth - 3,
                y + 6,
                8f,
                VALUE,
                ResourceManager.getSelectedFont()
        );
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY) {
        // handled externally (needs button)
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {

        boolean hovering =
                mouseX >= 0 && mouseX <= 100 &&
                        mouseY >= 0 && mouseY <= 10;

        if (!hovering) return;

        if (button == 0) {
            setting.next(); // left click
        }

        if (button == 1) {
            setting.previous(); // right click
        }
    }

    @Override
    public void mouseReleased() {}
}