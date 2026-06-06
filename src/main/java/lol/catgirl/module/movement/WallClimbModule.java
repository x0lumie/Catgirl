package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.utils.player.MoveUtils;

public final class WallClimbModule extends Module {
    public static final WallClimbModule INSTANCE = new WallClimbModule();

    private enum Mode {
        MineMenClub
    }

    private boolean hitHead;

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.MineMenClub);

    public WallClimbModule() {
        super("WallClimb", "Allows you to climb up walls.", ModuleCategory.Movement);
        addSetting(mode);
    }

    @EventHook
    public void onMove(PreMotionEvent event) {
        if(mc.player == null) return;

        if (mode.getValue() == Mode.MineMenClub) {
            if(mc.player.tickCount % 3 == 0) {
                if(mc.player.horizontalCollision && !hitHead) {
                    MoveUtils.setMotionY(0.42F);
                }
            }

            if (mc.player.verticalCollision) {
                hitHead = !mc.player.onGround();
            }
        }
    }

    @Override
    public String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
