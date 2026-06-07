package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.module.hud.HotbarModule;
import lol.catgirl.module.render.NoRenderModule;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

	@Shadow
	private org.apache.commons.lang3.tuple.Pair<Gui.ContextualInfo, net.minecraft.client.gui.contextualbar.ContextualBarRenderer> contextualInfoBar;

	@Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"), cancellable = true)
	private void cancelScoreboard(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
		NoRenderModule noRender = NoRenderModule.INSTANCE;
		if (noRender != null && noRender.isEnabled() && noRender.scoreboard.getValue()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
	private void onRenderStatusEffectOverlay(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
		if (NoRenderModule.INSTANCE.activeEffects.getValue()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
	private void renderHotbar(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo info) {
		var hotbarModule = HotbarModule.INSTANCE;
		if (hotbarModule != null && hotbarModule.isEnabled()) {
			info.cancel();
		}
	}

	@Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"))
	private void interceptContextualBarState(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
		var hotbarModule = HotbarModule.INSTANCE;
		if (hotbarModule != null && hotbarModule.isEnabled()) {
			this.contextualInfoBar = org.apache.commons.lang3.tuple.Pair.of(
					Gui.ContextualInfo.EMPTY,
					net.minecraft.client.gui.contextualbar.ContextualBarRenderer.EMPTY
			);
		}
	}

	@Inject(at = @At("TAIL"), method = "renderHotbarAndDecorations")
	void renderTick(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
		RenderTickEvent event = new RenderTickEvent(tickCounter.getGameTimeDeltaPartialTick(false), context);
		Catgirl.INSTANCE.eventBus.post(event);
	}
}