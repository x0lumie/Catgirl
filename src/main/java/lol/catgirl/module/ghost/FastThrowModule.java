package lol.catgirl.module.ghost;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.BoolProperty;
import lol.catgirl.setting.impl.SliderProperty;
import net.minecraft.world.item.*;

public class FastThrowModule extends Module {
    public static final FastThrowModule INSTANCE = new FastThrowModule();

    public final SliderProperty delay = new SliderProperty("Delay", 0, 0, 50, 1);
    public final BoolProperty includeBlocks = new BoolProperty("Include Blocks", false);
    public final BoolProperty includeProjectiles = new BoolProperty("Include Projectiles", false);
    public final BoolProperty includeXP = new BoolProperty("Include XP", true);

    public FastThrowModule() {
        super("FastThrow", "Modifies the delay when using items.", ModuleCategory.Ghost);
        addSettings(delay, includeBlocks, includeProjectiles, includeXP);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (!isEnabled() || mc.player == null || mc.player.getInventory().getSelectedItem() == null) {
            return;
        }

        Item item = mc.player.getInventory().getSelectedItem().getItem();

        if (includeBlocks.getValue() && !(item instanceof BlockItem)) {
            return;
        }

        if (includeProjectiles.getValue() && !isProjectile(item)) {
            return;
        }

        // if this errors ignore it
        mc.rightClickDelay = delay.getValue().intValue();
    }

    private boolean isProjectile(Item item) {
        return item instanceof SnowballItem
                || item instanceof EggItem
                || item instanceof EnderpearlItem
                || item instanceof ExperienceBottleItem
                || item instanceof PotionItem
                || item instanceof BowItem;
    }
}
