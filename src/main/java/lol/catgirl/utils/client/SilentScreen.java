package lol.catgirl.utils.client;

import com.mojang.blaze3d.platform.InputConstants;
import lol.catgirl.utils.IMinecraft;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class SilentScreen extends Screen implements IMinecraft {

    @Getter
    private final Screen wrapped;

    public SilentScreen(Screen wrapped) {
        super(wrapped.getTitle());
        this.wrapped = wrapped;
    }

    @Override
    protected void init() {
        if (minecraft != null) {
            wrapped.init(width, height);
        }
    }

    @Override
    public void added() {
        wrapped.added();

        if (mc != null) {
            mc.execute(() -> {
                if (mc.screen == this) {
                    mc.mouseHandler.grabMouse();
                }
            });
        }
    }

    @Override
    public void removed() {
        wrapped.removed();
        resyncMovementKeys();
    }

    private void resyncMovementKeys() {
        Minecraft mc = minecraft;
        if (mc == null) return;

        Options opts = mc.options;

        KeyMapping[] movementKeys = {
                opts.keyUp,
                opts.keyDown,
                opts.keyLeft,
                opts.keyRight,
                opts.keyJump,
                opts.keyShift,
                opts.keySprint
        };

        for (KeyMapping key : movementKeys) {
            KeyMapping.set(
                    InputConstants.getKey(key.saveString()),
                    key.isDown()
            );
        }
    }

    @Override
    public boolean isPauseScreen() {
        return wrapped.isPauseScreen();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return wrapped.shouldCloseOnEsc();
    }

    @Override
    public void onClose() {
        wrapped.onClose();
    }

    @Override
    public void tick() {
        wrapped.tick();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return wrapped.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return wrapped.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return wrapped.charTyped(event);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick ) {

    }
}