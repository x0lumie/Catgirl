package lol.catgirl.module.client;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.event.impl.WorldJoinEvent;
import lol.catgirl.manager.FriendManager;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.ui.notification.Notification;
import lol.catgirl.ui.notification.NotificationManager;
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
//                        Catgirl.sendChatMessage("Removed " + name + " as a friend.");

                        switch (NotificationsModule.INSTANCE.mode.getValue()) {
                            case Chat -> {
                                Catgirl.sendChatMessage(this.getDisplayName() + " Removed " + name + " as a friend.");
                            }
                            case Exhibition -> {
                                NotificationManager.post(this.getDisplayName(), "Removed " + name + " as a friend.", Notification.Type.OKAY);
                            }
                            case None -> {}
                        }

                    } else {
                        FriendManager.add(name);
                    switch (NotificationsModule.INSTANCE.mode.getValue()) {
                            case Chat -> {
                                Catgirl.sendChatMessage(this.getDisplayName() + " Added " + name + " as a friend.");
                            }
                            case Exhibition -> {
                                NotificationManager.post(this.getDisplayName(), "Added " + name + " as a friend.", Notification.Type.OKAY);
                            }
                            case None -> {}
                        }
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
