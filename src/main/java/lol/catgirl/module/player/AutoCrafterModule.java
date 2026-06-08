package lol.catgirl.module.player;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.event.EventHook;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.TickingTimer;
import lol.catgirl.utils.player.inventory.InventoryUtils;
import lol.catgirl.utils.player.inventory.Recipe;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;

public final class AutoCrafterModule extends Module {
    public static final AutoCrafterModule INSTANCE = new AutoCrafterModule();

    private final List<Recipe> RECIPES = Arrays.asList(
            new Recipe(Items.DIAMOND_SWORD, Items.DIAMOND, 2, Items.STICK, 1, new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}, Recipe.Type.SWORD),
            new Recipe(Items.DIAMOND_PICKAXE, Items.DIAMOND, 3, Items.STICK, 2, new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}, Recipe.Type.PICKAXE),
            new Recipe(Items.DIAMOND_AXE, Items.DIAMOND, 3, Items.STICK, 2, new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}, Recipe.Type.AXE),
            new Recipe(Items.DIAMOND_SHOVEL, Items.DIAMOND, 1, Items.STICK, 2, new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}, Recipe.Type.SHOVEL),

            new Recipe(Items.IRON_SWORD, Items.IRON_INGOT, 2, Items.STICK, 1, new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}, Recipe.Type.SWORD),
            new Recipe(Items.IRON_PICKAXE, Items.IRON_INGOT, 3, Items.STICK, 2, new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}, Recipe.Type.PICKAXE),
            new Recipe(Items.IRON_AXE, Items.IRON_INGOT, 3, Items.STICK, 2, new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}, Recipe.Type.AXE),
            new Recipe(Items.IRON_SHOVEL, Items.IRON_INGOT, 1, Items.STICK, 2, new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}, Recipe.Type.SHOVEL),

            new Recipe(Items.GOLDEN_SWORD, Items.GOLD_INGOT, 2, Items.STICK, 1, new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}, Recipe.Type.SWORD),
            new Recipe(Items.GOLDEN_PICKAXE, Items.GOLD_INGOT, 3, Items.STICK, 2, new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}, Recipe.Type.PICKAXE),
            new Recipe(Items.GOLDEN_AXE, Items.GOLD_INGOT, 3, Items.STICK, 2, new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}, Recipe.Type.AXE),
            new Recipe(Items.GOLDEN_SHOVEL, Items.GOLD_INGOT, 1, Items.STICK, 2, new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}, Recipe.Type.SHOVEL),

            new Recipe(Items.WOODEN_SWORD, Items.OAK_PLANKS, 2, Items.STICK, 1, new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}, Recipe.Type.SWORD),
            new Recipe(Items.WOODEN_PICKAXE, Items.OAK_PLANKS, 3, Items.STICK, 2, new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}, Recipe.Type.PICKAXE),
            new Recipe(Items.WOODEN_AXE, Items.OAK_PLANKS, 3, Items.STICK, 2, new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}, Recipe.Type.AXE),
            new Recipe(Items.WOODEN_SHOVEL, Items.OAK_PLANKS, 1, Items.STICK, 2, new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}, Recipe.Type.SHOVEL),

            new Recipe(Items.DIAMOND_HELMET, Items.DIAMOND, 5, new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}, Recipe.Type.HELMET),
            new Recipe(Items.DIAMOND_CHESTPLATE, Items.DIAMOND, 8, new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}, Recipe.Type.CHESTPLATE),
            new Recipe(Items.DIAMOND_LEGGINGS, Items.DIAMOND, 7, new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}, Recipe.Type.LEGGINGS),
            new Recipe(Items.DIAMOND_BOOTS, Items.DIAMOND, 4, new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}, Recipe.Type.BOOTS),

            new Recipe(Items.IRON_HELMET, Items.IRON_INGOT, 5, new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}, Recipe.Type.HELMET),
            new Recipe(Items.IRON_CHESTPLATE, Items.IRON_INGOT, 8, new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}, Recipe.Type.CHESTPLATE),
            new Recipe(Items.IRON_LEGGINGS, Items.IRON_INGOT, 7, new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}, Recipe.Type.LEGGINGS),
            new Recipe(Items.IRON_BOOTS, Items.IRON_INGOT, 4, new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}, Recipe.Type.BOOTS),

            new Recipe(Items.GOLDEN_HELMET, Items.GOLD_INGOT, 5, new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}, Recipe.Type.HELMET),
            new Recipe(Items.GOLDEN_CHESTPLATE, Items.GOLD_INGOT, 8, new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}, Recipe.Type.CHESTPLATE),
            new Recipe(Items.GOLDEN_LEGGINGS, Items.GOLD_INGOT, 7, new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}, Recipe.Type.LEGGINGS),
            new Recipe(Items.GOLDEN_BOOTS, Items.GOLD_INGOT, 4, new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}, Recipe.Type.BOOTS),

            new Recipe(Items.LEATHER_HELMET, Items.LEATHER, 5, new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}, Recipe.Type.HELMET),
            new Recipe(Items.LEATHER_CHESTPLATE, Items.LEATHER, 8, new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}, Recipe.Type.CHESTPLATE),
            new Recipe(Items.LEATHER_LEGGINGS, Items.LEATHER, 7, new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1},Recipe. Type.LEGGINGS),
            new Recipe(Items.LEATHER_BOOTS, Items.LEATHER, 4, new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}, Recipe.Type.BOOTS),

            new Recipe(Items.GOLDEN_APPLE, Items.GOLD_INGOT, 8, Items.APPLE, 1, new int[]{1, 1, 1, 1, 2, 1, 1, 1, 1}, Recipe.Type.APPLE)
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

    //Sexy mapping to remove if/else, ors, and switch statements <3
    private final java.util.Map<Recipe.Type, BoolProperty> propertyMap = java.util.Map.of(
            Recipe.Type.SWORD, craftSwords,
            Recipe.Type.PICKAXE, craftPickaxes,
            Recipe.Type.AXE, craftAxes,
            Recipe.Type.SHOVEL, craftShovels,
            Recipe.Type.HELMET, craftHelmets,
            Recipe.Type.CHESTPLATE, craftChestplates,
            Recipe.Type.LEGGINGS, craftLeggings,
            Recipe.Type.BOOTS, craftBoots,
            Recipe.Type.APPLE, craftApples
    );

    public AutoCrafterModule() {
        super("AutoCrafter",
                "Automatically crafts items when crafting table is open",
                ModuleCategory.Player);
        this.addSettings(craftSwords, craftPickaxes, craftAxes, craftShovels,
                craftHelmets, craftChestplates, craftLeggings, craftBoots,
                craftApples, craftDelay);
    }

    //Main Loop
    @EventHook
    private void onTickEvent(ClientTickEvent event) {
        if (!isValidState()) return;
        if (!craftTimer.hasTimeElapsed(craftDelay.getValue().intValue())) return;

        if (isCrafting) {
            collectResult();
        } else if (hasAnythingEnabled()) {
            startCrafting();
        }
    }

    //Check if player is in crafting ui
    private boolean isValidState() {
        return mc.player != null && mc.level != null && mc.screen instanceof CraftingScreen;
    }

    //Returns true if any of the mapped properties is true
    private boolean hasAnythingEnabled() {
        return propertyMap.values().stream().anyMatch(BoolProperty::getValue);
    }

    //Finds a valid recipe and if its valid attempts to craft it
    private void startCrafting() {
        Recipe selectedRecipe = findCraftableRecipe();
        if (selectedRecipe != null) {
            craftRecipe(selectedRecipe);
        }
    }

    //Loop through each recipe to see if its craftable - prioritizes gapples
    private Recipe findCraftableRecipe() {
        boolean applesEnabled = craftApples.getValue();

        if (applesEnabled) {
            Recipe appleRecipe = getRecipeByResult(Items.GOLDEN_APPLE);
            if (appleRecipe != null && canCraftRecipe(appleRecipe)) {
                return appleRecipe;
            }
        }

        for (Recipe recipe : RECIPES) {
            if (recipe.type == Recipe.Type.APPLE || (applesEnabled && usesGold(recipe))) continue;

            if (isRecipeEnabled(recipe) && shouldCraftItem(recipe.result) && canCraftRecipe(recipe)) {
                return recipe;
            }
        }

        return null;
    }

    //Checks if the matching recipe type module in the mapping is true.
    private boolean isRecipeEnabled(Recipe recipe) {
        return propertyMap.get(recipe.type).getValue();
    }

    //Checks if u have enough of a needed item
    private boolean canCraftRecipe(Recipe recipe) {
        if (InventoryUtils.getItemCount(recipe.material1) < recipe.count1) return false;
        return recipe.material2 == null || InventoryUtils.getItemCount(recipe.material2) >= recipe.count2;
    }

    //Always crafts gapples and doesn't craft an item if u have it already
    private boolean shouldCraftItem(Item item) {
        return item == Items.GOLDEN_APPLE || !InventoryUtils.hasItemInInventory(item);
    }

    //Clears grid and places items
    private void craftRecipe(Recipe recipe) {
        InventoryUtils.clearCraftingGrid();
        placeRecipeItems(recipe);
        isCrafting = true;
        craftTimer.reset();
    }

    //Places items from recipe into crafting grid
    private void placeRecipeItems(Recipe recipe) {
        for (int i = 0; i < recipe.pattern.length; i++) {
            int materialType = recipe.pattern[i];
            if (materialType == -1) continue;

            Item material = (materialType == 1) ? recipe.material1 : recipe.material2;
            if (material != null) {
                InventoryUtils.placeItemInSlot(material, i + 1);
            }
        }
    }

    //Collect crafted item
    private void collectResult() {
        ItemStack result = mc.player.containerMenu.getSlot(0).getItem();
        if (!result.isEmpty()) {
            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 0, 0, ClickType.QUICK_MOVE, mc.player);
        }
        isCrafting = false;
        craftTimer.reset();
    }

    //Returns recipe via item lookup
    private Recipe getRecipeByResult(Item result) {
        return RECIPES.stream().filter(r -> r.result == result).findFirst().orElse(null);
    }

    //Method to check if the recipe has gold, exists to prioritize gapples
    private boolean usesGold(Recipe recipe) {
        return recipe.material1 == Items.GOLD_INGOT || (recipe.material2 != null && recipe.material2 == Items.GOLD_INGOT);
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
}