package lol.catgirl.utils.player.inventory;

import lol.catgirl.utils.IMinecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;

public class SlotUtils implements IMinecraft {
    public static final int HOTBAR_START = 0;
    public static final int HOTBAR_END = 8;
    public static final int MAIN_START = 9;
    public static final int MAIN_END = 35;
    public static final int ARMOR_START = 36;
    public static final int ARMOR_END = 39;
    public static final int OFFHAND = 40;

    public static int indexToId(int i) {
        var player = mc.player; 
        if(player == null) return -1;

        AbstractContainerMenu handler = player.containerMenu;
        if(handler instanceof InventoryMenu) return survivalInventory(i);
        return i;
    } 

    private static int survivalInventory(int i) {
        if(isHotbar(i)) return 36 + i; 
        if(isArmor(i)) return 5 + (i - 36); 
        if(i == OFFHAND) return 45;
        return i;
    }

    public static boolean isHotbar(int index) {
        return index >= HOTBAR_START && index <= HOTBAR_END;
    } 

    public static boolean isMain(int index) {
        return index >= MAIN_START && index <= MAIN_END;
    }

    public static boolean isArmor(int index) {
        return index >= ARMOR_START && index <= ARMOR_END;
    }
}
