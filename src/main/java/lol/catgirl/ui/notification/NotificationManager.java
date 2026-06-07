package lol.catgirl.ui.notification;

import lol.catgirl.module.client.NotificationsModule;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager implements IMinecraft {

    @Getter @Setter
    private static float defaultTime = 2F;

    @Getter
    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public static void post(String title, String message, Color color) {
        notifications.add(new Notification(title, message, defaultTime, color));
    }

    public static void post(String title, String message, float time, Color color) {
        notifications.add(new Notification(title, message, time, color));
    }

    public static void post(String title, String message, Notification.Type type) {
        notifications.add(new Notification(title, message, defaultTime, type));
    }

    public static void post(String title, String message, float time, Notification.Type type) {
        notifications.add(new Notification(title, message, time, type));
    }

    public static void render(GuiGraphics guiGraphics) {
        if (notifications.isEmpty()) return;

        DrawUtil.begin();

        notifications.removeIf(n -> n.isExpired() && n.getAnimation().getValue() <= 0.01F);

        NotificationsModule.Mode currentMode = NotificationsModule.INSTANCE.mode.getValue();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (currentMode == NotificationsModule.Mode.Exhibition) {
            boolean isChatOpen = mc.screen instanceof ChatScreen;
            float y = screenHeight - (notifications.size() * 24F) - (isChatOpen ? 14F : 0F);
            float x = screenWidth - 0F;

            for (Notification notification : notifications) {
                notification.drawExhibition(guiGraphics, x, y);
                y += 24F;
            }
        }

        DrawUtil.end();
    }
}