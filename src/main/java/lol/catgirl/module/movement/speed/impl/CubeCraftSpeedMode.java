package lol.catgirl.module.movement.speed.impl;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.movement.SpeedModule;
import lol.catgirl.module.movement.speed.SpeedMode;
import lol.catgirl.utils.player.MoveUtils;

import java.util.Random;

public class CubeCraftSpeedMode implements SpeedMode {
    private final SpeedModule module;

    public CubeCraftSpeedMode(SpeedModule module) {
        this.module = module;
    }

    private final int LOW_HOP_TICK = 2;
    private final float LOW_HOP_DOWN_VEL = 0.08f;
    private final float SPEED_AMPLIFIER = 0.3f;

    private static final Random RANDOM = new Random();

    @Override
    public void onTick(ClientTickEvent event) {
        if (!MoveUtils.isMoving())
            return;

        if (mc.player == null)
            return;

        if (mc.player.tickCount <= 11)
            return;

        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
        }

        if (module.cubeCraftLowHop.getValue()
                && !mc.player.horizontalCollision
                && !mc.options.keyJump.isDown()
                && mc.player.fallDistance == LOW_HOP_TICK) {

            MoveUtils.setMotionY(-LOW_HOP_DOWN_VEL);
        }

        double base = 0.321 + (0.342 - 0.321) * RANDOM.nextDouble();
        double speedFromJump = base + SPEED_AMPLIFIER;
        double baseSpeed = (mc.player.tickCount % 2 == 0)
                ? 0.32
                : 0.2999999999999999;

        double speedFromMove = baseSpeed + SPEED_AMPLIFIER;

        double finalSpeed = Math.max(speedFromJump, speedFromMove);

        MoveUtils.setSpeedWithFixedDirection(finalSpeed);
    }
}
