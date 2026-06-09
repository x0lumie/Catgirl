package lol.catgirl.mixin;

import lol.catgirl.module.client.InterfaceModule;
import lol.catgirl.ui.CustomMainMenu;
import lol.catgirl.utils.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class TitleScreenMixin implements IMinecraft {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void replaceTitleScreen(Screen screen, CallbackInfo ci) {
        InterfaceModule module = InterfaceModule.INSTANCE;
        if(!module.customMainMenu.getValue()) {
            return;
        }

            if (screen instanceof TitleScreen || (screen == null && mc.level == null)) {
                mc.setScreen(new CustomMainMenu());
                ci.cancel();
            }
        }

}