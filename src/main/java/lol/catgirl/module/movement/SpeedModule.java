package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.EnumProperty;
import lol.catgirl.utils.client.GameTimer;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class SpeedModule extends Module {
    public static final SpeedModule INSTANCE = new SpeedModule();

    public enum Mode {
        Legit,
        LegitExploit,
        Intave
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Intave);

    public SpeedModule() {
        super("Speed", "Allows you to go faster!!!", ModuleCategory.Movement);
        addSetting(mode);
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

            case LegitExploit -> {
                if (MoveUtils.isMoving() && mc.player.onGround()) {
                    PlayerUtils.jump();
                }

                GameTimer.setSpeed(1.0075f);
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
