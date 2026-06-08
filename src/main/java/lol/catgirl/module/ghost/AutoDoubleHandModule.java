package lol.catgirl.module.ghost;


import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.utils.client.TickingTimer;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.*;

public final class AutoDoubleHandModule extends Module {
    public static final AutoDoubleHandModule INSTANCE = new AutoDoubleHandModule();

    private final BoolProperty inventorySwitch = new BoolProperty("Inventory Switch", true);
    private final BoolProperty heightSwitch = new BoolProperty("Height Switch", true);
    private final SliderProperty heightThreshold = new SliderProperty("Height Threshold", 1.0f, 10.0f, 3.0f, 0.1f);
    private final BoolProperty healthSwitch = new BoolProperty("Health Switch", true);
    private final SliderProperty healthThreshold = new SliderProperty("Health Threshold", 1.0f, 20.0f, 6.0f, 0.5F);

    private int originalSlot = -1;

    private final TickingTimer groundLevelTimer = new TickingTimer();

    private double cachedGroundLevel = 0;

    private static final long GROUND_LEVEL_CACHE_MS = 500;

    public AutoDoubleHandModule() {
        super("AutoDoubleHand",
                "Automatically switches to totem based on conditions",
                ModuleCategory.Ghost
        );

        addSettings(
                inventorySwitch,
                heightSwitch,
                heightThreshold,
                healthSwitch,
                healthThreshold
        );
    }

    @EventHook
    private void onTick(ClientTickEvent event) {
        if (mc.player == null) {
            return;
        }

        boolean needsTotem = shouldHoldTotem();
        boolean hasTotem = isHoldingTotem();

        if (needsTotem && !hasTotem) {
            switchToTotem();
        } else if (!needsTotem && hasTotem && originalSlot != -1) {
            switchBack();
        }
    }

    private boolean shouldHoldTotem() {

        if (inventorySwitch.getValue()
                && mc.screen instanceof InventoryScreen) {
            return true;
        }

        if (heightSwitch.getValue()) {
            double playerY = mc.player.getY();
            double groundY = getCachedGroundLevel();

            if ((playerY - groundY) > heightThreshold.getValue()) {
                return true;
            }
        }

        if (healthSwitch.getValue()) {
            if (mc.player.getHealth() <= healthThreshold.getValue()) {
                return true;
            }
        }

        return false;
    }

    private boolean isHoldingTotem() {
        ItemStack heldItem = mc.player.getInventory().getItem(mc.player.getInventory().getSelectedSlot());
        return !heldItem.isEmpty() && heldItem.getItem() == Items.TOTEM_OF_UNDYING;
    }

    private void switchToTotem() {

        int totemSlot = findTotemInHotbar();

        if (totemSlot != -1) {
            originalSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(totemSlot);
        }
    }

    private void switchBack() {
        mc.player.getInventory().setSelectedSlot(originalSlot);
        originalSlot = -1;
    }

    private int findTotemInHotbar() {

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);

            if (!stack.isEmpty()
                    && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }

        return -1;
    }

    private double getCachedGroundLevel() {
        if (groundLevelTimer.hasTimeElapsed(GROUND_LEVEL_CACHE_MS, true)) {
            cachedGroundLevel = calculateGroundLevel();
        }

        return cachedGroundLevel;
    }

    private double calculateGroundLevel() {

        double playerY = mc.player.getY();

        for (int y = (int) playerY; y >= mc.level.getMinY(); y--) {
            if (!mc.level
                    .getBlockState(mc.player.blockPosition().atY(y))
                    .isAir()) {
                return y + 1;
            }
        }

        return mc.level.getMinY();
    }

    @Override
    public void onDisable() {
        if (originalSlot != -1) {
            mc.player.getInventory().setSelectedSlot(originalSlot);
            originalSlot = -1;
        }

        groundLevelTimer.reset();

        super.onDisable();
    }

    @Override
    public void onEnable() {
        groundLevelTimer.reset();
        super.onEnable();
    }
}