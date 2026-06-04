package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
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

    @ModifyArg(
            method = "updateTitle",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/Window;setTitle(Ljava/lang/String;)V"
            ))
    private String setTitle(String original) {
        return Catgirl.windowTitle;
    }

}
