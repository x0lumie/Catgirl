package lol.catgirl.utils.player.inventory;

import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class InventoryUtils implements IMinecraft {
    public static void move(int from, int to) {
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, from, 0, ClickType.PICKUP, mc.player);
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, to, 0, ClickType.PICKUP, mc.player);
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, from, 0, ClickType.PICKUP, mc.player);
    }

    public static int getBlockFromHotbar() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }

    private static int previousSlot = -1;

    public static void setInventorySlot(int slot) {
        mc.player.getInventory().setSelectedSlot(slot);
        //   ((lol.karane.mixin.interfaces.ClientPlayerInteractionAccessor)mc.interactionManager).syncSelectedSlot();
    }

    public static int getAxeSlot() {
        Container playerInventory = mc.player.getInventory();

        for (int itemIndex = 0; itemIndex < 9; itemIndex++) {
            if (playerInventory.getItem(itemIndex).getItem() instanceof AxeItem)
                return itemIndex;
        }

        return -1;
    }

    public static boolean selectItemFromHotbar(Item item) {
        return selectItemFromHotbar(i -> i == item);
    }

    public static boolean selectItemFromHotbar(Predicate<Item> item) {
        Inventory inv = mc.player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inv.getItem(i);
            if (!item.test(itemStack.getItem()))
                continue;

            inv.setSelectedSlot(i);
            return true;
        }

        return false;
    }

    public static boolean selectAxe() {
        int itemIndex = getAxeSlot();

        if (itemIndex != -1) {
            mc.player.getInventory().setSelectedSlot(itemIndex);
            return true;
        } else return false;
    }

    public static FindItemResult findItem(Item item) {
        return find(s -> s.is(item));
    }

    public static int findItemInHotbar(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(item)) {
                return i;
            }
        }
        return -1;
    }

    public static void swap(final AbstractContainerMenu screenHandler, final int originalSlot, final int newSlot) {
        mc.gameMode.handleInventoryMouseClick(screenHandler.containerId, originalSlot, newSlot, ClickType.SWAP, mc.player);
    }

    public static FindItemResult find(Predicate<ItemStack> predicate) {
        if(mc.player == null) {
            return new FindItemResult(-1, 0);
        }

        int slot = -1;
        int count = 0;

        for(int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if(predicate.test(stack)) {
                if(slot == -1) slot = i;
                count += stack.getCount();
            }
        }
        return new FindItemResult(slot, count);
    }

    public static List<Slot> filterSlots(final AbstractContainerMenu screenHandler, final Predicate<Slot> filterCondition, final boolean shuffle) {
        final List<Slot> filteredSlots = screenHandler.slots.stream().filter(filterCondition).collect(Collectors.toList());

        if (shuffle)
            Collections.shuffle(filteredSlots);

        return filteredSlots;
    }

    public static void pickup(int i) {
        click(SlotUtils.indexToId(i));
    }

    public static void click(int id) {
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                id, 0, ClickType.PICKUP, mc.player
        );
    }

    public static boolean swap(int slot, boolean swpBack) {
        if(slot == SlotUtils.OFFHAND) return true;
        if(slot < 0 || slot > 8) return false;
        if(swpBack && previousSlot == -1) previousSlot = mc.player.getInventory().getSelectedSlot();
        else if (!swpBack) previousSlot = -1;
        mc.player.getInventory().setSelectedSlot(slot);

        return true;
    }

    public record FindItemResult(int slot, int count) {
        public boolean found() {
            return slot != -1 && count > 0;
        }
    }

    public static boolean isArmor(ItemStack stack) {
        if(stack.getItem() == Items.PLAYER_HEAD || stack.getItem() == Items.PUMPKIN) {
            return false;
        }

        return stack.getComponents().get(DataComponents.EQUIPPABLE) != null;
    }
    public static boolean isInventoryFull() {
        return false;
    }

    public static int calculateEnchantmentLevel(final ItemStack itemStack, final ResourceKey<Enchantment> enchantment) {
        final RegistryAccess drm = mc.level.registryAccess();
        final HolderLookup.RegistryLookup<Enchantment> registryWrapper = drm.lookupOrThrow(Registries.ENCHANTMENT);
        return EnchantmentHelper.getItemEnchantmentLevel(registryWrapper.getOrThrow(enchantment), itemStack);
    }

    public static void drop(AbstractContainerMenu handler, int slot) {
        mc.gameMode.handleInventoryMouseClick(handler.containerId, slot, 1, ClickType.THROW, mc.player);
    }

    public static void shiftClick(final AbstractContainerMenu screenHandler, final int slot, final int mouseButton) {
        mc.gameMode.handleInventoryMouseClick(screenHandler.containerId, slot, mouseButton, ClickType.QUICK_MOVE, mc.player);
    }

    public static boolean isGoodItem(ItemStack stack) {
        Item item = stack.getItem();

        if (item == Items.PLAYER_HEAD || item == Items.PUMPKIN || item == Items.CARVED_PUMPKIN) {
            return false;
        }

        if (item instanceof BlockItem blockItem) {
            return isGoodBlock(blockItem.getBlock());
        }

        return item instanceof EnderpearlItem
                || item instanceof PotionItem
                || item instanceof ShieldItem
                || item instanceof FireChargeItem
                || item.components().has(DataComponents.FOOD);
    }

    public static double getSwordValue(ItemStack stack) {
        if(!(stack.is(ItemTags.SWORDS))) {
            return 0.0;
        }

        double score = PlayerUtils.getStackAttackDamage(stack);

        final int sharpnessLevel = calculateEnchantmentLevel(stack, Enchantments.SHARPNESS) + 1;
        score *= sharpnessLevel;

        score += calculateEnchantmentLevel(stack, Enchantments.FIRE_ASPECT);
        float durabilityRatio = stack.getDamageValue() / (float) stack.getMaxDamage();
        score -= durabilityRatio * 0.1;
        return score;
    }

    public static double getArmorValue(ItemStack stack) {
        if(!isArmor(stack)) {
            return 0.0;
        }

        double score = PlayerUtils.getArmorProtection(stack);
        int protectionLevel = calculateEnchantmentLevel(stack, Enchantments.PROTECTION);
        score *= protectionLevel;

        score += calculateEnchantmentLevel(stack, Enchantments.THORNS);
        score += calculateEnchantmentLevel(stack, Enchantments.UNBREAKING) * 0.5;
        score += calculateEnchantmentLevel(stack, Enchantments.PROJECTILE_PROTECTION) * 0.25;
        float durabilityRatio = stack.getDamageValue() / (float) stack.getMaxDamage();
        score -= durabilityRatio * 0.1;
        return score;
    }

    public static double getToolValue(ItemStack stack) {
        Tool component = stack.get(DataComponents.TOOL);
        if(component == null) return 0;

        double score = component.damagePerBlock();

        int efficencyLevel = calculateEnchantmentLevel(stack, Enchantments.EFFICIENCY);
        score *= efficencyLevel;
        score += calculateEnchantmentLevel(stack, Enchantments.UNBREAKING);
        float durabilityRatio = stack.getDamageValue() / (float) stack.getMaxDamage();
        score -= durabilityRatio * 0.1;
        return score;
    }


    public static boolean isGoodBlock(final Block block) {
        return !isBlockInteractable(block)
                && block.defaultBlockState().getShape(EmptyBlockGetter.INSTANCE, mc.player.blockPosition(), CollisionContext.of(mc.player)) == Shapes.block()
                && !(block instanceof TntBlock)
                && !(block instanceof FallingBlock);
    }

    public static boolean isBlockInteractable(final Block block) {
        return interactableBlocks.contains(block);
    }

    private static final List<Block> interactableBlocks = BuiltInRegistries.BLOCK.stream()
            .filter(block ->
                    block instanceof TrapDoorBlock ||
                            block instanceof SweetBerryBushBlock ||
                            block instanceof AbstractFurnaceBlock ||
                            block instanceof SignBlock ||
                            block instanceof AnvilBlock ||
                            block instanceof BarrelBlock ||
                            block instanceof BeaconBlock ||
                            block instanceof BedBlock ||
                            block instanceof BellBlock ||
                            block instanceof BrewingStandBlock ||
                            block instanceof ButtonBlock ||
                            block instanceof CakeBlock ||
                            block instanceof CandleCakeBlock ||
                            block instanceof CartographyTableBlock ||
                            block instanceof CaveVinesPlantBlock ||
                            block instanceof CaveVinesBlock ||
                            block instanceof ChestBlock ||
                            block instanceof ChiseledBookShelfBlock ||
                            block instanceof CommandBlock ||
                            block instanceof ComparatorBlock ||
                            block instanceof ComposterBlock ||
                            block instanceof CraftingTableBlock ||
                            block instanceof DaylightDetectorBlock ||
                            block instanceof DecoratedPotBlock ||
                            block instanceof DispenserBlock ||
                            block instanceof DoorBlock ||
                            block instanceof DragonEggBlock ||
                            block instanceof EnchantingTableBlock ||
                            block instanceof EnderChestBlock ||
                            block instanceof FenceBlock ||
                            block instanceof FenceGateBlock ||
//                            block instanceof TableBloc ||
                            block instanceof FlowerPotBlock ||
                            block instanceof GrindstoneBlock ||
                            block instanceof HopperBlock ||
                            block instanceof JigsawBlock ||
                            block instanceof JukeboxBlock ||
                            block instanceof LecternBlock ||
                            block instanceof LeverBlock ||
                            block instanceof LightBlock ||
                            block instanceof LoomBlock ||
                            block instanceof NoteBlock ||
                            block instanceof MovingPistonBlock ||
                            block instanceof RedStoneWireBlock ||
                            block instanceof RepeaterBlock ||
                            block instanceof RespawnAnchorBlock ||
                            block instanceof ShulkerBoxBlock ||
                            block instanceof SmithingTableBlock ||
                            block instanceof StonecutterBlock ||
                            block instanceof FlowerBlock ||
                            block instanceof StructureBlock ||
                            block instanceof SlimeBlock ||
                            block instanceof WebBlock)
            .toList();

    public static int findSplashPotion(MobEffect type, int duration, int amplifier) {
        Inventory inv = mc.player.getInventory();
        MobEffectInstance potion = new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(type), duration, amplifier);

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inv.getItem(i);

            if (!(itemStack.getItem() instanceof SplashPotionItem))
                continue;

            //String s = PotionUtil.getPotion(itemStack).getEffects().toString();
            String s = itemStack.get(DataComponents.POTION_CONTENTS).getAllEffects().toString();
            if (s.contains(potion.toString())) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isSplashPotion(MobEffect type, int duration, int amplifier, ItemStack itemStack) {
        MobEffectInstance potion = new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(type), duration, amplifier);

        return itemStack.getItem() instanceof SplashPotionItem &&
                itemStack.get(DataComponents.POTION_CONTENTS).getAllEffects().toString().contains(potion.toString());
    }

    public static boolean findAndSelectItem(Class<? extends Item> itemClass) {
        int slot = findItemInHotbarClass(itemClass);
        if (slot != -1) {
            mc.player.getInventory().setSelectedSlot(slot);
            return true;
        }
        return false;
    }

    public static int findItemInHotbarClass(Class<? extends Item> itemClass) {
        if (mc.player == null) return -1;

        Container inventory = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    public static int getGappleSlot() {
        if (mc.player == null) return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(Items.GOLDEN_APPLE)) {
                return i;
            }
        }
        return -1;
    }

    public static void swapToOffhand(int slot) {
        if (slot < 0) return;

        if (slot < 9) {
            slot += 36;
        }

        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                slot, 0, ClickType.PICKUP, mc.player
        );

        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                45, 0, ClickType.PICKUP, mc.player
        );

        if (!mc.player.getOffhandItem().isEmpty() && mc.player.getOffhandItem().getItem() != Items.GOLDEN_APPLE) {

            mc.gameMode.handleInventoryMouseClick(
                    mc.player.containerMenu.containerId,
                    slot, 0, ClickType.PICKUP, mc.player
            );
        }
    }

    public static int getItemCount(Item item) {
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean hasItemInInventory(Item item) {
        return getItemCount(item) > 0;
    }

    public static int findLargestStack(Item item) {
        int bestSlot = -1;
        int largestStack = 0;

        for (int i = 1; i < mc.player.containerMenu.slots.size(); i++) {
            ItemStack stack = mc.player.containerMenu.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getItem() == item && stack.getCount() > largestStack) {
                largestStack = stack.getCount();
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    public static void clearCraftingGrid() {
        for (int i = 1; i <= 9; i++) {
            ItemStack stack = mc.player.containerMenu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
            }
        }
    }

    public static void placeItemInSlot(Item item, int craftingSlot) {
        int sourceSlot = findLargestStack(item);
        if (sourceSlot != -1) {
            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, sourceSlot, 1, ClickType.PICKUP, mc.player);
            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, craftingSlot, 1, ClickType.PICKUP, mc.player);
        }
    }
}
