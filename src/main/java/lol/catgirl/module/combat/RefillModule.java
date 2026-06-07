package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.SilentScreen;
import lol.catgirl.utils.player.inventory.InventoryUtils;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public final class RefillModule extends Module {
    public static final RefillModule INSTANCE = new RefillModule();

    public enum Mode {
        Auto, Legit
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Legit);
    public final BoolProperty silentScreen = new BoolProperty("Silent Screen", false)
            .hide(() -> mode.getValue() != Mode.Legit);
    public final SliderProperty delay = new SliderProperty("Delay", 200, 0, 1000, 25);

    public RefillModule() {
        super("Refill", "Automatically fills hotbar with buff splash potions.", ModuleCategory.Combat);
        addSettings(mode, silentScreen, delay);
    }

    private long lastSwap = 0;
    private InventoryScreen openedScreen = null;

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null || !isEnabled()) return;
        if (System.currentTimeMillis() - lastSwap < delay.getValue().longValue()) return;

        switch (mode.getValue()) {
            case Auto -> tickAuto();
            case Legit -> tickLegit();
        }
    }

    private void tickAuto() {
        int hotbarTarget = findEmptyHotbarSlot();
        if (hotbarTarget == -1) return;

        int invSlot = findPotionInInventory();
        if (invSlot == -1) return;

        // InventoryUtils.move expects container slot indices.
        // Inventory slots 9-35 map to container slots 9-35.
        // Hotbar slots 0-8 map to container slots 36-44.
        InventoryUtils.move(invSlot + 27, 36 + hotbarTarget);
        lastSwap = System.currentTimeMillis();
    }

    // Legit: open inventory (optionally silent) and shift-click potions into hotbar one per interval
    private void tickLegit() {
        if (mc.screen instanceof InventoryScreen && openedScreen != null && mc.screen == openedScreen && hasPotionInHotbar()) {
            mc.player.closeContainer();
            openedScreen = null;
            return;
        }

        if (!(mc.screen instanceof InventoryScreen)) {
            if (hasPotionInHotbar()) return;

            int hotbarTarget = findEmptyHotbarSlot();
            if (hotbarTarget == -1) return;

            int invSlot = findPotionInInventory();
            if (invSlot == -1) return;

            if (openedScreen != null) return; // already waiting for our screen

            InventoryScreen screen = new InventoryScreen(mc.player);
            openedScreen = screen;
            if (silentScreen.getValue()) {
                mc.setScreen(new SilentScreen(screen));
            } else {
                mc.setScreen(screen);
            }
            return;
        }

        // Only act if this is our screen
        if (mc.screen != openedScreen && !(mc.screen instanceof SilentScreen)) return;

        int hotbarTarget = findEmptyHotbarSlot();
        if (hotbarTarget == -1) {
            mc.player.closeContainer();
            openedScreen = null;
            return;
        }

        int invSlot = findPotionInInventory();
        if (invSlot == -1) {
            mc.player.closeContainer();
            openedScreen = null;
            return;
        }

        mc.gameMode.handleInventoryMouseClick(
                mc.player.inventoryMenu.containerId,
                invSlot,
                0,
                net.minecraft.world.inventory.ClickType.QUICK_MOVE,
                mc.player
        );
        lastSwap = System.currentTimeMillis();
    }

    private boolean hasPotionInHotbar() {
        for (int i = 0; i < 9; i++) {
            if (isBuff(mc.player.getInventory().getItem(i))) return true;
        }
        return false;
    }

    /**
     * Returns the first hotbar slot index (0-8) that has no buff potion, or -1 if all filled.
     */
    private int findEmptyHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (!isBuff(mc.player.getInventory().getItem(i))) return i;
        }
        return -1;
    }

    /**
     * Returns the raw inventory slot (9-35) of the first buff potion found in the main inventory,
     * or -1 if none exists.
     */
    private int findPotionInInventory() {
        for (int i = 9; i < 36; i++) {
            if (isBuff(mc.player.getInventory().getItem(i))) return i;
        }
        return -1;
    }

    private boolean isBuff(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.is(Items.SPLASH_POTION)) return false;

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return false;

        var potion = contents.potion();
        if (potion.isPresent()) {
            var p = potion.get();
            if (p == Potions.HEALING
                    || p == Potions.STRONG_HEALING
                    || p == Potions.REGENERATION
                    || p == Potions.STRONG_REGENERATION
                    || p == Potions.LONG_REGENERATION
                    || p == Potions.SWIFTNESS
                    || p == Potions.STRONG_SWIFTNESS
                    || p == Potions.LONG_SWIFTNESS
                    || p == Potions.STRENGTH
                    || p == Potions.STRONG_STRENGTH
                    || p == Potions.LONG_STRENGTH
                    || p == Potions.FIRE_RESISTANCE
                    || p == Potions.LONG_FIRE_RESISTANCE) {
                return true;
            }
        }

        for (var effect : contents.getAllEffects()) {
            var type = effect.getEffect();
            if (type == MobEffects.HEALTH_BOOST
                    || type == MobEffects.REGENERATION
                    || type == MobEffects.SPEED
                    || type == MobEffects.STRENGTH
                    || type == MobEffects.FIRE_RESISTANCE
                    || type == MobEffects.ABSORPTION
                    || type == MobEffects.SATURATION) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected String getFinalSuffix() {
        return String.valueOf(delay.getValue().intValue());
    }

    @Override
    public void onDisable() {
        if (openedScreen != null) {
            if (mc.screen instanceof InventoryScreen || mc.screen instanceof SilentScreen) mc.player.closeContainer();
            openedScreen = null;
        }
        super.onDisable();
    }
}