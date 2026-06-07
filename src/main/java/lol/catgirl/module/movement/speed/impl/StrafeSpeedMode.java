package lol.catgirl.module.movement.speed.impl;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.movement.speed.SpeedMode;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;

public final class StrafeSpeedMode implements SpeedMode {
    @Override
    public void onTick(ClientTickEvent event) {
        MoveUtils.strafe();

        if (MoveUtils.isMoving() && mc.player.onGround()) {
            PlayerUtils.jump();
        }
    }
}
