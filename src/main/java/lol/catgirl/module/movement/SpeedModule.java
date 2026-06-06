package lol.catgirl.module.movement;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PlayerJumpEvent;
import lol.catgirl.event.impl.PlayerJumpFactorEvent;
import lol.catgirl.event.impl.PlayerStrafeEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.player.ScaffoldModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.GameTimer;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.world.phys.Vec3;

public final class SpeedModule extends Module {
    public static final SpeedModule INSTANCE = new SpeedModule();

    private int polarGoyer = 0;

    public enum Mode {
        Legit,
        LegitExploit,
        Intave,
        Matrix,
        Polar
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Intave);

    public final BoolProperty matrixLowHop = new BoolProperty("Low Hop", false).hide(()-> !(mode.getValue() == Mode.Matrix));
    public final SliderProperty matrixGroundBoost = new SliderProperty("Ground Boost", 0.2f, 0f, 0.5f, 0.01f).hide(() -> !(mode.getValue() == Mode.Matrix));

    public SpeedModule() {
        super("Speed", "Allows you to go faster!!!", ModuleCategory.Movement);
        addSettings(mode, matrixLowHop, matrixGroundBoost);
    }

    private int airTicks, groundTicks;

    @EventHook
    public void onTick(ClientTickEvent event) {
        if(mc.player == null) return;

        if (mc.player.onGround()) {
            airTicks = 0;
            groundTicks++;
        } else {
            groundTicks = 0;
            airTicks++;
        }

        switch (mode.getValue()) {
            case Legit -> {
                if (MoveUtils.isMoving() && mc.player.onGround()) {
                    mc.player.jumpFromGround();
                }
            }

            case Polar -> {
                GameTimer.setSpeed(1.009f); // yo.
            }

            case LegitExploit -> {
                if (MoveUtils.isMoving() && mc.player.onGround()) {
                    PlayerUtils.jump();
                }

                GameTimer.setSpeed(1.0075f);
            }

            case Matrix -> {
                if (mc.player.isInWater() || mc.player.isInLava()
                        || PlayerUtils.isInWeb() || mc.player.onClimbable()) return;
//                GameTimer.setSpeed(1.0075f);

                if (MoveUtils.isMoving()) {
                    if (mc.player.onGround()) {
                        double speed = MoveUtils.getBaseMoveSpeed();
                        MoveUtils.strafe(speed);
                        mc.player.setDeltaMovement(mc.player.getDeltaMovement().x,
                                0.42 - (matrixLowHop.getValue() ? 3.48E-3 : 0.0),
                                mc.player.getDeltaMovement().z);
                    } else {
                        if (MoveUtils.getSpeed() < 0.10) {
                            MoveUtils.strafe();
                        }
                    }
                }
            }

            case Intave -> {
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
        }
    }

    @EventHook
    public void onJumpFactor(PlayerJumpFactorEvent event) {
        if (mc.player == null) return;

        if (mode.getValue() == Mode.Matrix && matrixLowHop.getValue()
                && MoveUtils.isMoving()) {
            event.factor = 0.026f;
        }
    }

    @EventHook
    public void onStrafe(PlayerStrafeEvent event) {
        if(mc.player == null) return;

        if(mode.getValue() == Mode.Polar) {
            if (mc.player.getId() == 1) {}

            if (mc.player.getId() == 5 && polarGoyer % 2 != 0) {
                var a = mc.player.getDeltaMovement().y;
                a -= 0.03;
                MoveUtils.setMotionY(a);
            }

            if (mc.player.onGround()) {
                PlayerUtils.jump();
            }

            if (polarGoyer % 2 != 0) {
                Catgirl.sendChatMessage(String.valueOf(mc.player.getId()));
            }

            MoveUtils.moveFlying(0.002);
        }
    }

    @EventHook
    public void onJump(PlayerJumpEvent event) {
        if(mode.getValue() == Mode.Polar) {
            polarGoyer++;
        }
    }

    @Override
    public void onDisable() {
        GameTimer.reset();

        airTicks = 0;
        groundTicks = 0;
        super.onDisable();
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}