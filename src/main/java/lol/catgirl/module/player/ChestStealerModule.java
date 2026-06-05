package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.BoolProperty;
import lol.catgirl.setting.impl.SliderProperty;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.Random;
import lol.catgirl.module.Module;

public final class ChestStealerModule extends Module {

    public static final ChestStealerModule INSTANCE = new ChestStealerModule();

    public ChestStealerModule() {
        super("ChestStealer",
                "Automatically steals items inside chests.",
                ModuleCategory.Player
        );
        addSettings(minDelay, maxDelay, autoClose);
    }

    public final SliderProperty minDelay = new SliderProperty("Min delay", 25, 0f, 500f, 1);
    public final SliderProperty maxDelay = new SliderProperty("Max delay", 75, 0f, 500f, 1);
    public final BoolProperty autoClose = new BoolProperty("Auto close", true);

    private final Random random = new Random();
    private long nextClickTime;

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null || mc.gameMode == null) return;

        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;

        AbstractContainerMenu menu = screen.getMenu();

        if (!(screen instanceof ContainerScreen)) return;

        long now = System.currentTimeMillis();
        if (now < nextClickTime) return;

        for (Slot slot : menu.slots) {
            if (slot.index >= menu.slots.size() - 36) {
                continue;
            }

            if (!slot.hasItem()) continue;

            mc.gameMode.handleInventoryMouseClick(
                    menu.containerId,
                    slot.index, 0, ClickType.QUICK_MOVE, mc.player
            );

            nextClickTime = now + getDelay();
            return;
        }

        if (autoClose.getValue()) {
            mc.player.closeContainer();
        }
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
}
