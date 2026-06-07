package lol.catgirl.module.render;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.render.RenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class BlockOverlayModule extends Module {
    public static final BlockOverlayModule INSTANCE = new BlockOverlayModule();

    public enum Mode {
        Outline,
        Filled,
        Both
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Both);


    public BlockOverlayModule() {
        super("BlockOverlay", "Shows a box around blocks.",
                ModuleCategory.Render);
        addSettings(mode);
    }

    @EventHook
    public void onRender(Render3DEvent event) {
        if(!this.isEnabled() || mc.player == null || mc.level == null) {
            return;
        }

        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = ((BlockHitResult)mc.hitResult).getBlockPos();

        switch (mode.getValue()) {
            case Filled -> {
                RenderUtils.renderBlock(pos, event, ColorUtils.getClientTheme());
            }

            case Outline -> {
                RenderUtils.renderBlockOutline(pos, event, ColorUtils.getClientTheme());
            }

            case Both -> {
                RenderUtils.renderBlock(pos, event, ColorUtils.getClientTheme());
                RenderUtils.renderBlockOutline(pos, event, ColorUtils.getClientTheme().darker().darker().darker());
            }
        }
    }
}
