package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
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

    @EventHook
    public void onTick(ClientTickEvent event) {
        if(mc.player == null) return;
        if(!PlayerUtils.isInWeb()) return;
        if(!MoveUtils.isMoving()) return;

        switch (mode.getValue()) {
            case Intave -> {
                if (mc.player.onGround()) {
                    if (mc.player.tickCount % 3 == 0) {
                        MoveUtils.strafe(0.734f);
                    } else {
                        mc.player.jumpFromGround();
                        MoveUtils.strafe(0.346f);
                    }
                }
            }

            case Grim -> {
                if (MoveUtils.isMoving()) {
                    MoveUtils.setSpeed(0.644d);
                }
            }

            case Matrix -> {
                MoveUtils.setSpeed(0.30d);
            }
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
