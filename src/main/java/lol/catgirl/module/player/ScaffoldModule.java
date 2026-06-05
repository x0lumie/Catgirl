package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PlayerRotationEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.BoolProperty;
import lol.catgirl.setting.impl.EnumProperty;
import lol.catgirl.setting.impl.SliderProperty;
import lol.catgirl.utils.client.TickingTimer;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;
import lol.catgirl.utils.player.RotationUtils;
import lol.catgirl.utils.player.ScaffoldUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class ScaffoldModule extends Module {

    private enum Mode {
        Normal,
        Telly,
        Breezily,
        God
    }

    private static final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Normal);
    private final SliderProperty rotationSpeed = new SliderProperty("Rotation Speed", 2, 1, 10, 0.5f);
    private final SliderProperty placeDelay = new SliderProperty("Place Delay", 0, 0, 10, 1);
    public static BoolProperty rayCast = new BoolProperty("Ray Cast", true);
    public static BoolProperty strict = new BoolProperty("Strict Ray Cast", true).hide(() -> !rayCast.getValue());
    public static BoolProperty sprint = new BoolProperty("Sprint", false);
    public static BoolProperty jump = new BoolProperty("Jump", false);
    public static BoolProperty keepY = new BoolProperty("Keep Y", false).hide(() -> !jump.getValue());
    private static final BoolProperty sneak = new BoolProperty("Sneak", false);
    private final SliderProperty sneakEvery = new SliderProperty("Sneak Every", 1, 0, 10, 1).hide(() -> !sneak.getValue());

    public static final ScaffoldModule INSTANCE = new ScaffoldModule();

    public static int sameYPos;
    private final TickingTimer timer = new TickingTimer();
    private int placedBlocks;
    private int sneakTicks;
    private boolean shouldSneak;
    private BlockData blockData;
    private float placeYaw;
    private float placePitch;
    private boolean canPlace;

    public ScaffoldModule() {
        super("Scaffold", "Places blocks under you creating a bridge.", ModuleCategory.Player);
        addSettings(mode, rotationSpeed, placeDelay, rayCast, strict, sprint, jump, keepY, sneak, sneakEvery);
    }

    @Override
    public void onEnable() {
        sameYPos = 0;
        placedBlocks = 0;
        sneakTicks = 0;
        shouldSneak = false;
        sameYPos = (int) (mc.player.position().y - 1);
        canPlace = true;
        timer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        placedBlocks = 0;
        if (mc.player != null && sneak.getValue() && shouldSneak) {
            mc.options.keyShift.setDown(false);
            shouldSneak = false;
        }
        resetBinds();
        super.onDisable();
    }

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null) return;

        updateScaffoldYCoord();
        updateBlockPos();

        handleSneak();
        handleMovement();

        if (getBlockSlot() != -1) mc.player.getInventory().setSelectedSlot(getBlockSlot());

        if (blockData == null) return;
        place();
    }

    @EventHook
    public void onPlayerRotation(PlayerRotationEvent event) {
        handleRotations(event);
    }

    private void updateScaffoldYCoord() {
        if (keepY.getValue() && !mc.options.keyJump.isDown()) {
            if (mc.player.onGround()) {
                sameYPos = (int) Math.floor(mc.player.position().y - 1);
            }
        } else {
            sameYPos = (int) Math.floor(mc.player.position().y - 1);
        }
    }

    private void updateBlockPos() {
        if (ScaffoldUtils.getBlockData() != null) {
            blockData = ScaffoldUtils.getBlockData();
        }
    }

    private void handleSneak() {
        if (!sneak.getValue()) {
            if (shouldSneak) {
                mc.options.keyShift.setDown(false);
                shouldSneak = false;
            }
            return;
        }

        int sneakEveryVal = sneakEvery.getValue().intValue();
        if (sneakEveryVal <= 0) return;

        if (placedBlocks % sneakEveryVal == 0 && placedBlocks > 0) {
            if (!shouldSneak) {
                mc.options.keyShift.setDown(true);
                shouldSneak = true;
                sneakTicks = 5;
            }
        }

        if (shouldSneak) {
            sneakTicks--;
            if (sneakTicks <= 0) {
                mc.options.keyShift.setDown(false);
                shouldSneak = false;
            }
        }
    }

    private void handleMovement() {
        if (sprint.getValue()) {
            if (!mc.player.isSprinting() && mc.player.input.hasForwardImpulse()) {
                mc.player.setSprinting(true);
            }
        } else {
            if (mc.player.isSprinting()) {
                mc.player.setSprinting(false);
            }
        }

        if (jump.getValue()) {
            if (!MoveUtils.isMoving() || mc.options.keyJump.isDown()) return;
            if (mc.player.onGround()) {
                mc.player.jumpFromGround();
            }
        }
    }

    private void handleRotations(PlayerRotationEvent event) {
        switch (mode.getValue()) {
            case Normal -> getBaseRotations(event);
            case Telly -> {
                if (!mc.player.onGround()) {
                    getBaseRotations(event);
                    canPlace = true;
                } else {
                    canPlace = false;
                    event.yaw = mc.player.getYRot();
                    event.pitch = 60;
                }
            }
            case Breezily -> {
                getBaseRotations(event);
                if (mc.options.keyUp.isDown() && !mc.options.keyJump.isDown()) {
                    double offset = 0;
                    double speed = 0;

                    switch (mc.player.getDirection()) {
                        case NORTH:
                            offset = mc.player.position().x - Math.floor(mc.player.position().x);
                            speed = mc.player.getDeltaMovement().z;
                            break;

                        case EAST:
                            offset = mc.player.position().z - Math.floor(mc.player.position().z);
                            speed = mc.player.getDeltaMovement().x;
                            break;

                        case SOUTH:
                            offset = 1 - (mc.player.position().x - Math.floor(mc.player.position().x));
                            speed = mc.player.getDeltaMovement().z;
                            break;

                        case WEST:
                            offset = 1 - (mc.player.position().z - Math.floor(mc.player.position().z));
                            speed = mc.player.getDeltaMovement().x;
                            break;

                        default:
                            break;
                    }
                    speed = Math.abs(speed);

                    if (speed < 0.086 && Math.abs(offset - 0.5) < 0.4 && placeDelay.getValue().intValue() <= 1) {
                    } else if (offset < 0.5 + ((Math.random() - 0.5) / 10)) {
                        mc.options.keyLeft.setDown(false);
                        mc.options.keyRight.setDown(true);
                    } else {
                        mc.options.keyRight.setDown(false);
                        mc.options.keyLeft.setDown(true);
                    }
                }
            }
            case God -> {
                getBaseRotations(event);
                if (placedBlocks >= 7) {
                    mc.options.keyJump.setDown(true);
                    placedBlocks = 0;
                }
                if (placedBlocks == 1 && mc.options.keyJump.isDown()) {
                    mc.options.keyJump.setDown(false);
                }
            }
        }
    }

    private void getBaseRotations(PlayerRotationEvent event) {
        float[] targetRotations = new float[]{mc.player.getYRot() - 180, 82.5f};
        for (float possibleYaw = mc.player.getYRot() - 180; possibleYaw <= mc.player.getYRot() + 360 - 180; possibleYaw += 45) {
            for (float possiblePitch = 90; possiblePitch > 30; possiblePitch -= possiblePitch > (mc.player.hasEffect(MobEffects.SPEED) ? 60 : 80) ? 1 : 10) {
                if (PlayerUtils.isLookingAtBlock(blockData.getFacing(), blockData.getPosition(), true, 5, possibleYaw, possiblePitch)) {
                    targetRotations[0] = possibleYaw;
                    targetRotations[1] = possiblePitch;
                }
            }
        }

        float smoothedYaw = RotationUtils.smoothRotation(event.yaw, targetRotations[0], rotationSpeed.getValue() / 2f);
        float smoothedPitch = RotationUtils.smoothRotation(event.pitch, targetRotations[1], rotationSpeed.getValue() / 2f);

        float[] finalRotations = RotationUtils.getFixedRotation(new float[]{smoothedYaw, smoothedPitch}, new float[]{event.yaw, event.pitch});

        event.yaw = finalRotations[0];
        event.pitch = finalRotations[1];

        placeYaw = event.yaw;
        placePitch = event.pitch;
    }

    private void place() {

        boolean STOP = false;

        if (!canPlace) return;

        if (timer.hasTimeElapsed((long) (placeDelay.getValue() * 50L))) {
            timer.reset();
        } else {
            return;
        }

        if (rayCast.getValue()) {
            BlockHitResult blockHitResult = PlayerUtils.raycastBlocks(placeYaw, placePitch, 4.5f, false);
            if (blockHitResult == null) {
                STOP = true;
            }
            if (!(blockHitResult.getBlockPos().getX() == blockData.getPosition().getX() && blockHitResult.getBlockPos().getY() == blockData.getPosition().getY() && blockHitResult.getBlockPos().getZ() == blockData.getPosition().getZ())) {
                STOP = true;
            }
            if (strict.getValue()) {
                if (blockHitResult.getDirection() != blockData.getFacing()) {
                    STOP = true;
                }
            }
        }

        if (STOP) {
            return;
        }

        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(ScaffoldUtils.getNewVector(blockData), blockData.getFacing(), blockData.getPosition(), false));
        mc.player.swing(InteractionHand.MAIN_HAND);

        placedBlocks++;
    }


    private int getBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }

    public void resetBinds() {
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyJump.setDown(false);
        mc.options.keyShift.setDown(false);
    }

    @Getter
    public static class BlockData {
        private BlockPos position;
        private Direction facing;

        public BlockData(final BlockPos position, final Direction facing) {
            this.position = position;
            this.facing = facing;
        }
    }
}