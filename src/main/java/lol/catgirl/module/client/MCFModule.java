package lol.catgirl.module.client;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.event.impl.WorldJoinEvent;
import lol.catgirl.manager.FriendManager;
import lol.catgirl.property.impl.BoolProperty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;


public final class MCFModule extends Module {

    public static final MCFModule INSTANCE = new MCFModule();

    public final BoolProperty clearOnWorldChange = new BoolProperty("Clear on world change", false);

    public MCFModule() {
        super("MCF", "Allows you to press the mid click to friend players.",
                ModuleCategory.Client
        );
        addSetting(clearOnWorldChange);
    }

    private boolean wasPressed = false;

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if(mc.player == null || mc.level == null) {
            return;
        }

        if(!this.isEnabled()) return;

        boolean pressed = GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == 1;

        if(pressed && !wasPressed) {
            if(mc.hitResult instanceof EntityHitResult hitResult) {
                Entity entity = hitResult.getEntity();

                if (entity instanceof Player player) {
                    String name = player.getName().getString();

                    if (FriendManager.isFriend(name)) {
                        FriendManager.remove(name);
                        Catgirl.sendChatMessage("Removed " + name + " as a friend.");
                    } else {
                        FriendManager.add(name);
                        Catgirl.sendChatMessage("Added " + name + " as a friend.");
                    }
                }
            }
        }
        wasPressed = pressed;
    }

    @EventHook
    public void onWorldJoin(WorldJoinEvent event) {
        if(!this.isEnabled()) return;
        if(!clearOnWorldChange.getValue()) return;

        FriendManager.clear();
        int friends = FriendManager.getFriends().size();

        Catgirl.sendChatMessage("Cleared " + friends + " friends on world change. ");
    }
}
