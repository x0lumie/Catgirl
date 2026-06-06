package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.client.SilentScreen;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.awt.*;
import java.util.Random;
import lol.catgirl.module.Module;

public final class ChestStealerModule extends Module {

    public static final ChestStealerModule INSTANCE = new ChestStealerModule();

    public ChestStealerModule() {
        super("ChestStealer",
                "Automatically steals items inside chests.",
                ModuleCategory.Player
        );
        addSettings(silentScreen, minDelay, maxDelay, autoClose);
    }

    public final SliderProperty minDelay = new SliderProperty("Min delay", 25, 0f, 500f, 1);
    public final SliderProperty maxDelay = new SliderProperty("Max delay", 75, 0f, 500f, 1);
    public final BoolProperty autoClose = new BoolProperty("Auto close", true);
    public final BoolProperty silentScreen = new BoolProperty("Silent Screen", false);

    private final Random random = new Random();
    private long nextClickTime;

    private int totalItems = 0;
    private int remainingItems = 0;
    private boolean tracking = false;
    private float animProgress = 0f;

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null || mc.gameMode == null) {
            resetStealer();
            return;
        }

        if (silentScreen.getValue()
                && mc.screen instanceof ContainerScreen
                && !(mc.screen instanceof SilentScreen)) {
            mc.setScreen(new SilentScreen(mc.screen));
            return;
        }

        Screen currentScreen = mc.screen;
        if (currentScreen instanceof SilentScreen silent) {
            currentScreen = silent.getWrapped();
        }

        if (!(currentScreen instanceof ContainerScreen containerScreen)) {
            resetStealer();
            return;
        }

        AbstractContainerMenu menu = containerScreen.getMenu();
        int containerSlotsSize = menu.slots.size() - 36;

        if (!tracking) {
            totalItems = 0;
            for (int i = 0; i < containerSlotsSize; i++) {
                if (menu.slots.get(i).hasItem()) {
                    totalItems++;
                }
            }
            remainingItems = totalItems;
            tracking = true;
        }

        remainingItems = 0;
        for (int i = 0; i < containerSlotsSize; i++) {
            if (menu.slots.get(i).hasItem()) {
                remainingItems++;
            }
        }

        long now = System.currentTimeMillis();
        if (now < nextClickTime) {
            return;
        }

        for (int i = 0; i < containerSlotsSize; i++) {
            Slot slot = menu.slots.get(i);
            if (!slot.hasItem()) {
                continue;
            }

            mc.gameMode.handleInventoryMouseClick(
                    menu.containerId,
                    slot.index,
                    0,
                    ClickType.QUICK_MOVE,
                    mc.player
            );

            nextClickTime = now + getDelay();
            return;
        }

        if (autoClose.getValue()) {
            mc.player.closeContainer();
            mc.setScreen(null);
            resetStealer();
        }
    }

    private void resetStealer() {
        tracking = false;
        totalItems = 0;
        remainingItems = 0;
    }

    private long getDelay() {
        float min = minDelay.getValue();
        float max = maxDelay.getValue();

        if (min > max) {
            float tmp = min;
            min = max;
            max = tmp;
        }

        return (long) (min + random.nextFloat() * (max - min));
    }

    @EventHook
    public void onRender(RenderTickEvent event) {
        if (!silentScreen.getValue()) {
            animProgress = 0;
            return;
        }

        float progress = totalItems <= 0 ? 0f : (float) (totalItems - remainingItems) / totalItems;

        float speed = 0.08f;

        if (animProgress < progress) {
            animProgress = Math.min(progress, animProgress + speed);
        } else if (animProgress > progress) {
            animProgress = Math.max(progress, animProgress - speed);
        }

        if (Math.abs(progress - animProgress) < 0.01f) {
            animProgress = progress;
        }

        if (progress <= 0f && animProgress <= 0f) {
            animProgress = 0f;
            return;
        }

        DrawUtil.begin();

        String text = (int) (animProgress * 100) + "%";
        float textSize = 10f;
        float paddingY = 7f;
        float radius = 8f;

        float screenW = mc.getWindow().getGuiScaledWidth();
        float screenH = mc.getWindow().getGuiScaledHeight();

        float barWidth = 125f;
        float barHeight = DrawUtil.getFontHeight(textSize) + paddingY;

        float x = (screenW - barWidth) / 2f;
        float y = screenH - 70f;

        float fillW = barWidth * animProgress;

        float textWidth = (float) DrawUtil.getStringWidth(text, textSize);
        float textHeight = DrawUtil.getFontHeight(textSize);

        float textX = x + (barWidth / 2f) - (textWidth / 2f);
        float textY = y + (barHeight / 2f) + (textHeight / 2f) - 2f;

        Color bg = new Color(25, 25, 25, 170);
        Color border = new Color(70, 70, 70, 180);

        DrawUtil.roundedRect(x, y, x + barWidth, y + barHeight, radius, bg);
        DrawUtil.drawOutline(x, y, barWidth, barHeight, radius, 1.0f, border);

        if (animProgress > 0.001f) {
            DrawUtil.roundedRect(
                    x, y,
                    x + fillW, y + barHeight,
                    radius,
                    ColorUtils.getClientTheme()
            );
        }

        DrawUtil.drawString(text, textX, textY, textSize, new Color(255, 255, 255, 255));

        DrawUtil.end();
    }
}