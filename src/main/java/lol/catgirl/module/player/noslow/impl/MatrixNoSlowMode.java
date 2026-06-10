package lol.catgirl.module.player.noslow.impl;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.module.movement.SpeedModule;
import lol.catgirl.module.player.NoSlowModule;
import lol.catgirl.module.player.noslow.NoSlowMode;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;
import lombok.AllArgsConstructor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;

@AllArgsConstructor
public final class MatrixNoSlowMode implements NoSlowMode {
    public final NoSlowModule module;

    private boolean shouldApplyMatrixForItem(Item item) {
        ItemStack stack = new ItemStack(item);
        if (stack.has(DataComponents.FOOD) && module.matrixFood.getValue()) return true;
        if (item instanceof PotionItem && module.matrixPotion.getValue()) return true;
        if (PlayerUtils.isHoldingWeapon() && module.matrixSword.getValue()) return true;
        if (item instanceof BowItem && module.matrixBow.getValue()) return true;
        return false;
    }

    private boolean isUsingRelevantItem() {
        if (mc.player == null) return false;
        ItemStack heldItem = mc.player.getMainHandItem();
        if (heldItem.isEmpty()) return false;
        return shouldApplyMatrixForItem(heldItem.getItem());
    }

    @Override
    public void onUsingItem(PlayerUseMultiplierEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;

        if (mc.player.isUsingItem() && isUsingRelevantItem()) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onBruhTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.isUsingItem() || !isUsingRelevantItem()) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;

        int ticksSinceUse = mc.player.getUseItemRemainingTicks();
        float itemUseTime = mc.player.getTicksUsingItem();

        if (itemUseTime > 1) {
            float strafeValue = module.matrixStrafeSpeed.getValue().floatValue();
            MoveUtils.setMotionWithoutY(strafeValue);
        } else {
            boolean speedEnabled = SpeedModule.INSTANCE.isEnabled();

            if (!speedEnabled) {
                mc.player.setDeltaMovement(
                        mc.player.getDeltaMovement().x * 0.992,
                        mc.player.getDeltaMovement().y,
                        mc.player.getDeltaMovement().z * 0.992
                );
            } else {
                mc.player.setDeltaMovement(
                        mc.player.getDeltaMovement().x * 0.99,
                        mc.player.getDeltaMovement().y,
                        mc.player.getDeltaMovement().z * 0.99
                );
            }
        }
    }
}
