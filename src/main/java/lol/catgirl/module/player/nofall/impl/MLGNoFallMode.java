package lol.catgirl.module.player.nofall.impl;

import lol.catgirl.event.impl.PlayerRotationEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.module.player.NoFallModule;
import lol.catgirl.module.player.nofall.NoFallMode;
import lol.catgirl.utils.player.RotationUtils;
import lombok.AllArgsConstructor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

@AllArgsConstructor
public class MLGNoFallMode implements NoFallMode {
    public final NoFallModule module;

    public MLGNoFallMode(NoFallModule module) {
        this.module = module;
    }

    private float[] rotations;
    private boolean placing;
    private boolean hasPlaced;

    private int previousSlot = -1;

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (mc.player.onGround()) {
            if (placing) {
                tryCollectWater();
            }
            resetState();
            return;
        }

        if (mc.player.fallDistance < module.distance.getValue()) {
            if (!placing) {
                rotations = null;
            }
            return;
        }

        Vec3 eye = mc.player.getEyePosition();
        Vec3 end = eye.add(0, -(mc.player.fallDistance + 8), 0);

        var hit = mc.level.clip(new ClipContext(
                eye, end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                mc.player
        ));

        if (hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
            return;
        }

        int slot = findMLGItem();
        if (slot == -1) return;

        if (previousSlot == -1) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
        }
        mc.player.getInventory().setSelectedSlot(slot);

        Vec3 targetPos = hit.getLocation();

        rotations = RotationUtils.getRotationsToPoint(
                new float[]{mc.player.getYRot(), mc.player.getXRot()},
                eye,
                targetPos
        );

        double nextY = mc.player.getY() + mc.player.getDeltaMovement().y;
        boolean closeToGround = nextY <= hit.getLocation().y + 1.5;

        if (closeToGround && !hasPlaced) {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            hasPlaced = true;
            placing = true;
        }
    }

    @Override
    public void onRotation(PlayerRotationEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (rotations == null) return;

        RotationUtils.setRotationSpeed(180);

        event.yaw = rotations[0];
        event.pitch = rotations[1];

    }

    private void tryCollectWater() {
        if (!module.collectWater.getValue()) return;

        int bucketSlot = findEmptyBucket();
        if (bucketSlot == -1) return;

        Vec3 eye = mc.player.getEyePosition();
        Vec3 end = eye.add(0, -2.0, 0);

        var hit = mc.level.clip(new ClipContext(
                eye, end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY,
                mc.player
        ));

        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            mc.player.getInventory().setSelectedSlot(bucketSlot);
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }
    }

    private void resetState() {
        placing = false;
        hasPlaced = false;
        rotations = null;

        if (previousSlot != -1) {
            mc.player.getInventory().setSelectedSlot(previousSlot);
            previousSlot = -1;
        }
    }

    private int findMLGItem() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.WATER_BUCKET) return i;
            if (module.useCobwebs.getValue() && stack.getItem() == Items.COBWEB) return i;
        }
        return -1;
    }

    private int findEmptyBucket() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.BUCKET) return i;
        }
        return -1;
    }

}
