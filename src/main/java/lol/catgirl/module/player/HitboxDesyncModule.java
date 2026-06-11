package lol.catgirl.module.player;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.WorldJoinEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.Module;
import lol.catgirl.module.client.NotificationsModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.ui.notification.Notification;
import lol.catgirl.ui.notification.NotificationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HitboxDesyncModule extends Module {
    public static final HitboxDesyncModule INSTANCE = new HitboxDesyncModule();

    public HitboxDesyncModule() {
        super("HitboxDesync",
                "Desyncs your hitbox.",
                ModuleCategory.Player
                );
    }

    private static final double MAGICNUMBER = .200009968835369999878673424677777777777761;

    @EventHook
    public void onTick(ClientTickEvent event) {
        if(!this.isEnabled() || mc.player == null) {
            return;
        }

        desync();
    }

    public void desync() {
        Direction facing = mc.player.getDirection();
        AABB bb = mc.player.getBoundingBox();
        Vec3 center = bb.getCenter();
        Vec3 offset = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());

        Vec3 fin = merge(
                Vec3.atCenterOf(BlockPos.containing(center)).add(offset.scale(MAGICNUMBER)),
                facing
        );

        mc.player.setPos(
                fin.x == 0 ? mc.player.getX() : fin.x,
                mc.player.getY(),
                fin.z == 0 ? mc.player.getZ() : fin.z
        );


        toggle();
        switch (NotificationsModule.INSTANCE.mode.getValue()) {
            case Chat -> Catgirl.sendChatMessage(this.getDisplayName() + " Your hitbox has been desynced.");
            case Exhibition -> NotificationManager.post(this.getDisplayName(), "Your hitbox has been desynced.", Notification.Type.NOTIFY);
            case None -> {}
        }
    }

    private Vec3 merge(Vec3 vec, Direction facing) {
        return new Vec3(
                vec.x * Math.abs(facing.getStepX()),
                vec.y * Math.abs(facing.getStepY()),
                vec.z * Math.abs(facing.getStepZ())
        );
    }
}
