package lol.catgirl.ui.notification;

import lol.catgirl.utils.client.Animation;
import lol.catgirl.utils.client.Easing;
import lol.catgirl.utils.client.TickingTimer;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.awt.*;

@Getter
public class Notification {

    public enum Type {
        INFO(new Color(64, 131, 214), Identifier.fromNamespaceAndPath("catgirl", "images/info.png")),
        NOTIFY(new Color(242, 206, 87), Identifier.fromNamespaceAndPath("catgirl", "images/notify.png")),
        WARNING(new Color(226, 74, 74), Identifier.fromNamespaceAndPath("catgirl", "images/warning.png")),
        OKAY(new Color(65, 252, 65), Identifier.fromNamespaceAndPath("catgirl", "images/okay.png"));

        @Getter private final Color defaultColor;
        @Getter private final Identifier iconLocation;

        Type(Color defaultColor, Identifier iconLocation) {
            this.defaultColor = defaultColor;
            this.iconLocation = iconLocation;
        }
    }

    private final String title;
    private final String message;
    private final long time;
    private final Color color;
    private final Type type;

    private final TickingTimer timer = new TickingTimer();
    private final Animation animation = new Animation(Easing.EASE_OUT_BACK, 250);

    public Notification(String title, String message, float seconds, Color color) {
        this.title = title;
        this.message = message;
        this.time = (long) (seconds * 1000L);
        this.color = color;
        this.type = Type.INFO;
        timer.reset();
    }

    public Notification(String title, String message, float seconds, Type type) {
        this.title = title;
        this.message = message;
        this.time = (long) (seconds * 1000L);
        this.type = type;
        this.color = type.getDefaultColor();
        timer.reset();
    }

    public boolean isExpired() {
        return timer.hasTimeElapsed(time, false);
    }

    public float getProgress() {
        return Math.min(1F, (float) timer.getTime() / time);
    }

    public void drawExhibition(GuiGraphics guiGraphics, float x, float y) {
        float titleWidth = (float) DrawUtil.getStringWidth(
                title,
                11f,
                ResourceManager.FontResources.productSansBold
        );

        float messageWidth = (float) DrawUtil.getStringWidth(
                message,
                9f,
                ResourceManager.FontResources.regular
        );

        float contentWidth = Math.max(titleWidth, messageWidth);

        float width = contentWidth + 26F;
        float height = 23F;

        animation.run(isExpired() ? 0F : 1F);
        float anim = animation.getValue();

        float animatedX = x - (width * anim);

        int alpha = Math.max(0, Math.min(255, (int) (anim * 255F)));
        int bgAlpha = Math.max(0, Math.min(255, (int) (anim * 200F)));

        DrawUtil.roundedRect(
                animatedX,
                y,
                animatedX + width,
                y + height,
                0F,
                new Color(0, 0, 0, bgAlpha)
        );

        int iconSize = 16;
        int iconX = (int) (animatedX + 4F);
        int iconY = (int) (y + 4F);

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED, type.getIconLocation(),
                iconX, iconY, 0F, 0F, iconSize, iconSize,
                iconSize,
                iconSize
        );

        DrawUtil.drawString(
                title, animatedX + 24F, y + 10F, 10F,
                new Color(255, 255, 255, alpha),
                ResourceManager.FontResources.productSansBold
        );

        DrawUtil.drawString(
                message, animatedX + 24F, y + 19F,
                9F, new Color(200, 200, 200, alpha),
                ResourceManager.FontResources.regular
        );

        float progressPct = getProgress();
        float fullWidth = width + 1F;
        float progressX = fullWidth * progressPct;

        Color barColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

        int dimAlpha = Math.max(0, Math.min(255, (int) (alpha * 0.45F)));
        Color dimBarColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                dimAlpha
        );

        DrawUtil.roundedRect(
                animatedX - 1F,
                y + 21.5F,
                animatedX + width,
                y + height,
                0F,
                dimBarColor
        );

        DrawUtil.roundedRect(
                animatedX + progressX,
                y + 21.5F,
                animatedX + width,
                y + height,
                0F,
                barColor
        );
    }
}