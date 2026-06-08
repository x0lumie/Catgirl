package lol.catgirl.utils.player.inventory;

import net.minecraft.world.item.Item;

public final class Recipe {
    public final Item result;
    public final Item material1;
    public final int count1;
    public final Item material2;
    public final int count2;
    public final int[] pattern;
    public final Type type;

    public Recipe(Item result, Item material1, int count1, int[] pattern, Type type) {
        this(result, material1, count1, null, 0, pattern, type);
    }

    public Recipe(Item result, Item material1, int count1, Item material2, int count2, int[] pattern, Type type) {
        this.result = result;
        this.material1 = material1;
        this.count1 = count1;
        this.material2 = material2;
        this.count2 = count2;
        this.pattern = pattern;
        this.type = type;
    }

    public enum Type {
        SWORD, PICKAXE, AXE, SHOVEL, HELMET, CHESTPLATE, LEGGINGS, BOOTS, APPLE
    }
}
