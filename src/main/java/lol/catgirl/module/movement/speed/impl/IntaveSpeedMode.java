package lol.catgirl.module.movement.speed.impl;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.movement.speed.SpeedMode;
import lol.catgirl.utils.client.GameTimer;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.world.phys.Vec3;

public final class IntaveSpeedMode implements SpeedMode {
    private int airTicks, groundTicks;

    @Override
    public void onTick(ClientTickEvent event) {

        if (mc.player.onGround()) {
            airTicks = 0;
            groundTicks++;
        } else {
            groundTicks = 0;
            airTicks++;
        }

        if (mc.player.onGround() && MoveUtils.isMoving()) {
            PlayerUtils.jump();
        }

        Vec3 motion = mc.player.getDeltaMovement();

        switch (airTicks) {
            case 1 -> mc.player.setDeltaMovement(motion.x * 1.005,
                    motion.y, motion.z * 1.005);
            case 2, 3, 4, 5, 6 -> mc.player.setDeltaMovement(motion.x *
                    1.011, motion.y, motion.z * 1.011);
        }

        if (groundTicks == 1) {
            motion = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(motion.x * 1.0045, motion.y, motion.z * 1.0045);
        }

        GameTimer.setSpeed(1.0075f);
    }


    @Override
    public void onDisable() {
        GameTimer.reset();

        airTicks = 0;
        groundTicks = 0;
    }
}
