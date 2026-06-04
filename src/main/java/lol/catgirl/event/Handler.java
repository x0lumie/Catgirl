package lol.catgirl.event;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PostTickEvent;
import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.keybind.KeybindRegistry;
import lol.catgirl.utils.keybind.KeybindState;
import lol.catgirl.utils.keybind.Keybindable;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

import java.awt.*;

// I KNOW OKAY. THIS IS RETARDED BUT
// it works.

public class Handler implements IMinecraft {
    public static final Identifier RENDER_IDENTIFIER = Identifier.
            fromNamespaceAndPath("catgirl", "meow");

    public static void initialize() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if(client.player == null) return;
            Catgirl.INSTANCE.eventBus.post(new ClientTickEvent());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (mc.screen != null) return;

            Catgirl.INSTANCE.eventBus.post(new PostTickEvent());

            for (Keybindable keybindable : KeybindRegistry.getKeybindables()) {

                int key = keybindable.getKey();
                if (key <= 0) continue;

                boolean pressed = org.lwjgl.glfw.GLFW.glfwGetKey(
                        mc.getWindow().handle(),
                        key
                ) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

                if (pressed && !KeybindState.wasPressed(keybindable.getKeybindId())) {
                    keybindable.onBindPress();
                }

                KeybindState.setPressed(keybindable.getKeybindId(), pressed);
            }
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, RENDER_IDENTIFIER, (drawContext, tickDelta) -> {

            int scaledWidth = mc.getWindow().getGuiScaledWidth();
            int scaledHeight = mc.getWindow().getGuiScaledHeight();
            double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getWidth();
            double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getHeight();

            Catgirl.INSTANCE.eventBus.post(new Render2DEvent(drawContext, tickDelta,
                    scaledWidth, scaledHeight, mouseX, mouseY,
                    mc.getDeltaTracker().getRealtimeDeltaTicks()));
        });
    }

    @EventHook
    public void onRenderTick(RenderTickEvent event) {
        int x = 1;
        int y = 1;
            DrawUtil.drawString(
                    "a",
                    x + x + 2 + x + 25, y,
                    1, Color.WHITE,
                    ResourceManager.FontResources.roboto
            );
        }
}
