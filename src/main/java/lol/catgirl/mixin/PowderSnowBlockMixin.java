package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.module.movement.AntiPowderSnowModule;
import lol.catgirl.utils.IMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.PowderSnowBlock;

@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin extends Block implements BucketPickup
{
	private PowderSnowBlockMixin(Catgirl catgirl, Properties settings) {
		super(settings);
	}
	
	@Inject(
		method = "canEntityWalkOnPowderSnow(Lnet/minecraft/world/entity/Entity;)Z",
		at = @At("HEAD"),
		cancellable = true)
	private static void onCanWalkOnPowderSnow(Entity entity,
		CallbackInfoReturnable<Boolean> cir)
	{
		if(!AntiPowderSnowModule.INSTANCE.isEnabled()) {
			return;
		}
		
		if(entity == IMinecraft.mc.player) {
			cir.setReturnValue(true);
		}
	}
}