package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import net.minecraft.world.phys.Vec3;

public final class SafeWalkModule extends Module {
    public static final SafeWalkModule INSTANCE = new SafeWalkModule();

    public SafeWalkModule() {
        super("SafeWalk", "Prevents you from walking off block edges.", ModuleCategory.Movement);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null) return;

        double x = mc.player.getDeltaMovement().x;
        double y = mc.player.getDeltaMovement().y;
        double z = mc.player.getDeltaMovement().z;
        if (mc.player.onGround()) {
            double increment;
            for (increment = 0.05D; x != 0.0D;) {
                if (x < increment && x >= -increment) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= increment;
                } else {
                    x += increment;
                }
            }
            while (z != 0.0D)
            {
                if (z < increment && z >= -increment) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= increment;
                } else {
                    z += increment;
                }
            }
            while (x != 0.0D && z != 0.0D)
            {
                if (x < increment && x >= -increment) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= increment;
                } else {
                    x += increment;
                }
                if (z < increment && z >= -increment) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= increment;
                } else {
                    z += increment;
                }
            }
        }
        mc.player.setDeltaMovement(new Vec3(x, y, z));
    }
}
