package lol.catgirl.module.render;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.WorldUtils;
import lol.catgirl.utils.render.RenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;

import java.awt.*;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class StorageESPModule extends Module {

    public static final StorageESPModule INSTANCE = new StorageESPModule();

    public final SliderProperty range =
            new SliderProperty("Range", 64.0f, 1f, 256f, 1f);
    public final SliderProperty alpha =
            new SliderProperty("Alpha", 255, 0, 255, 1);
    //public final BoolSetting tracers = new BoolSetting("Tracers", true);

    public final BoolProperty chests = new BoolProperty("Chests", true);
    public final BoolProperty trappedChests = new BoolProperty("Trapped Chests", true);
    public final BoolProperty enderChests = new BoolProperty("Ender Chests", true);
    public final BoolProperty shulkers = new BoolProperty("Shulkers", true);
    public final BoolProperty barrels = new BoolProperty("Barrels", true);

    public final BoolProperty furnaces = new BoolProperty("Furnaces", true);
    public final BoolProperty blastSmoker = new BoolProperty("Blast/smoker", true);
    public final BoolProperty hoppers = new BoolProperty("Hoppers", true);
    public final BoolProperty enchanting = new BoolProperty("Enchanting", true);

    public final BoolProperty spawners = new BoolProperty("Spawners", true);
    public final BoolProperty trialSpawners = new BoolProperty("Trial spawners", true);
    public final BoolProperty vaults = new BoolProperty("Vaults", true);

    public StorageESPModule() {
        super("StorageESP",
                "Shows custom boxes around storage containers.",
                ModuleCategory.Render
        );

        addSettings(
                range,
                alpha,
                chests,
                trappedChests,
                enderChests,
                shulkers,
                barrels,
                furnaces,
                blastSmoker,
                hoppers,
                enchanting,
                spawners,
                trialSpawners,
                vaults
        );
    }

    @EventHook
    public void onRenderWorld(Render3DEvent event) {
        if (mc.level == null || mc.player == null)
            return;

        double rangeSq = range.getValue() * range.getValue();

        WorldUtils.getLoadedBlockEntities().forEach(be -> {

            if (be == null)
                return;

            BlockPos pos = be.getBlockPos();

            if (mc.player.distanceToSqr(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5
            ) > rangeSq)
                return;

            Color color = getColor(be);

            if (color == null)
                return;

            RenderUtils.renderBlock(pos, event, color);

//            if (tracers.getValue()) {
//                Vec3 blockCenter = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
//                RenderUtil.renderTracers(blockCenter, color, event);
//
//            }
        });
    }

    private Color getColor(BlockEntity be) {
        int a = alpha.getValue().intValue();

        if (chests.getValue() && be instanceof ChestBlockEntity)
            return new Color(145, 95, 45, a);

        if (trappedChests.getValue() && be instanceof TrappedChestBlockEntity)
            return new Color(180, 70, 45, a);

        if (enderChests.getValue() && be instanceof EnderChestBlockEntity)
            return new Color(70, 0, 120, a);

        if (shulkers.getValue() && be instanceof ShulkerBoxBlockEntity)
            return new Color(122, 73, 178, a);

        if (barrels.getValue() && be instanceof BarrelBlockEntity)
            return new Color(125, 85, 55, a);

        if (furnaces.getValue() && be instanceof FurnaceBlockEntity)
            return new Color(110, 110, 110, a);

        if (blastSmoker.getValue()) {

            if (be instanceof BlastFurnaceBlockEntity)
                return new Color(70, 70, 70, a);

            if (be instanceof SmokerBlockEntity)
                return new Color(100, 90, 75, a);
        }

        if (hoppers.getValue() && be instanceof HopperBlockEntity)
            return new Color(55, 55, 55, a);

        if (enchanting.getValue() && be instanceof EnchantingTableBlockEntity)
            return new Color(60, 120, 255, a);

        if (spawners.getValue() && be instanceof SpawnerBlockEntity)
            return new Color(90, 90, 90, a);

        if (trialSpawners.getValue() && be instanceof TrialSpawnerBlockEntity)
            return new Color(0, 190, 170, a);

        if (vaults.getValue() && be instanceof VaultBlockEntity)
            return new Color(212, 175, 55, a);

        return null;
    }
}