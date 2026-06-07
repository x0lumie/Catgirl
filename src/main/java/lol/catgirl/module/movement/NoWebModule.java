package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;

public final class NoWebModule extends Module {
    public static final NoWebModule INSTANCE = new NoWebModule();

    public enum Mode {
        Intave,
        Grim,
        Matrix
    }

    public final EnumProperty<Mode> mode = new EnumProperty("Mode", Mode.Intave);

    public NoWebModule() {
        super("NoWeb", "Stops you from slowing down inside webs.", ModuleCategory.Movement);
        addSettings(mode);
    }

    // IF U REPLACED THIS EVENT IM GONNA DO BAD THINGS OKAY???!!!!

    @EventHook
    public void onTick(PreUpdateEvent event) {
        if(mc.player == null) return;
        if(!PlayerUtils.isInWeb()) return;
        if(!MoveUtils.isMoving()) return;

        switch (mode.getValue()) {
            case Intave -> {
                if (mc.player.onGround()) {
                    if (mc.player.tickCount % 3 == 0) {
                        MoveUtils.setSpeedWithFixedDirection(0.734f);
                    } else {
                        mc.player.jumpFromGround();
                        MoveUtils.setSpeedWithFixedDirection(0.346f);
                    }
                }
            }

            case Grim -> {
                MoveUtils.setSpeedWithFixedDirection(0.644d);
            }

            case Matrix -> {
                MoveUtils.setSpeedWithFixedDirection(0.30d);
            }
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
