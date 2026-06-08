package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.SliderProperty;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.phys.Vec3;

public final class FastLadderModule extends Module {
    public static final FastLadderModule INSTANCE = new FastLadderModule();

    public FastLadderModule() {
        super("FastLadder", "Allows you to go up ladders faster.", ModuleCategory.Movement);
        addSettings(speed);
    }

    public final SliderProperty speed = new SliderProperty("Speed", 0.42f, 0.1f, 1.0f, 0.01f);

    @EventHook
    private void onTick(ClientTickEvent event) {
        if (climbing()) {
            Vec3 velocity = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(velocity.x, speed.getValue(), velocity.z);
        }
    }

    private boolean climbing() {
        return mc.player.horizontalCollision
                && (mc.player.onClimbable()
                || (mc.player.getInBlockState().is(Blocks.POWDER_SNOW)
                && PowderSnowBlock.canEntityWalkOnPowderSnow(mc.player)));
    }
}
