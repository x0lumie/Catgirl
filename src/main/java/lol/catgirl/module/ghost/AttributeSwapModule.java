package lol.catgirl.module.ghost;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.KeyEvent;
import lol.catgirl.event.impl.PlayerAttackPreEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.lwjgl.glfw.GLFW;

public final class AttributeSwapModule extends Module {
    public static final AttributeSwapModule INSTANCE = new AttributeSwapModule();

    private int lastSlot = -1;
    private boolean swapped = false;
    public static boolean swapping = false;
    private int ticks = 0;

    public enum Variant {
        DENSITY, BREACH
    }

    private boolean toggleRequested = false;
    public static boolean extraClickQueued = false;
    private Entity target;

    public final EnumProperty<Variant> preferred = new EnumProperty<Variant>("Preferred", Variant.DENSITY);
    private final SliderProperty swapTick = new SliderProperty("Swap Tick", 2, 1, 5, 1);
    private final BoolProperty onlyWeapons = new BoolProperty("Only Weapons", true);

    public AttributeSwapModule() {
        super("AttributeSwap",
                "Swaps to mace based on weapon and chosen mace variant",
                ModuleCategory.Ghost
        );
        addSettings(preferred, swapTick, onlyWeapons);
    }

    @Override
    public void onEnable() {
        Catgirl.sendChatMessage("To toggle this press the 'G' key.");
    }

    @EventHook
    public void onKey(KeyEvent event) { // TODO: make this changble
        if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == GLFW.GLFW_KEY_G) {
            toggleRequested = true;
        }
    }

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (toggleRequested) {
            toggleRequested = false;
        }

        int tick = extraClickQueued ? 3 : swapTick.getValue().intValue();

        if (swapped && lastSlot != -1) {
            ticks++;
            if (ticks >= tick) {
                extraClickQueued = false;
                reset();
            }
        }
    }

    @EventHook
    public void onAttack(PlayerAttackPreEvent event) {
        target = event.target;
        if (!(target instanceof LivingEntity)) return;

        boolean shouldSwap = !onlyWeapons.getValue() || PlayerUtils.isHoldingWeapon();

        if (shouldSwap) {
            if (ticks == 1) {
                extraClickQueued = true;
            }
                
            swapToPreferredMace();

            if (swapping && swapped) {
                swapping = false;
            }
        }
    }

    private void swapToPreferredMace() {
        int targetSlot = findPreferredMace();
        if (targetSlot == -1) return;
        
        swapping = true;

        if (!swapped) {
            lastSlot = mc.player.getInventory().getSelectedSlot();
        }

        mc.player.getInventory().setSelectedSlot(targetSlot);
        swapped = true;
    }

    private int findPreferredMace() {
        int anyMaceSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() != Items.MACE) continue;
            if (anyMaceSlot == -1) {
                anyMaceSlot = i;
            }
            if (preferred.getValue() == Variant.DENSITY && isDensityMace(stack)) return i;
            if (preferred.getValue() == Variant.BREACH && isBreachMace(stack)) return i;
        }
        return anyMaceSlot;
    }

    private boolean isDensityMace(ItemStack stack) {
        return stack.getItem() == Items.MACE && stack.getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().is(Enchantments.DENSITY));
    }

    private boolean isBreachMace(ItemStack stack) {
        return stack.getItem() == Items.MACE && stack.getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().is(Enchantments.BREACH));
    }

    public void reset() {
        if (swapped) {
            mc.player.getInventory().setSelectedSlot(lastSlot);
            swapped = false;
        }
        lastSlot = -1;
        ticks = 0;
    }

    @Override
    protected String getFinalSuffix() {
        return preferred.getValue().toString();
    }
}