package lol.catgirl.module.client;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.ModuleCategory;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import lol.catgirl.module.Module;

public final class MCPModule extends Module {

    public static final MCPModule INSTANCE = new MCPModule();

    public MCPModule() {
        super("MCP",
                "Allows you to press the mid click to throw ender pearls.",
                ModuleCategory.Client
        );
    }

    private boolean wasPressed = false;
    private int previousSlot = -1;
    private long lastThrowTime;

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null || mc.level == null || !this.isEnabled()) {
            return;
        }

        boolean pressed = GLFW.glfwGetMouseButton(mc.getWindow().handle(),
                GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == 1;

        if (pressed && !wasPressed) {

            if (System.currentTimeMillis() - lastThrowTime < 300) {
                wasPressed = pressed;
                return;
            }

            int pearlSlot = findPearl();

            if (pearlSlot != -1) {
                previousSlot = mc.player.getInventory().getSelectedSlot();

                mc.player.getInventory().setSelectedSlot(pearlSlot);
                mc.gameMode.useItem(mc.player, mc.player.getUsedItemHand());
                mc.player.swing(mc.player.getUsedItemHand());
                mc.player.getInventory().setSelectedSlot(previousSlot);

                lastThrowTime = System.currentTimeMillis();
            }
        }

        wasPressed = pressed;
    }

    private int findPearl() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }
}