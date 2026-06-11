package lol.catgirl.module.combat.velocity.impl;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.combat.VelocityModule;
import lol.catgirl.module.combat.velocity.VelocityMode;
import lol.catgirl.utils.player.MoveUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MineMenClubVelocityMode implements VelocityMode {
    private final VelocityModule module;

    @Override
    public void onTick(ClientTickEvent event) {
        if (mc.player.hurtTime > 1) {
            MoveUtils.setMotionWithoutY(module.mineMenClubValue.getValue().floatValue());
        }

        if (mc.player.onGround() && !mc.options.keyJump.isDown()) {
            mc.player.jumpFromGround();
        }
    }
}
