package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PlayerRotationEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.BoolProperty;
import lol.catgirl.setting.impl.EnumProperty;
import lol.catgirl.setting.impl.SliderProperty;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

public final class NoFallModule extends Module {
    public static final NoFallModule INSTANCE = new NoFallModule();

    public enum Mode {
        MLG,
        OnGround,
        CubeCraft
    }

    private float[] rotations;
    private boolean placing;
    private boolean hasPlaced;

    private int previousSlot = -1;

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.MLG);
    public final SliderProperty distance = new SliderProperty("Distance", 4, 1, 255, 1);
    public final BoolProperty useCobwebs = new BoolProperty("Cobwebs", true).hide(() -> mode.getValue() != Mode.MLG);
    public final BoolProperty collectWater = new BoolProperty("Collect Water", true).hide(() -> mode.getValue() != Mode.MLG);

    public NoFallModule() {
        super("NoFall", "Stops or negates fall damage.", ModuleCategory.Player);
        addSettings(mode, distance, useCobwebs, collectWater);
    }

    @EventHook
    public void onPacket(PacketSendEvent event) {
        if (mc.player == null) return;

        if (mode.getValue() == Mode.CubeCraft) {
            if (event.getPacket() instanceof ServerboundMovePlayerPacket && mc.player.fallDistance >= 2.5) {
                mc.player.setPosRaw(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                MoveUtils.setMotionY(mc.player.getDeltaMovement().y + 0.1);
                mc.player.fallDistance = 0;
            }
        }

        if (mode.getValue() == Mode.OnGround) {
            if (event.getPacket() instanceof ServerboundMovePlayerPacket packet && mc.player.fallDistance >= 2.5) {
                // if this errors ignore it, and reload gradle
                packet.onGround = true;
                mc.player.fallDistance = 0;
            }
        }
    }

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        if (mode.getValue() != Mode.MLG) return;
        if (mc.player == null || mc.level == null) return;

        if (mc.player.onGround()) {
            if (placing) {
                tryCollectWater();
            }
            resetState();
            return;
        }

        if (mc.player.fallDistance < distance.getValue()) {
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

        float[] rot = RotationUtils.getRotationsToPoint(
                new float[]{mc.player.getYRot(), mc.player.getXRot()},
                eye,
                targetPos
        );
        rotations = RotationUtils.getFixedRotation(rot, rot);

        double nextY = mc.player.getY() + mc.player.getDeltaMovement().y;
        boolean closeToGround = nextY <= hit.getLocation().y + 1.5;

        if (closeToGround && !hasPlaced) {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            hasPlaced = true;
            placing = true;
        }
    }

    @EventHook
    public void onRotation(PlayerRotationEvent event) {
        if (mc.player == null) return;
        if (mode.getValue() == Mode.MLG && rotations != null) {
            event.yaw = rotations[0];
            event.pitch = rotations[1];
        }
    }

    private void tryCollectWater() {
        if (!collectWater.getValue()) return;

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
            if (useCobwebs.getValue() && stack.getItem() == Items.COBWEB) return i;
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

    @Override
    public String getFinalSuffix() {
        return mode.getValue().toString();
    }
}