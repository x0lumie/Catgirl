package lol.catgirl.module.player;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.event.EventHook;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.TickingTimer;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;

public final class AutoCrafterModule extends Module {
    public static final AutoCrafterModule INSTANCE = new AutoCrafterModule();

    private static final List<Recipe> RECIPES = Arrays.asList(
            new Recipe(Items.DIAMOND_SWORD, Items.DIAMOND, 2, Items.STICK, 1,
                    new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}),
            new Recipe(Items.DIAMOND_PICKAXE, Items.DIAMOND, 3, Items.STICK, 2,
                    new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}),
            new Recipe(Items.DIAMOND_AXE, Items.DIAMOND, 3, Items.STICK, 2,
                    new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}),
            new Recipe(Items.DIAMOND_SHOVEL, Items.DIAMOND, 1, Items.STICK, 2,
                    new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}),

            new Recipe(Items.IRON_SWORD, Items.IRON_INGOT, 2, Items.STICK, 1,
                    new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}),
            new Recipe(Items.IRON_PICKAXE, Items.IRON_INGOT, 3, Items.STICK, 2,
                    new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}),
            new Recipe(Items.IRON_AXE, Items.IRON_INGOT, 3, Items.STICK, 2,
                    new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}),
            new Recipe(Items.IRON_SHOVEL, Items.IRON_INGOT, 1, Items.STICK, 2,
                    new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}),

            new Recipe(Items.GOLDEN_SWORD, Items.GOLD_INGOT, 2, Items.STICK, 1,
                    new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}),
            new Recipe(Items.GOLDEN_PICKAXE, Items.GOLD_INGOT, 3, Items.STICK, 2,
                    new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}),
            new Recipe(Items.GOLDEN_AXE, Items.GOLD_INGOT, 3, Items.STICK, 2,
                    new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}),
            new Recipe(Items.GOLDEN_SHOVEL, Items.GOLD_INGOT, 1, Items.STICK, 2,
                    new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}),

            new Recipe(Items.WOODEN_SWORD, Items.OAK_PLANKS, 2, Items.STICK, 1,
                    new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}),
            new Recipe(Items.WOODEN_PICKAXE, Items.OAK_PLANKS, 3, Items.STICK, 2,
                    new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}),
            new Recipe(Items.WOODEN_AXE, Items.OAK_PLANKS, 3, Items.STICK, 2,
                    new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}),
            new Recipe(Items.WOODEN_SHOVEL, Items.OAK_PLANKS, 1, Items.STICK, 2,
                    new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}),

            new Recipe(Items.DIAMOND_HELMET, Items.DIAMOND, 5,
                    new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}),
            new Recipe(Items.DIAMOND_CHESTPLATE, Items.DIAMOND, 8,
                    new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}),
            new Recipe(Items.DIAMOND_LEGGINGS, Items.DIAMOND, 7,
                    new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}),
            new Recipe(Items.DIAMOND_BOOTS, Items.DIAMOND, 4,
                    new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}),

            new Recipe(Items.IRON_HELMET, Items.IRON_INGOT, 5,
                    new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}),
            new Recipe(Items.IRON_CHESTPLATE, Items.IRON_INGOT, 8,
                    new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}),
            new Recipe(Items.IRON_LEGGINGS, Items.IRON_INGOT, 7,
                    new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}),
            new Recipe(Items.IRON_BOOTS, Items.IRON_INGOT, 4,
                    new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}),

            new Recipe(Items.GOLDEN_HELMET, Items.GOLD_INGOT, 5,
                    new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}),
            new Recipe(Items.GOLDEN_CHESTPLATE, Items.GOLD_INGOT, 8,
                    new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}),
            new Recipe(Items.GOLDEN_LEGGINGS, Items.GOLD_INGOT, 7,
                    new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}),
            new Recipe(Items.GOLDEN_BOOTS, Items.GOLD_INGOT, 4,
                    new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}),

            new Recipe(Items.LEATHER_HELMET, Items.LEATHER, 5,
                    new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}),
            new Recipe(Items.LEATHER_CHESTPLATE, Items.LEATHER, 8,
                    new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}),
            new Recipe(Items.LEATHER_LEGGINGS, Items.LEATHER, 7,
                    new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}),
            new Recipe(Items.LEATHER_BOOTS, Items.LEATHER, 4,
                    new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}),

            new Recipe(Items.GOLDEN_APPLE, Items.GOLD_INGOT, 8, Items.APPLE, 1,
                    new int[]{1, 1, 1, 1, 2, 1, 1, 1, 1})
    );
    
    private final BoolProperty craftSwords = new BoolProperty("Swords", true);
    private final BoolProperty craftPickaxes = new BoolProperty("Pickaxes", true);
    private final BoolProperty craftAxes = new BoolProperty("Axes", true);
    private final BoolProperty craftShovels = new BoolProperty("Shovels", true);
    private final BoolProperty craftHelmets = new BoolProperty("Helmets", true);
    private final BoolProperty craftChestplates = new BoolProperty("Chestplates", true);
    private final BoolProperty craftLeggings = new BoolProperty("Leggings", true);
    private final BoolProperty craftBoots = new BoolProperty("Boots", true);
    private final BoolProperty craftApples = new BoolProperty("Golden Apples", true);
    private final SliderProperty craftDelay = new SliderProperty("Craft Delay", 0, 500, 100, 10);
    private final TickingTimer craftTimer = new TickingTimer();
    private boolean isCrafting = false;

    public AutoCrafterModule() {
        super("AutoCrafter",
                "Automatically crafts items when crafting table is open",
                ModuleCategory.Player);
        this.addSettings(craftSwords, craftPickaxes, craftAxes, craftShovels,
                craftHelmets, craftChestplates, craftLeggings, craftBoots,
                craftApples, craftDelay);
    }

    @EventHook
    private void onTickEvent(ClientTickEvent event) {
        if (!isValidState()) return;
        if (!craftTimer.hasTimeElapsed(craftDelay.getValue().intValue())) return;

        if (isCrafting) {
            collectResult();
        } else {
            if (hasAnythingEnabled()) {
                startCrafting();
            }
        }
    }

    private boolean isValidState() {
        return mc.player != null && mc.level != null && mc.screen instanceof CraftingScreen;
    }

    private boolean hasAnythingEnabled() {
        return craftSwords.getValue() || craftPickaxes.getValue() || craftAxes.getValue() ||
                craftShovels.getValue() || craftHelmets.getValue() || craftChestplates.getValue() ||
                craftLeggings.getValue() || craftBoots.getValue() || craftApples.getValue();
    }

    private void startCrafting() {
        Recipe selectedRecipe = findCraftableRecipe();
        if (selectedRecipe != null) {
            craftRecipe(selectedRecipe);
        }
    }

    private Recipe findCraftableRecipe() {
        if (craftApples.getValue()) {
            Recipe appleRecipe = getRecipeByResult(Items.GOLDEN_APPLE);
            if (appleRecipe != null && canCraftRecipe(appleRecipe)) {
                return appleRecipe;
            }
        }

        for (Recipe recipe : RECIPES) {
            if (recipe.result == Items.GOLDEN_APPLE) continue;

            if (isRecipeEnabled(recipe) && canCraftRecipe(recipe) && shouldCraftItem(recipe.result)) {
                if (craftApples.getValue() && usesGold(recipe)) continue;
                return recipe;
            }
        }

        return null;
    }

    private boolean isRecipeEnabled(Recipe recipe) {
        Item result = recipe.result;

        if (isSword(result)) return craftSwords.getValue();
        if (isPickaxe(result)) return craftPickaxes.getValue();
        if (isAxe(result)) return craftAxes.getValue();
        if (isShovel(result)) return craftShovels.getValue();

        if (isHelmet(result)) return craftHelmets.getValue();
        if (isChestplate(result)) return craftChestplates.getValue();
        if (isLeggings(result)) return craftLeggings.getValue();
        if (isBoots(result)) return craftBoots.getValue();

        return false;
    }

    private boolean canCraftRecipe(Recipe recipe) {
        if (getItemCount(recipe.material1) < recipe.count1) return false;
        return recipe.material2 == null || getItemCount(recipe.material2) >= recipe.count2;
    }

    private boolean shouldCraftItem(Item item) {
        if (item == Items.GOLDEN_APPLE) return true;
        return !hasItemInInventory(item);
    }

    private void craftRecipe(Recipe recipe) {
        clearCraftingGrid();
        placeRecipeItems(recipe);
        isCrafting = true;
        craftTimer.reset();
    }

    private void placeRecipeItems(Recipe recipe) {
        for (int i = 0; i < recipe.pattern.length; i++) {
            int materialType = recipe.pattern[i];
            if (materialType == -1) continue;

            Item material = (materialType == 1) ? recipe.material1 : recipe.material2;
            if (material != null) {
                placeItemInSlot(material, i + 1);
            }
        }
    }

    private void placeItemInSlot(Item item, int craftingSlot) {
        int sourceSlot = findLargestStack(item);
        if (sourceSlot != -1) {
            mc.gameMode.handleInventoryMouseClick(
                    mc.player.containerMenu.containerId,
                    sourceSlot,
                    1,
                    ClickType.PICKUP,
                    mc.player
            );
            mc.gameMode.handleInventoryMouseClick(
                    mc.player.containerMenu.containerId,
                    craftingSlot,
                    1,
                    ClickType.PICKUP,
                    mc.player
            );
        }
    }

    private void clearCraftingGrid() {
        for (int i = 1; i <= 9; i++) {
            ItemStack stack = mc.player.containerMenu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                mc.gameMode.handleInventoryMouseClick(
                        mc.player.containerMenu.containerId,
                        i,
                        0,
                        ClickType.QUICK_MOVE,
                        mc.player
                );
            }
        }
    }

    private void collectResult() {
        ItemStack result = mc.player.containerMenu.getSlot(0).getItem();
        if (!result.isEmpty()) {
            mc.gameMode.handleInventoryMouseClick(
                    mc.player.containerMenu.containerId,
                    0,
                    0,
                    ClickType.QUICK_MOVE,
                    mc.player
            );
        }
        isCrafting = false;
        craftTimer.reset();
    }

    private int findLargestStack(Item item) {
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

    private int getItemCount(Item item) {
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private boolean hasItemInInventory(Item item) {
        return getItemCount(item) > 0;
    }

    private Recipe getRecipeByResult(Item result) {
        return RECIPES.stream().filter(r -> r.result == result).findFirst().orElse(null);
    }

    private boolean usesGold(Recipe recipe) {
        return recipe.material1 == Items.GOLD_INGOT || (recipe.material2 != null && recipe.material2 == Items.GOLD_INGOT);
    }

    private boolean isHelmet(Item item) {
        return item == Items.DIAMOND_HELMET || item == Items.IRON_HELMET ||
                item == Items.GOLDEN_HELMET || item == Items.LEATHER_HELMET;
    }

    private boolean isChestplate(Item item) {
        return item == Items.DIAMOND_CHESTPLATE || item == Items.IRON_CHESTPLATE ||
                item == Items.GOLDEN_CHESTPLATE || item == Items.LEATHER_CHESTPLATE;
    }

    private boolean isLeggings(Item item) {
        return item == Items.DIAMOND_LEGGINGS || item == Items.IRON_LEGGINGS ||
                item == Items.GOLDEN_LEGGINGS || item == Items.LEATHER_LEGGINGS;
    }

    private boolean isBoots(Item item) {
        return item == Items.DIAMOND_BOOTS || item == Items.IRON_BOOTS ||
                item == Items.GOLDEN_BOOTS || item == Items.LEATHER_BOOTS;
    }

    private boolean isSword(Item item) {
        return item == Items.DIAMOND_SWORD || item == Items.IRON_SWORD ||
                item == Items.GOLDEN_SWORD || item == Items.WOODEN_SWORD;
    }

    private boolean isPickaxe(Item item) {
        return item == Items.DIAMOND_PICKAXE || item == Items.IRON_PICKAXE ||
                item == Items.GOLDEN_PICKAXE || item == Items.WOODEN_PICKAXE;
    }

    private boolean isAxe(Item item) {
        return item == Items.DIAMOND_AXE || item == Items.IRON_AXE ||
                item == Items.GOLDEN_AXE || item == Items.WOODEN_AXE;
    }

    private boolean isShovel(Item item) {
        return item == Items.DIAMOND_SHOVEL || item == Items.IRON_SHOVEL ||
                item == Items.GOLDEN_SHOVEL || item == Items.WOODEN_SHOVEL;
    }

    @Override
    public void onEnable() {
        resetState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        resetState();
        super.onDisable();
    }

    private void resetState() {
        isCrafting = false;
        craftTimer.reset();
    }

    private static class Recipe {
        final Item result;
        final Item material1;
        final int count1;
        final Item material2;
        final int count2;
        final int[] pattern;

        Recipe(Item result, Item material1, int count1, int[] pattern) {
            this(result, material1, count1, null, 0, pattern);
        }

        Recipe(Item result, Item material1, int count1, Item material2, int count2, int[] pattern) {
            this.result = result;
            this.material1 = material1;
            this.count1 = count1;
            this.material2 = material2;
            this.count2 = count2;
            this.pattern = pattern;
        }
    }
}