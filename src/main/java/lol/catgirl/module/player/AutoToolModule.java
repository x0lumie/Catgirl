package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.combat.AuraModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.utils.client.ItemAnimationUtils;
import lol.catgirl.utils.player.inventory.InventoryUtils;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public final class AutoToolModule extends Module {
    public static final AutoToolModule INSTANCE = new AutoToolModule();

    private int lastSlot = -1;

    public final BoolProperty spoof = new BoolProperty("Spoof", false);
    public final BoolProperty auraSword = new BoolProperty("AuraSword", true);

    public AutoToolModule() {
        super("AutoTool",
                "Switches to the best tool for the right job.",
                ModuleCategory.Player
        );

        addSettings(spoof, auraSword);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || mc.player.isCreative()) {
            reset();
            return;
        }

        boolean auraActive = auraSword.getValue()
                        && AuraModule.INSTANCE.isEnabled()
                        && AuraModule.target != null;

        if (auraActive) {
            handleAuraSword();
            return;
        }

        boolean breaking = mc.gameMode.isDestroying();

        if (breaking) {
            handleMining();
        } else {
            reset();
        }
    }

    private void handleAuraSword() {
        int swordSlot = getBestSwordSlot();

        if (swordSlot == -1) return;

        if (lastSlot == -1) {
            lastSlot = mc.player.getInventory().getSelectedSlot();
        }

        if (spoof.getValue()) {
            ItemAnimationUtils.setSpoofedItem(mc.player.getInventory().getItem(swordSlot));
        } else {
            mc.player.getInventory().setSelectedSlot(swordSlot);
        }
    }

    private void handleMining() {
        int bestSlot = getBestToolSlot();

        if (bestSlot == -1) return;

        if (lastSlot == -1) {
            lastSlot = mc.player.getInventory().getSelectedSlot();
        }

        if (spoof.getValue()) {
            ItemAnimationUtils.setSpoofedItem(mc.player.getInventory().getItem(bestSlot));
        } else {
            mc.player.getInventory().setSelectedSlot(bestSlot);
        }
    }

    private void reset() {
        ItemAnimationUtils.setSpoofedItem(null);

        if (lastSlot != -1) {
            mc.player.getInventory().setSelectedSlot(lastSlot);
        }

        lastSlot = -1;
    }

    private int getBestToolSlot() {
        if (!(mc.hitResult instanceof BlockHitResult hit)) {
            return mc.player.getInventory().getSelectedSlot();
        }

        Block block = mc.level.getBlockState(hit.getBlockPos()).getBlock();

        int bestSlot = mc.player.getInventory().getSelectedSlot();
        float bestSpeed = 1.0F;

        for (int i = 0; i < 9; i++) {
            float speed = mc.player.getInventory()
                    .getItem(i)
                    .getDestroySpeed(block.defaultBlockState());

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private int getBestSwordSlot() {
        int bestSlot = -1;
        double bestValue = -1.0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);

            if (!stack.is(ItemTags.SWORDS)) {
                continue;
            }

            double value = InventoryUtils.getSwordValue(stack);

            if (value > bestValue) {
                bestValue = value;
                bestSlot = i;
            }
        }

        return bestSlot;
    }
}