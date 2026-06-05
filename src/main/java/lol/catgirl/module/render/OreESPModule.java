package lol.catgirl.module.render;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.Module;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;

import lol.catgirl.utils.client.WorldUtils;
import lol.catgirl.utils.render.RenderUtils;
import net.minecraft.core.BlockPos;

import java.awt.*;
import java.util.List;

public final class OreESPModule extends Module {

    public static final OreESPModule INSTANCE = new OreESPModule();

    public final SliderProperty range =
            new SliderProperty("Range", 20f, 1f, 128f, 1f);

    public final BoolProperty diamond = new BoolProperty("Diamond", true);
    public final BoolProperty iron = new BoolProperty("Iron", true);
    public final BoolProperty gold = new BoolProperty("Gold", true);
    public final BoolProperty coal = new BoolProperty("Coal", true);
    public final BoolProperty lapis = new BoolProperty("Lapis", true);
    public final BoolProperty emerald = new BoolProperty("Emerald", true);
    public final BoolProperty ancientDebris = new BoolProperty("Ancient Debris", true);

    private List<BlockPos> cachedOres = List.of();
    private int tick;

    public OreESPModule() {
        super("OreESP",
                "Shows custom boxes around ores.",
                ModuleCategory.Render
        );

        addSettings(
                range, diamond, iron, gold, coal, lapis,
                emerald, ancientDebris
        );
    }

    @EventHook
    public void onRender(Render3DEvent event) {
        if (mc.level == null || mc.player == null)
            return;

        tick++;

        if (tick % 10 == 0) {
            cachedOres = WorldUtils.findAllOres(
                    mc.player.blockPosition(),
                    range.getValue().intValue()
            );
        }

        for (BlockPos pos : cachedOres) {
            RenderUtils.renderBlock(pos, event, getColor(pos));
        }
    }

    private Color getColor(BlockPos pos) {
        var state = mc.level.getBlockState(pos);
        var block = state.getBlock();

        int a = 120;

        if (diamond.getValue() &&
                (block == net.minecraft.world.level.block.Blocks.DIAMOND_ORE
                        || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_DIAMOND_ORE))
            return new Color(0, 170, 255, a);

        if (iron.getValue() &&
                (block == net.minecraft.world.level.block.Blocks.IRON_ORE
                        || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_IRON_ORE))
            return new Color(200, 200, 200, a);

        if (gold.getValue() &&
                (block == net.minecraft.world.level.block.Blocks.GOLD_ORE
                        || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_GOLD_ORE))
            return new Color(255, 200, 0, a);

        if (coal.getValue() &&
                (block == net.minecraft.world.level.block.Blocks.COAL_ORE
                        || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_COAL_ORE))
            return new Color(60, 60, 60, a);

        if (lapis.getValue() &&
                (block == net.minecraft.world.level.block.Blocks.LAPIS_ORE
                        || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_LAPIS_ORE))
            return new Color(0, 0, 255, a);

        if (emerald.getValue() &&
                (block == net.minecraft.world.level.block.Blocks.EMERALD_ORE
                        || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_EMERALD_ORE))
            return new Color(0, 255, 120, a);

        if (ancientDebris.getValue() &&
                block == net.minecraft.world.level.block.Blocks.ANCIENT_DEBRIS)
            return new Color(120, 0, 0, a);

        return new Color(0, 0, 0, 0);
    }
}