package lol.catgirl.mixin;

import lol.catgirl.utils.client.GameTimer;
import net.minecraft.client.DeltaTracker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeltaTracker.Timer.class)
public class DeltaTrackerTimerMixin {

    @Shadow
    private float deltaTicks;

    @Inject(
            method = "advanceGameTime(J)I",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/DeltaTracker$Timer;lastMs:J",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0
            )
    )
    public void onBeginRenderTick(long time, CallbackInfoReturnable<Integer> cir) {
        float speed = GameTimer.getSpeed();

        if (speed != 1.0f) {
            deltaTicks *= speed;
        }
    }
}