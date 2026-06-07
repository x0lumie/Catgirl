package lol.catgirl.module.player;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.*;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.combat.AuraModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.player.PlayerUtils;
import lol.catgirl.utils.player.RotationUtils;
import lol.catgirl.utils.render.RenderUtils;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static net.minecraft.client.renderer.RenderPipelines.DEBUG_FILLED_SNIPPET;
import static net.minecraft.client.renderer.RenderPipelines.LINES_SNIPPET;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public class ChestAuraModule extends Module {
    public static final ChestAuraModule INSTANCE = new ChestAuraModule();

    public final SliderProperty range = new SliderProperty("Range", 3.0f, 0f, 6f, 0.05f);
    public final BoolProperty rotate = new BoolProperty("Rotate", true);
    public final SliderProperty rotationSpeed = new SliderProperty("Rotation Speed", 69f, 1f, 180f, 1f);
    public final BoolProperty visuals = new BoolProperty("Visuals", true);
    public final SliderProperty visualRange = new SliderProperty("Visual Range", 6.0f, 0f, 12f, 0.05f).hide(() -> !visuals.getValue());
    public final EnumProperty<RenderMode> renderMode = new EnumProperty<>("Mode", RenderMode.Outline).hide(() -> !visuals.getValue());
    public final SliderProperty alphaValue = new SliderProperty("Fill Alpha", 150, 0, 255, 1).hide(() -> !visuals.getValue() || !renderMode.getValue().equals(RenderMode.Filled));
    public final SliderProperty outlineWidth = new SliderProperty("Outline Width", 1.5f, 1.0f, 5.0f, 0.1f).hide(() -> !visuals.getValue() || !renderMode.getValue().equals(RenderMode.Outline));
    public final BoolProperty pauseScaffold = new BoolProperty("Pause Scaffold", true);
    public final BoolProperty waitForAura = new BoolProperty("Wait For Aura", true);
    public final Set<BlockPos> openedChests = new HashSet<>();
    public BlockPos targetChest = null;

    public ChestAuraModule() {
        super("ChestAura", "Automatically opens chests around you.", ModuleCategory.Player);
    }

    @RequiredArgsConstructor
    public enum RenderMode {
        Outline, Filled

    }

    private static final RenderPipeline DEBUG_FILLED_BOX =
            RenderPipelines.register(
                    RenderPipeline.builder(DEBUG_FILLED_SNIPPET)
                            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                            .withDepthWrite(false)
                            .withLocation("pipeline/debug_filled_box")
                            .build()
            );

    private static final RenderType FILLED =
            RenderType.create(
                    "filled_box",
                    RenderSetup.builder(DEBUG_FILLED_BOX)
                            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                            .setOutputTarget(OutputTarget.MAIN_TARGET)
                            .sortOnUpload()
                            .createRenderSetup()
            );

    private static final RenderPipeline LINES_PIPELINE =
            RenderPipelines.register(
                    RenderPipeline.builder(LINES_SNIPPET)
                            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                            .withDepthWrite(false)
                            .withLocation("pipeline/lines_no_depth")
                            .build()
            );

    private static final RenderType LINES =
            RenderType.create(
                    "lines",
                    RenderSetup.builder(LINES_PIPELINE)
                            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                            .setOutputTarget(OutputTarget.MAIN_TARGET)
                            .createRenderSetup()
            );

    private boolean isRiding() {
        return mc.player != null && mc.player.isPassenger();
    }

    private boolean shouldWaitForAura() {
        var aura = AuraModule.INSTANCE;
        return waitForAura.getValue() && aura != null && AuraModule.target != null;
    }

    private boolean isScaffolding() {
        return pauseScaffold.getValue() &&
                ScaffoldModule.INSTANCE != null &&
                ScaffoldModule.INSTANCE.isEnabled();
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null) {
            return;
        }


        if (isChestOpen()) {
            targetChest = null;
            return;
        }

        if (AuraModule.target != null) {
            targetChest = null;
            return;
        }

        if (isScaffolding()) {
            targetChest = null;
            return;
        }

        if (shouldWaitForAura()) {
            targetChest = null;
            return;
        }

        if (isRiding()) {
            targetChest = null;
            return;
        }

        BlockPos playerPos = mc.player.blockPosition();
        int rangeint = range.getValue().intValue();

        targetChest = findChests(playerPos, rangeint);

        if (!isValidTarget(targetChest)) {
            targetChest = null;
            return;
        }

        if (!rotate.getValue()) {
            openChest(targetChest);
        }
    }

    @EventHook
    public void onRotation(PlayerRotationEvent event) {
        if (!isValidTarget(targetChest)) return;
        if (shouldWaitForAura()) return;
        if (isScaffolding()) return;
        if (AuraModule.target != null) return;
        if (isRiding()) return;
        if (openedChests.contains(targetChest)) return;

        Vec3 chestVec = new Vec3(
                targetChest.getX() + 0.5,
                targetChest.getY() + 0.5,
                targetChest.getZ() + 0.5
        );

        float[] targetRot = RotationUtils.getRotationsToBlock(
                mc.player.getEyePosition(),
                targetChest,
                Direction.UP
        );

        if (targetRot == null) return;

        // meow
        RotationUtils.setRotationSpeed(rotationSpeed.getValue());

        event.yaw = targetRot[0];
        event.pitch = targetRot[1];

        HitResult hit = PlayerUtils.raycast(
                targetRot[0],
                targetRot[1],
                mc.player.getEyePosition().distanceTo(chestVec),
                false
        );

        if (!(hit instanceof BlockHitResult blockHit)) return;
        if (!blockHit.getBlockPos().equals(targetChest)) return;

        openChest(targetChest);
    }

    private BlockPos findChests(BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            if (!openedChests.contains(pos) &&
                    mc.level.getBlockState(pos).getBlock() instanceof ChestBlock) {
                return pos;
            }
        }
        return null;
    }

    private void openChest(BlockPos chestPos) {
        if (isScaffolding() || isRiding() || !isValidTarget(chestPos)) return;

        Vec3 hitPos = new Vec3(
                chestPos.getX() + 0.5,
                chestPos.getY() + 0.5,
                chestPos.getZ() + 0.5
        );

        BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, chestPos, false);

        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);

        openedChests.add(chestPos);
        targetChest = null;
    }

    @Override
    public void onDisable() {
        openedChests.clear();
        targetChest = null;
    }

    @EventHook
    public void onWorldChange(WorldJoinEvent event) {
        openedChests.clear();
        targetChest = null;
    }

    @EventHook
    public void onRender(Render3DEvent event) {
        if (mc.player == null || mc.level == null || !visuals.getValue()) {
            return;
        }

        BlockPos playerPos = mc.player.blockPosition();
        int rangeInt = visualRange.getValue().intValue();

        Set<BlockPos> renderedChests = new HashSet<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-rangeInt, -rangeInt, -rangeInt),
                playerPos.offset(rangeInt, rangeInt, rangeInt))) {

            if (renderedChests.contains(pos)) {
                continue;
            }

            if (!(mc.level.getBlockState(pos).getBlock() instanceof ChestBlock)) {
                continue;
            }

            BlockState state = mc.level.getBlockState(pos);
            ChestType chestType = state.getValue(ChestBlock.TYPE);

            Color color = openedChests.contains(pos)
                    ? ColorUtils.getClientTheme()
                    : Color.RED;

            switch (renderMode.getValue()) {
                case Filled -> RenderUtils.renderBlock(pos, event, color);

                case Outline -> RenderUtils.renderBlockOutline(pos, event, color);
            }

            if (chestType != ChestType.SINGLE) {
                Direction facing = state.getValue(ChestBlock.FACING);

                BlockPos secondPos = pos.relative(
                        chestType == ChestType.LEFT
                                ? facing.getClockWise()
                                : facing.getCounterClockWise()
                );

                if (mc.level.getBlockState(secondPos).getBlock() instanceof ChestBlock) {
                    switch (renderMode.getValue()) {
                        case Filled -> RenderUtils.renderBlock(secondPos, event, color);

                        case Outline -> RenderUtils.renderBlockOutline(secondPos,
                                event, color);
                    }

                    renderedChests.add(secondPos);
                }
            }

            renderedChests.add(pos);
        }
    }

    private boolean isValidTarget(BlockPos pos) {
        return pos != null && mc.level != null && !openedChests.contains(pos) &&
                mc.level.getBlockState(pos).getBlock() instanceof ChestBlock;
    }

    private boolean isChestOpen() {
        return mc.screen instanceof ContainerScreen;
    }
}
