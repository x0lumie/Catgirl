package lol.catgirl.utils.client;

import lol.catgirl.utils.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

public class ClickUtil implements IMinecraft {

    public static void action(Button button, boolean action) {
        KeyMapping bind = switch (button) {
            case LEFT -> mc.options.keyAttack;
            case RIGHT -> mc.options.keyUse;
        };

        if (action) {
            press(bind);
        } else {
            release(bind);
        }
    }

    private static void press(KeyMapping key) {
        if (key == null) return;

        key.setDown(true);

        int currentPresses = key.clickCount;
        key.clickCount = currentPresses + 1;
    }

    private static void release(KeyMapping key) {
        if (key == null) return;

        key.setDown(false);
    }

    public enum Button {
        LEFT,
        RIGHT
    }
}
