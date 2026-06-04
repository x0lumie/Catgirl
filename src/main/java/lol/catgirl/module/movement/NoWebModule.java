package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.EnumProperty;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;

public class NoWebModule extends Module {
    public static final NoWebModule INSTANCE = new NoWebModule();

    public enum Mode {
        Intave
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
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
