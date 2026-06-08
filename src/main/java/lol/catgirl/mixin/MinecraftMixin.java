package lol.catgirl.mixin;

import com.mojang.blaze3d.platform.Window;
import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.OpenScreenEvent;
import lol.catgirl.event.impl.StartUseItemEvent;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {


    @Inject(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/client/Minecraft;)Lnet/minecraft/client/gui/Gui;",
                    shift = At.Shift.AFTER
            )
    )
    private void initializeNanoVG(GameConfig args, CallbackInfo ci) {
        DrawUtil.init();
    }

    @Shadow
    @Final
    private Window window;


    @Inject(method = "<init>", at = @At("RETURN"))
    public void initImGui(GameConfig args, CallbackInfo ci) {
        lol.catgirl.ui.click.imgui.ImGuiImpl.initialize(window.handle());
    }


    @ModifyArg(
            method = "updateTitle",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/Window;setTitle(Ljava/lang/String;)V"
            ))
    private String setTitle(String original) {
        return Catgirl.windowTitle;
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {

        OpenScreenEvent event = new OpenScreenEvent(screen);
        Catgirl.INSTANCE.eventBus.post(event);

        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionHand;values()[Lnet/minecraft/world/InteractionHand;"), cancellable = true)
    private void onStartUseItemBeforeHands(CallbackInfo ci) {
        StartUseItemEvent event = new StartUseItemEvent();
        Catgirl.INSTANCE.eventBus.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
