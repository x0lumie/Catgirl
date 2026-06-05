package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.combat.AuraModule;
import lol.catgirl.setting.impl.SliderProperty;
import lol.catgirl.utils.player.PlayerUtils;
import lol.catgirl.utils.player.inventory.InventoryUtils;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.Comparator;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public class AutoArmorModule extends Module {
    public static final AutoArmorModule INSTANCE = new AutoArmorModule();

    public final SliderProperty delay = new SliderProperty("Delay", 75, 0f, 500f, 1);

    public AutoArmorModule() {
        super("AutoArmor",
                "Automatically equips armor.",
                ModuleCategory.Player
        );
        addSetting(delay);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null) return;
        if(!this.isEnabled()) return;

        if (!(mc.screen instanceof InventoryScreen) &&
                !InventoryMoveModule.INSTANCE.isEnabled()) {
            return;
        }

        var aura = AuraModule.INSTANCE;
        if (aura.isEnabled() && AuraModule.target != null) return;

        AbstractContainerMenu handler = mc.player.containerMenu;
        if (!(handler instanceof InventoryMenu playerHandler)) return;

        var manager = InventoryManagerModule.INSTANCE;

        if (!manager.canMove(delay.getValue().longValue())) return;

        for (EquipmentSlot type : EquipmentSlot.values()) {

            Slot bestSlot = playerHandler.slots.stream()
                    .filter(s -> isArmorForSlot(s, type))
                    .max(Comparator.comparingDouble(s ->
                            getArmorValue(s.getItem())))
                    .orElse(null);

            if (bestSlot == null) continue;

            ItemStack equipped = mc.player.getItemBySlot(type);
            double equippedValue = getArmorValue(equipped);
            double bestValue = getArmorValue(bestSlot.getItem());

            if (equipped.isEmpty() || bestValue > equippedValue) {
                InventoryUtils.shiftClick(playerHandler, bestSlot.index, 0);
                manager.timer.reset();
                return;
            }
        }
    }

    private boolean isArmorForSlot(Slot slot, EquipmentSlot type) {
        final var stack = slot.getItem();

        if (stack.isEmpty() || !InventoryUtils.isArmor(stack)) return false;

        final var eq = stack.getComponents().get(DataComponents.EQUIPPABLE);
        return eq != null && eq.slot() == type;
    }

    public double getArmorValue(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !InventoryUtils.isArmor(stack)) {
            return 0;
        }

        double base = PlayerUtils.getArmorProtection(stack);

        int prot = InventoryUtils.calculateEnchantmentLevel(stack, Enchantments.PROTECTION);
        int unbreaking = InventoryUtils.calculateEnchantmentLevel(stack, Enchantments.UNBREAKING);
        int thorns = InventoryUtils.calculateEnchantmentLevel(stack, Enchantments.THORNS);
        int proj = InventoryUtils.calculateEnchantmentLevel(stack, Enchantments.PROJECTILE_PROTECTION);

        double score = base;

        score += prot * 0.75;
        score += proj * 0.25;
        score += unbreaking * 0.1;
        score += thorns * 0.2;

        float durability = stack.getDamageValue() / (float) stack.getMaxDamage();
        score -= durability * 0.2;

        return score;
    }
}
