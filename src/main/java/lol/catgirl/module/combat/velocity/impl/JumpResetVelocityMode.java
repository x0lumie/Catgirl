package lol.catgirl.module.combat.velocity.impl;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.combat.VelocityModule;
import lol.catgirl.module.combat.velocity.VelocityMode;
import lol.catgirl.utils.player.PlayerUtils;

import java.util.concurrent.ThreadLocalRandom;

public final class JumpResetVelocityMode implements VelocityMode {
    public final VelocityModule module;

    public JumpResetVelocityMode(VelocityModule module) {
        this.module = module;
    }

    private boolean hitProcessed;
    private int jumpTicks;

    @Override
    public void onTick(ClientTickEvent event) {

        if (mc.player.hurtTime > 0) {
            if (!hitProcessed) {
                if (module.polar.getValue()) {
                    if (ThreadLocalRandom.current().nextDouble() <= 0.75D) {
                        jumpTicks = ThreadLocalRandom.current().nextInt(0, 5);
                    }
                } else {
                    jumpTicks = 0;
                }
                hitProcessed = true;
            }
        } else {
            hitProcessed = false;
        }

        if (jumpTicks >= 0) {
            if (jumpTicks == 0) {
                if (mc.player.onGround()) {
                    PlayerUtils.jump();
                }
                jumpTicks = -1;
            } else {
                jumpTicks--;
            }
        }
    }
}
