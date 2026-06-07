package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.*;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.Animation;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.client.Easing;
import lol.catgirl.utils.client.TickingTimer;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;
import lol.catgirl.utils.player.RotationUtils;
import lol.catgirl.utils.player.ScaffoldUtils;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.nanovg.NanoVG;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public final class ScaffoldModule extends Module {

    private enum Mode {
        Normal,
        Telly,
        Breezily,
        God
    }

    public enum TowerMode {
        None,
        Matrix
    }

    public enum BlockCounterMode {
        None,
        Simple,
        Catgirl
    }

    private static final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Normal);
    private final EnumProperty<TowerMode> towerMode = new EnumProperty<>("Mode", TowerMode.Matrix);
    private final EnumProperty<BlockCounterMode> blockCounterMode = new EnumProperty<>("Block Counter Mode", BlockCounterMode.Simple);
    public static SliderProperty minRotationSpeed = new SliderProperty("Min Rot Speed", 30, 1f, 180, 1f);
    public static SliderProperty maxRotationSpeed = new SliderProperty("Max Rot Speed", 30, 1f, 180, 1f);
    private final SliderProperty placeDelay = new SliderProperty("Place Delay", 0, 0, 10, 1);
    public static BoolProperty rayCast = new BoolProperty("Ray Cast", true);
    public static BoolProperty strict = new BoolProperty("Strict Ray Cast", true).hide(() -> !rayCast.getValue());
    public static BoolProperty useMouseClick = new BoolProperty("Use Mouse Click", true);
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
        addSettings(mode, blockCounterMode, towerMode, minRotationSpeed, maxRotationSpeed, placeDelay, rayCast, strict, useMouseClick, sprint, jump, keepY, sneak, sneakEvery);
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

        if (maxRotationSpeed.getValue() < minRotationSpeed.getValue())
            maxRotationSpeed.setValue(minRotationSpeed.getValue());
        if (minRotationSpeed.getValue() > maxRotationSpeed.getValue())
            minRotationSpeed.setValue(maxRotationSpeed.getValue());

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

    private float randomRotationSpeed() {
        float min = minRotationSpeed.getValue();
        float max = maxRotationSpeed.getValue();
        if (min >= max) return min;
        return (float) ThreadLocalRandom.current().nextDouble(min, max);
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
                PlayerUtils.jump();
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

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        if (towerMode.getValue() == TowerMode.Matrix) {
            Towers.matrix(event);
        }
    }

    private void getBaseRotations(PlayerRotationEvent event) {
        float[] targetRotations = new float[]{mc.player.getYRot() - 180f, 82.5f};
        boolean foundValidRotation = false;

        double difference = mc.player.getY() + mc.player.getEyeHeight() - blockData.position.getY() -
                0.5 - (Math.random() - 0.5) * 0.1;

        HitResult hitResult = null;

        for (int offset = -180; offset <= 180; offset += 45) {
            mc.player.setPos(mc.player.getX(), mc.player.getY() - difference, mc.player.getZ());
            hitResult = PlayerUtils.raycastBlocks(mc.player.getYRot() + (offset * 3), 0f, 4.5f, false);
            mc.player.setPos(mc.player.getX(), mc.player.getY() + difference, mc.player.getZ());

            if (hitResult != null && hitResult.getLocation() != null) {
                Vec2 rotations = RotationUtils.calculate(hitResult.getLocation());

                if (PlayerUtils.isLookingAtBlock(blockData.facing, blockData.position, true, 4.5f, rotations.x, rotations.y)) {
                    targetRotations[0] = rotations.x;
                    targetRotations[1] = rotations.y;
                    foundValidRotation = true;
                    break;
                }
            }
        }

        if (!foundValidRotation) {
            final Vec2 rotations = RotationUtils.calculate(
                    new Vec3(blockData.getPosition().getX(), blockData.getPosition().getY(), blockData.getPosition().getZ()), blockData.getFacing());

            if (PlayerUtils.isLookingAtBlock(blockData.facing, blockData.position, true, 4.5f, rotations.x, rotations.y)) {
                targetRotations[0] = rotations.x;
                targetRotations[1] = rotations.y;
                foundValidRotation = true;
            }
        }

        RotationUtils.setRotationSpeed(randomRotationSpeed());

        event.yaw = targetRotations[0];
        event.pitch = targetRotations[1];

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

        handlePlace();

        placedBlocks++;
    }

    private void handlePlace() {
        if (useMouseClick.getValue()) {
            mc.startUseItem();
        } else {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(ScaffoldUtils.getNewVector(blockData), blockData.getFacing(), blockData.getPosition(), false));
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
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

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }

    // TODO: matrix tower flags but doesnt setback so..

    public static class Towers {
        public static void matrix(PreMotionEvent event) {
            if (mc.options.keyJump.isDown()
                    && PlayerUtils.isBlockUnder(2, false)
                    && mc.player.getDeltaMovement().y < 0.2) {
                mc.player.jumpFromGround();

                event.onGround = true;
            }
        }
    }
    private final Animation popAnimation = new Animation(Easing.DECELERATE, 200L);
    private boolean shouldRender = false;

    @EventHook
    public void onRenderTick(RenderTickEvent event) {
        if (blockCounterMode.getValue() != BlockCounterMode.Catgirl) {
            return;
        }

        if (mc.player == null) return;

        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty() || !(held.getItem() instanceof BlockItem)) return;

        int count = 0;

        for (ItemStack stack : mc.player.getInventory().getNonEquipmentItems()) {
            if (stack.is(held.getItem())) {
                count += stack.getCount();
            }
        }

        String labelText = "Blocks  ";
        String countText = "" + count;
        String totalText = labelText + countText;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        float x = screenW / 2f;
        float y = screenH / 1f - 70;

        float iconSize = 16f;
        float innerSpacing = 5f;
        float paddingLeft = 6f;
        float paddingRight = 7f;

        float fontSize = 9f;
        float textWidth = (float) DrawUtil.getStringWidth(totalText, fontSize);
        float labelWidth = (float) DrawUtil.getStringWidth(labelText, fontSize);

        float boxW = paddingLeft + iconSize + innerSpacing + textWidth + paddingRight;
        float boxH = 22f;

        float left = x - boxW / 2f;
        float top = y - boxH / 2f;
        float right = x + boxW / 2f;
        float bottom = y + boxH / 2f;

        DrawUtil.begin();

        Color themeColor = ColorUtils.getClientTheme();
        Color glowColor = new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 40);
        DrawUtil.drawShadow(left, top, boxW, boxH, 5f, 6f, glowColor);

        DrawUtil.roundedRectVarying(
                left, top, right, bottom,
                4f, 4f, 4f, 4f,
                new Color(10, 10, 10, 245)
        );

        DrawUtil.end();

        var gg = event.context;

        int itemX = (int) (left + paddingLeft);
        int itemY = (int) (top + (boxH - iconSize) / 2f);

        gg.renderItem(held, itemX, itemY);

        DrawUtil.begin();

        float textX = left + paddingLeft + iconSize + innerSpacing;
        float textY = bottom - 7f;

        Color labelColor = new Color(themeColor.getRed(),
                themeColor.getGreen(),
                themeColor.getBlue(), 220
        );

        DrawUtil.drawString(
                labelText,
                textX,
                textY,
                fontSize,
                labelColor,
                ResourceManager.FontResources.productSansBold
        );

        DrawUtil.drawString(
                countText,
                textX + labelWidth,
                textY,
                fontSize,
                new Color(255, 255, 255, 255),
                ResourceManager.FontResources.productSansBold
        );

        DrawUtil.end();
    }

    @EventHook
    public void onRender2D(Render2DEvent event) {
        if (blockCounterMode.getValue() != BlockCounterMode.Simple) {
            return;
        }

        if (mc.player == null) return;

        int blocks = 0;
        for (ItemStack stack : mc.player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() instanceof BlockItem) {
                blocks += stack.getCount();
            }
        }

        String text = "blocks: " + blocks;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int x = centerX - mc.font.width(text) - 6;
        int y = centerY + 6;

        event.context.drawString(mc.font, text, x, y, Color.white.getRGB(), true);
    }
}