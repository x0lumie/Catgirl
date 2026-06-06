package lol.catgirl.ui.click.dropdown.property;

import net.minecraft.client.input.MouseButtonEvent;

public interface DropdownProperty {
    void draw(float x, float y, float mouseX, float mouseY, float partialTick);

    default void mouseClicked(double mouseX, double mouseY) {

    }

    default void mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {

    }

    default void mouseReleased() {}
}
