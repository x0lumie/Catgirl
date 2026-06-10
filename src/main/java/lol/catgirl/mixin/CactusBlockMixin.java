package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.module.player.AntiCactusModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(CactusBlock.class)
public abstract class CactusBlockMixin extends Block {

	public CactusBlockMixin(Properties settings) {
		super(settings);
	}

	@Inject(at = { @At("HEAD") }, method = {
			"getCollisionShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;" }, cancellable = true)
	private void onGetCollisionShape(BlockState blockState_1, BlockGetter blockView_1, BlockPos blockPos_1,
			CollisionContext entityContext_1, CallbackInfoReturnable<VoxelShape> cir) {
		if (Catgirl.INSTANCE != null) {
			if (AntiCactusModule.INSTANCE.isEnabled()) {
				cir.setReturnValue(Shapes.block());
			}
		}
	}
}