package lol.catgirl.mixin;

import lol.catgirl.module.hud.HotbarModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceBarRenderer.class)
public class ExperianceBarMixin {

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void disableXpBar(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        var hotbar = HotbarModule.INSTANCE;
        if(hotbar != null && hotbar.isEnabled()) {
            ci.cancel();
        }
    }
}