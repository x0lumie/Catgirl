package lol.catgirl.utils.player;

import lol.catgirl.module.player.ScaffoldModule;
import lol.catgirl.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ScaffoldUtils implements IMinecraft {
    // tenacity just to annoy a person
    public static ScaffoldModule.BlockData getBlockData() {
        final BlockPos belowBlockPos = new BlockPos(mc.player.getBlockX(), ScaffoldModule.sameYPos, mc.player.getBlockZ());

        if (mc.level.getBlockState(belowBlockPos).getBlock() instanceof AirBlock) {
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    for (int i = 1; i > -3; i -= 2) {
                        final BlockPos blockPos = belowBlockPos.offset(x * i, 0, z * i);
                        if (mc.level.getBlockState(blockPos).getBlock() instanceof AirBlock) {
                            for (Direction direction : Direction.values()) {
                                final BlockPos block = blockPos.relative(direction);
                                final BlockState material = mc.level.getBlockState(block).getBlock().defaultBlockState();
                                if (material.isSolid() && !material.canBeReplaced()) {
                                    return new ScaffoldModule.BlockData(block, direction.getOpposite());
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static ScaffoldModule.BlockData getBlockData(int offsetX, int offsetY, int offsetZ) {
        final BlockPos belowBlockPos = new BlockPos(mc.player.getBlockX() + offsetX, ScaffoldModule.sameYPos + offsetY, mc.player.getBlockZ() + offsetZ);

        if (mc.level.getBlockState(belowBlockPos).getBlock() instanceof AirBlock) {
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    for (int i = 1; i > -3; i -= 2) {
                        final BlockPos blockPos = belowBlockPos.offset(x * i, 0, z * i);
                        if (mc.level.getBlockState(blockPos).getBlock() instanceof AirBlock) {
                            for (Direction direction : Direction.values()) {
                                final BlockPos block = blockPos.relative(direction);
                                final BlockState material = mc.level.getBlockState(block).getBlock().defaultBlockState();
                                if (material.isSolid() && !material.canBeReplaced()) {
                                    return new ScaffoldModule.BlockData(block, direction.getOpposite());
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Vec3 getNewVector(ScaffoldModule.BlockData lastblockdata) {
        if (lastblockdata == null) {
            return null;
        }
        BlockPos pos = lastblockdata.getPosition();
        Direction facing = lastblockdata.getFacing();
        Vec3 vec3 = new Vec3(pos.getX(), pos.getY(), pos.getZ());

        double amount1 = 0.45 + Math.random() * 0.1;
        double amount2 = 0.45 + Math.random() * 0.1;

        if (facing == Direction.UP) {
            vec3 = vec3.add(amount1, 1, amount2);
        } else if (facing == Direction.DOWN) {
            vec3 = vec3.add(amount1, 0, amount2);
        } else if (facing == Direction.EAST) {
            vec3 = vec3.add(1, amount1, amount2);
        } else if (facing == Direction.WEST) {
            vec3 = vec3.add(0, amount1, amount2);
        } else if (facing == Direction.NORTH) {
            vec3 = vec3.add(amount1, amount2, 0);
        } else if (facing == Direction.SOUTH) {
            vec3 = vec3.add(amount1, amount2, 1);
        }

        return vec3;
    }
}
