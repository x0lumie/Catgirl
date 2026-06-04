package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.RenderTickEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class RenderMixin {

    @Inject(at = @At("TAIL"), method = "renderHotbarAndDecorations")
    void renderTick(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        RenderTickEvent event = new RenderTickEvent(
                tickCounter.getGameTimeDeltaPartialTick(false), context);
        Catgirl.INSTANCE.eventBus.post(event);
    }
}
