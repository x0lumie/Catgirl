package lol.catgirl.module.player;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.StartUseItemEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class GhostHandModule extends Module {
    public static final GhostHandModule INSTANCE = new GhostHandModule();

    private final Set<BlockPos> posList = new ObjectOpenHashSet<>();

    public GhostHandModule() {
        super("GhostHand", "Allows you to open containers through walls.", ModuleCategory.Player);
    }

    @EventHook
    public void onTick(StartUseItemEvent event) {
        if(!mc.options.keyUse.isDown() || mc.player.isShiftKeyDown()) return;

        if (mc.level.getBlockState(BlockPos.containing(mc.player.pick(mc.player.blockInteractionRange(), mc.getDeltaTracker().getGameTimeDeltaPartialTick(true), false).getLocation())).hasBlockEntity()) {
            return;
        }

        Vec3 direction = new Vec3(0, 0, 0.1)
                .xRot(-(float) Math.toRadians(mc.player.getXRot()))
                .yRot(-(float) Math.toRadians(mc.player.getYRot()));

        posList.clear();

        for (int i = 1; i < mc.player.blockInteractionRange() * 10; i++) {
            BlockPos pos = BlockPos.containing(mc.player.getEyePosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)).add(direction.scale(i)));
            if(posList.contains(pos)) continue;;
            posList.add(pos);

            if (mc.level.getBlockState(pos).hasBlockEntity()) {
                for (InteractionHand hand : InteractionHand.values()) {
                    InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));
                    if (result instanceof InteractionResult.Success || result instanceof InteractionResult.Fail) {
                        mc.player.swing(hand);
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
