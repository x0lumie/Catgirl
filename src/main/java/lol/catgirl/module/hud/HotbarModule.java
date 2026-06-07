package lol.catgirl.module.hud;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.utils.client.Animation;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.client.Easing;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public final class HotbarModule extends Module {
    public static final HotbarModule INSTANCE = new HotbarModule();

    private final Animation selectionAnimation = new Animation(Easing.DECELERATE, 200L);
    private float targetX = -1f;
    private float currentX = -1f;

    public HotbarModule() {
        super("Hotbar", "Allows you to customize the hotbar.", ModuleCategory.Hud);
    }

    @EventHook
    public void onRenderTick(RenderTickEvent event) {
        if (mc.player == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float barWidth = 182f;
        float barHeight = 22f;
        float x = (screenWidth / 2f) - (barWidth / 2f);
        float y = screenHeight - barHeight - 3f;

        Color mainColor = new Color(0, 0, 0, 250);
        float radius = 8f;

        float xpBarHeight = 1.5f;
        float xpBarY = y - xpBarHeight - 1f;
        float xpProgress = mc.player.experienceProgress;
        int xpLevel = mc.player.experienceLevel;

        DrawUtil.begin();

        DrawUtil.roundedRect(x, y, x + barWidth, y + barHeight, radius, mainColor);

        if (xpLevel > 0 || xpProgress > 0) {
            Color xpBgColor = new Color(0, 0, 0, 150);
            Color xpProgressColor = ColorUtils.getClientTheme();

            DrawUtil.roundedRect(x, xpBarY, x + barWidth, xpBarY + xpBarHeight, 1f, xpBgColor);

            if (xpProgress > 0) {
                float progressWidth = barWidth * xpProgress;
                DrawUtil.roundedRect(x, xpBarY, x + progressWidth, xpBarY + xpBarHeight, 1f, xpProgressColor);
            }
        }

        int activeSlot = mc.player.getInventory().getSelectedSlot();
        float slotTargetX = x + 1f + (activeSlot * 20f);
        float slotWidth = 20f;
        float slotHeight = 20f;
        float slotY = y + 1f;

        if (targetX == -1f) {
            targetX = slotTargetX;
            currentX = slotTargetX;
        }

        if (slotTargetX != targetX) {
            targetX = slotTargetX;
        }

        currentX += (targetX - currentX) * 0.28f * (event.partialTicks * 2f);
        if (Math.abs(targetX - currentX) < 0.05f) {
            currentX = targetX;
        }

//        Color accentColor = new Color(120, 60, 160, 140);
        DrawUtil.roundedRect(currentX, slotY,
                currentX + slotWidth,
                slotY + slotHeight, radius - 1f, ColorUtils.getClientTheme());

        DrawUtil.end();

        for (int slot = 0; slot < 9; slot++) {
            ItemStack itemStack = mc.player.getInventory().getItem(slot);

            int itemX = (int) (x + 3) + slot * 20;
            int itemY = (int) (y + 3);

            if (!itemStack.isEmpty()) {
                event.context.renderItem(mc.player, itemStack, itemX, itemY, slot);
                event.context.renderItemDecorations(mc.font, itemStack, itemX, itemY);
            }
        }

        if (xpLevel > 0) {
            String levelStr = String.valueOf(xpLevel);

            int textX = (screenWidth / 2) - (mc.font.width(levelStr) / 2);
            int textY = (int) xpBarY - 10;

            event.context.drawString(mc.font, levelStr, textX + 1, textY + 1, 0, false); // Black shadow
            event.context.drawString(mc.font, levelStr, textX, textY, 0x00FF3C, false); // Green text
        }
    }
}