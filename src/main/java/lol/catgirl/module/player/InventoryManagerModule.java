package lol.catgirl.module.player;


import lol.catgirl.Catgirl;
import lol.catgirl.module.combat.AuraModule;
import lol.catgirl.module.combat.AutoTotemModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.TickingTimer;
import lol.catgirl.utils.player.PlayerUtils;
import lol.catgirl.utils.player.inventory.InventoryUtils;
import lombok.Getter;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.Comparator;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.event.impl.*;
import lol.catgirl.event.EventHook;


public class InventoryManagerModule extends Module {
    public static final InventoryManagerModule INSTANCE = new InventoryManagerModule();

    @Getter
    private final SliderProperty swordSlot = new SliderProperty("Sword Slot", 1f, 0f, 9f, 1);
    @Getter
    private final SliderProperty pickaxeSlot = new SliderProperty("Pickaxe Slot", 2f, 0f, 9f, 1);
    @Getter
    private final SliderProperty axeSlot = new SliderProperty("Axe Slot", 3f, 0f, 9f, 1);
    @Getter
    private final SliderProperty blocksSlot = new SliderProperty("Blocks Slot", 4f, 0f, 9f, 1);
    @Getter
    private final SliderProperty gappleSlot = new SliderProperty("Gapple Slot", 5f, 0f, 9f, 1);
    @Getter
    private final SliderProperty bowSlot = new SliderProperty("Bow Slot", 6f, 0f, 9f, 1);
    @Getter
    private final SliderProperty waterBucketSlot = new SliderProperty("Water Bucket Slot", 7f, 0f, 9f, 1);


    @Getter
    private final BoolProperty keepSnowballs = new BoolProperty("Keep Snowballs", true);
    @Getter
    private final BoolProperty keepTNT = new BoolProperty("Keep TNT", true);
    @Getter
    private final BoolProperty keepArrows = new BoolProperty("Keep Arrows", true);
    @Getter
    private final BoolProperty keepFintAndSteal = new BoolProperty("Keep F&S", true);

    @Getter
    private final BoolProperty dropUselessArmor = new BoolProperty("Drop Useless Armor", true);

    @Getter
    private final BoolProperty autoDisable = new BoolProperty("Auto Disable", false);

    @Getter
    private final SliderProperty delay = new SliderProperty("Delay", 25, 0f, 500f, 1);

    public final TickingTimer timer = new TickingTimer();

    public enum Mode {
        Auto,
        OpenInv
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Auto);

    public InventoryManagerModule() {
        super("InventoryManager",
                "Helps you manage your inventory items and slots.",
                ModuleCategory.Player
        );
        addSettings(
                mode,
                swordSlot,
                pickaxeSlot,
                axeSlot,
                blocksSlot,
                gappleSlot,
                bowSlot,
                waterBucketSlot,
                keepSnowballs,
                keepTNT,
                keepArrows,
                keepFintAndSteal,
                dropUselessArmor,
                autoDisable,
                delay
        );
    }

    @EventHook
    public void onWorldChange(WorldJoinEvent event) {
        if(!this.isEnabled()) return;

        if (autoDisable.getValue()) {
            Catgirl.sendChatMessage("InventoryManager has been disabled due to world change.");

            toggle();
        }
    }

    @EventHook
    public void onPreUpdate(ClientTickEvent event) {
        if(!this.isEnabled()) return;

        if (mc.player == null || mc.level == null) return;
        switch (mode.getValue()) {
            case Auto -> {
                // always
            }

            case OpenInv -> {
                if (!(mc.screen instanceof InventoryScreen)) {
                    return;
                }
            }
        }

        AuraModule auraModule = AuraModule.INSTANCE;
        //  Scaffold scaffoldModule = ModuleManager.INSTANCE.getModule(Scaffold.class);
        if ((auraModule.isEnabled() && AuraModule.target != null)) {
            return;
        }

        AbstractContainerMenu screenHandler = mc.player.containerMenu;
        if (!(screenHandler instanceof InventoryMenu playerScreenHandler)) {
            return;
        }

        Slot bestSword = getBestSword(playerScreenHandler);
        Slot perfSwordSlot = screenHandler.getSlot(getSwordSlot().getValue().intValue() + 35);

        Slot bestPickaxe = getBestPickaxe(playerScreenHandler);
        Slot perfPickaxeSlot = screenHandler.getSlot(getPickaxeSlot().getValue().intValue() + 35);

        Slot bestAxe = getBestAxe(playerScreenHandler);
        Slot perfAxeSlot = screenHandler.getSlot(getAxeSlot().getValue().intValue() + 35);

        Slot mostBlocks = getMostBlocks(playerScreenHandler);
        Slot perfBlocksSlot = screenHandler.getSlot(getBlocksSlot().getValue().intValue() + 35);

        Slot bestBow = getBestBow(playerScreenHandler);
        Slot perfBowSlot = screenHandler.getSlot(getBowSlot().getValue().intValue() + 35);

        Slot bestGapple = getBestGapple(playerScreenHandler);
        Slot perfGappleSlot = screenHandler.getSlot(getGappleSlot().getValue().intValue() + 35);

        Slot bestWaterBucket = getWaterBucket(playerScreenHandler);
        Slot perfWaterBucketSlot = screenHandler.getSlot(getWaterBucketSlot().getValue().intValue() + 35);


        InventoryUtils.filterSlots(playerScreenHandler, slot -> !slot.getItem().isEmpty(), true).forEach(validSlot -> {
            if (!canMove(getDelay().getValue().longValue()) || InventoryUtils.isGoodItem(validSlot.getItem())) {
                return;
            }

       //     if (validSlot.getStack().getItem().getComponents().get(DataComponentTypes.EQUIPPABLE) != null) {
       //         return;
       //     }

            arrangeBestSword(screenHandler, perfSwordSlot, bestSword);
            arrangeBestPickaxe(screenHandler, perfPickaxeSlot, bestPickaxe);
            arrangeBestAxe(screenHandler, perfAxeSlot, bestAxe);
            arrangeMostBlocks(screenHandler, perfBlocksSlot, mostBlocks);
            arrangeBestBow(screenHandler, perfBowSlot, bestBow);
            arrangeBestGapple(screenHandler, perfGappleSlot, bestGapple);
            arrangeWaterBucket(screenHandler, perfWaterBucketSlot, bestWaterBucket);

            if (validSlot.getContainerSlot() == perfSwordSlot.getContainerSlot() && validSlot.getItem().is(ItemTags.SWORDS)) {
                return;
            }
            if (validSlot.getContainerSlot() == perfPickaxeSlot.getContainerSlot() && validSlot.getItem().is(ItemTags.PICKAXES)) {
                return;
            }
            if (validSlot.getContainerSlot() == perfAxeSlot.getContainerSlot() && validSlot.getItem().getItem() instanceof AxeItem) {
                return;
            }
            if (validSlot.getItem().getItem() instanceof BucketItem) {
                return;
            }

            if (validSlot.getContainerSlot() == perfBowSlot.getContainerSlot() && validSlot.getItem().getItem() instanceof BowItem) {
                return;
            }

            if (validSlot.getContainerSlot() == perfGappleSlot.getContainerSlot() && validSlot.getItem().getItem() == Items.GOLDEN_APPLE) {
                return;
            }

            ItemStack stack = validSlot.getItem();
            Item item = stack.getItem();

            if (keepSnowballs.getValue() && item == Items.SNOWBALL) {
                return;
            }

            if (keepFintAndSteal.getValue() && item == Items.FLINT_AND_STEEL) {
                return;
            }

            if (item == Items.FISHING_ROD) {
                return;
            }

            if (keepTNT.getValue() && item == Items.TNT) {
                return;
            }

            if (keepArrows.getValue() && item == Items.ARROW) {
                return;
            }

            if (dropUselessArmor.getValue() && isUselessArmor(playerScreenHandler, validSlot)) {
                InventoryUtils.drop(playerScreenHandler, validSlot.index);
                timer.reset();
                return;
            }
        });
    }

    @EventHook
    public void onPacketRecieve(PacketReceivedEvent event) {
        if(!this.isEnabled()) return;

        if (event.packet instanceof ClientboundContainerSetSlotPacket slotUpdate
                && slotUpdate.getItem().getItem() != Items.AIR
                && mc.player != null
                && slotUpdate.getContainerId() == mc.player.inventoryMenu.containerId) {
            timer.reset();
        }
    }


    private void arrangeBestSword(final AbstractContainerMenu screenHandler, final Slot preferredSwordSlot, final Slot bestSwordSlot) {
        if (bestSwordSlot != null && bestSwordSlot.getContainerSlot() != preferredSwordSlot.getContainerSlot()) {
            double bestSwordValue = InventoryUtils.getSwordValue(bestSwordSlot.getItem());
            double preferredSwordValue = InventoryUtils.getSwordValue(preferredSwordSlot.getItem());

            if (bestSwordValue > preferredSwordValue) {
                InventoryUtils.swap(screenHandler, bestSwordSlot.index, preferredSwordSlot.index - 36);
                timer.reset();
            }
        }
    }

    private void arrangeWaterBucket(final AbstractContainerMenu screenHandler, final Slot preferredSlot, final Slot waterBucketSlot) {
        if (waterBucketSlot != null && waterBucketSlot.getContainerSlot() != preferredSlot.getContainerSlot()) {
            InventoryUtils.swap(screenHandler, waterBucketSlot.index, preferredSlot.index - 36);
            timer.reset();
        }
    }

    private Slot getWaterBucket(final AbstractContainerMenu screenHandler) {
        return InventoryUtils.filterSlots(screenHandler,
                slot -> slot.getItem().getItem() == Items.WATER_BUCKET,
                false
        ).stream().findFirst().orElse(null);
    }

    private Slot getBestSword(final AbstractContainerMenu screenHandler) {
        return InventoryUtils.filterSlots(screenHandler, slot -> slot.getItem().is(ItemTags.SWORDS), false)
                .stream()
                .max(Comparator.comparing(swordSlot -> InventoryUtils.getSwordValue(swordSlot.getItem())))
                .orElse(null);
    }

    private void arrangeBestPickaxe(final AbstractContainerMenu screenHandler, final Slot preferredPickaxeSlot, final Slot bestPickaxeSlot) {
        if (bestPickaxeSlot != null && bestPickaxeSlot.getContainerSlot() != preferredPickaxeSlot.getContainerSlot()) {
            double bestPickaxeValue = InventoryUtils.getToolValue(bestPickaxeSlot.getItem());
            double preferredPickaxeValue = InventoryUtils.getToolValue(preferredPickaxeSlot.getItem());

            if (bestPickaxeValue > preferredPickaxeValue) {
                InventoryUtils.swap(screenHandler, bestPickaxeSlot.index, preferredPickaxeSlot.index - 36);
                timer.reset();
            }
        }
    }

    private Slot getBestPickaxe(final AbstractContainerMenu screenHandler) {
        return InventoryUtils.filterSlots(screenHandler, slot -> slot.getItem().is(ItemTags.PICKAXES), false)
                .stream()
                .max(Comparator.comparing(pickaxeSlot -> InventoryUtils.getToolValue(pickaxeSlot.getItem())))
                .orElse(null);
    }

    private void arrangeBestAxe(final AbstractContainerMenu screenHandler, final Slot preferredAxeSlot, final Slot bestAxeSlot) {
        if (bestAxeSlot != null && bestAxeSlot.getContainerSlot() != preferredAxeSlot.getContainerSlot()) {
            double bestAxeValue = InventoryUtils.getToolValue(bestAxeSlot.getItem());
            double preferredAxeValue = InventoryUtils.getToolValue(preferredAxeSlot.getItem());

            if (bestAxeValue > preferredAxeValue) {
                InventoryUtils.swap(screenHandler, bestAxeSlot.index, preferredAxeSlot.index - 36);
                timer.reset();
            }
        }
    }

    private Slot getBestAxe(final AbstractContainerMenu screenHandler) {
        return InventoryUtils.filterSlots(screenHandler, slot -> slot.getItem().getItem() instanceof AxeItem, false)
                .stream()
                .max(Comparator.comparing(axeSlot -> InventoryUtils.getToolValue(axeSlot.getItem())))
                .orElse(null);
    }

    private Slot getMostBlocks(final AbstractContainerMenu screenHandler) {
        return InventoryUtils.filterSlots(screenHandler, slot ->
                                slot.getItem().getItem() instanceof BlockItem blockItem &&
                                        slot.getItem().getCount() > 0 &&
                                        InventoryUtils.isGoodBlock(blockItem.getBlock())
                        , false)
                .stream()
                .max(Comparator.comparing(blockSlot -> blockSlot.getItem().getCount()))
                .orElse(null);
    }

    private void arrangeMostBlocks(final AbstractContainerMenu screenHandler, final Slot preferredBlockSlot, final Slot mostBlockSlot) {
        if (mostBlockSlot != null && mostBlockSlot.getContainerSlot() != preferredBlockSlot.getContainerSlot()) {
            double mostBlockCount = mostBlockSlot.getItem().getCount();
            double preferredBlockValue = preferredBlockSlot.getItem().getCount();

            if (mostBlockCount > preferredBlockValue) {
                InventoryUtils.swap(screenHandler, mostBlockSlot.index, preferredBlockSlot.index - 36);
                timer.reset();
            }
        }
    }

    private Slot getBestBow(final AbstractContainerMenu screenHandler) {
        return InventoryUtils.filterSlots(screenHandler, slot -> slot.getItem().getItem() instanceof BowItem, false)
                .stream()
                .max(Comparator.comparing(slot ->
                        slot.getItem().getOrDefault(DataComponents.ENCHANTMENTS, null) != null ? 1 : 0))
                .orElse(null);
    }

    private void arrangeBestBow(final AbstractContainerMenu screenHandler, final Slot preferredBowSlot, final Slot bestBowSlot) {
        if (bestBowSlot != null && bestBowSlot.getContainerSlot() != preferredBowSlot.getContainerSlot()) {
            InventoryUtils.swap(screenHandler, bestBowSlot.index, preferredBowSlot.index - 36);
            timer.reset();
        }
    }

    private Slot getBestGapple(final AbstractContainerMenu screenHandler) {
        return InventoryUtils.filterSlots(screenHandler,
                        slot -> slot.getItem().getItem() == Items.GOLDEN_APPLE, false)
                .stream()
                .max(Comparator.comparing(slot -> slot.getItem().getCount()))
                .orElse(null);
    }

    private void arrangeBestGapple(final AbstractContainerMenu screenHandler, final Slot preferredGappleSlot, final Slot bestGappleSlot) {
        if (bestGappleSlot != null && bestGappleSlot.getContainerSlot() != preferredGappleSlot.getContainerSlot()) {
            int bestCount = bestGappleSlot.getItem().getCount();
            int preferredCount = preferredGappleSlot.getItem().getCount();
            if (bestCount > preferredCount) {
                InventoryUtils.swap(screenHandler, bestGappleSlot.index, preferredGappleSlot.index - 36);
                timer.reset();
            }
        }
    }

    public boolean canMove(long delay) {
        if (delay == 0) return true;
        return timer.getTime() >= delay;
    }

    private boolean isUselessArmor(AbstractContainerMenu handler, Slot slot) {
        ItemStack stack = slot.getItem();

        if (!InventoryUtils.isArmor(stack)) return false;

        Equippable eq = stack.getComponents().get(DataComponents.EQUIPPABLE);
        if (eq == null) return false;

        EquipmentSlot type = eq.slot();


        double currentValue = getArmorValue(stack);

        var sameTypeArmor = handler.slots.stream()
                .filter(s -> !s.getItem().isEmpty())
                .filter(s -> InventoryUtils.isArmor(s.getItem()))
                .filter(s -> {
                    Equippable comp = s.getItem().getComponents().get(DataComponents.EQUIPPABLE);
                    return comp != null && comp.slot() == type;
                })
                .sorted((a, b) -> Double.compare(
                        getArmorValue(b.getItem()),
                        getArmorValue(a.getItem())
                ))
                .toList();

        if (sameTypeArmor.isEmpty()) return false;

        Slot best = sameTypeArmor.get(0);
        double bestValue = getArmorValue(best.getItem());

        if (currentValue < bestValue) return true;

        if (Math.abs(currentValue - bestValue) < 0.01 && best.index != slot.index) return true;

        return false;
    }

    public double getArmorValue(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !InventoryUtils.isArmor(stack)) {
            return 0;
        }

        double base = PlayerUtils.getArmorProtection(stack);

        int prot = InventoryUtils.calculateEnchantmentLevel(stack,
                net.minecraft.world.item.enchantment.Enchantments.PROTECTION);
        int unbreaking = InventoryUtils.calculateEnchantmentLevel(stack,
                net.minecraft.world.item.enchantment.Enchantments.UNBREAKING);
        int thorns = InventoryUtils.calculateEnchantmentLevel(stack,
                net.minecraft.world.item.enchantment.Enchantments.THORNS);
        int proj = InventoryUtils.calculateEnchantmentLevel(stack,
                net.minecraft.world.item.enchantment.Enchantments.PROJECTILE_PROTECTION);

        double score = base;

        score += prot * 0.75;
        score += proj * 0.25;
        score += unbreaking * 0.1;
        score += thorns * 0.2;

        float durability = stack.getDamageValue() / (float) stack.getMaxDamage();
        score -= durability * 0.2;

        return score;
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
