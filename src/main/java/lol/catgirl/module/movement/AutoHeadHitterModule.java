package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AutoHeadHitterModule extends Module {
    public static final AutoHeadHitterModule INSTANCE = new AutoHeadHitterModule();

    public AutoHeadHitterModule() {
        super("AutoHeadHitter",
                "Auto jumps when there's a solid block above to make u go fast",
                ModuleCategory.Movement
        );
    }

    @EventHook
    private void onTickEvent(ClientTickEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (!mc.player.onGround()) return;

        BlockPos playerPos = mc.player.blockPosition();
        BlockPos headPos = playerPos.above(2);

        BlockState blockState = mc.level.getBlockState(headPos);

        if (!blockState.isAir() && blockState.getBlock() != Blocks.WATER && blockState.getBlock() != Blocks.LAVA) {
            PlayerUtils.jump();
            mc.player.noJumpDelay = 0;
        }
    }
}