package lol.catgirl.module.ghost;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.mixin.MinecraftAccessor;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.TickingTimer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public final class AutoDrainModule extends Module {
    public static final AutoDrainModule INSTANCE = new AutoDrainModule();

    private final SliderProperty actionCooldownMs = new SliderProperty("Cooldown MS", 50, 2000, 250, 1);
    private final SliderProperty switchBackDelayMs = new SliderProperty("SwitchBack MS", 0, 500, 75, 1);

    private final TickingTimer cooldownTimer = new TickingTimer();
    private final TickingTimer switchBackTimer = new TickingTimer();

    private int originalSlot = -1;
    private boolean pendingSwitchBack = false;

    public AutoDrainModule() {
        super("AutoDrain",
                "Swap to empty bucket and pick up water when aiming at a source",
                ModuleCategory.Ghost
        );

        addSettings(actionCooldownMs, switchBackDelayMs);
    }

    @EventHook
    private void onTickEvent(ClientTickEvent event) {
        if (mc.screen != null || mc.level == null) {
            return;
        }

        if (pendingSwitchBack) {
            if (switchBackDelayMs.getValue().intValue() <= 0
                    || switchBackTimer.hasTimeElapsed(switchBackDelayMs.getValue().intValue())) {

                if (originalSlot != -1) {
                    mc.player.getInventory().setSelectedSlot(originalSlot);
                }

                originalSlot = -1;
                pendingSwitchBack = false;
            }

            return;
        }

        if (!cooldownTimer.hasTimeElapsed(actionCooldownMs.getValue().intValue())) {
            return;
        }

        if (!(mc.hitResult instanceof BlockHitResult blockHit)) {
            return;
        }

        BlockPos waterPos = getWaterSourcePosFromMouseOver(blockHit);

        if (waterPos == null) {
            return;
        }

        if (isPlayerInCobweb()) {
            return;
        }

        int emptyBucketSlot = findEmptyBucketInHotbar();

        if (emptyBucketSlot == -1) {
            return;
        }

        originalSlot = mc.player.getInventory().getSelectedSlot();

        mc.player.getInventory().setSelectedSlot(emptyBucketSlot);

        ((MinecraftAccessor) mc).invokeStartUseItem();

        pendingSwitchBack = true;

        switchBackTimer.reset();
        cooldownTimer.reset();
    }

    private boolean isWaterSource(FluidState fluidState) {
        return fluidState != null
                && fluidState.getType() == Fluids.WATER
                && fluidState.isSource();
    }

    private int findEmptyBucketInHotbar() {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);

            if (!stack.isEmpty() && stack.getItem() == Items.BUCKET) {
                return slot;
            }
        }

        return -1;
    }

    private boolean isPlayerInCobweb() {
        var box = mc.player.getBoundingBox();

        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);

        int maxX = (int) Math.floor(box.maxX);
        int maxY = (int) Math.floor(box.maxY);
        int maxZ = (int) Math.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (mc.level.getBlockState(new BlockPos(x, y, z)).is(Blocks.COBWEB)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private BlockPos getWaterSourcePosFromMouseOver(BlockHitResult blockHit) {
        BlockPos hitPos = blockHit.getBlockPos();

        BlockState hitState = mc.level.getBlockState(hitPos);

        if (hitState.is(Blocks.WATER)
                && isWaterSource(mc.level.getFluidState(hitPos))) {
            return hitPos;
        }

        BlockPos adjacentPos = hitPos.relative(blockHit.getDirection());

        BlockState adjacentState = mc.level.getBlockState(adjacentPos);

        if (adjacentState.is(Blocks.WATER)
                && isWaterSource(mc.level.getFluidState(adjacentPos))) {
            return adjacentPos;
        }

        return null;
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (pendingSwitchBack
                && originalSlot != -1
                && mc.player != null) {

            mc.player.getInventory().setSelectedSlot(originalSlot);
        }

        originalSlot = -1;
        pendingSwitchBack = false;
    }

}
