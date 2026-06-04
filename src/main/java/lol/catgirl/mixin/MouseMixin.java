package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.MouseEvent;
import lol.catgirl.utils.IMinecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin implements IMinecraft {

    @Inject(
            method = "onButton",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onMouseClickPre(long window, MouseButtonInfo info, int action, CallbackInfo ci) {

    }

    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void onUpdateMouse(CallbackInfo ci) {
        MouseEvent event = new MouseEvent(this.accumulatedDX, this.accumulatedDY);
        if (mc.player != null) {
            Catgirl.INSTANCE.eventBus.post(event);
        }
        this.accumulatedDX = event.getCursorDeltaX();
        this.accumulatedDY = event.getCursorDeltaY();
    }
}

