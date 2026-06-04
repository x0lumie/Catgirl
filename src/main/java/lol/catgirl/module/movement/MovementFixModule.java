package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.utils.player.RotationUtil;

public final class MovementFixModule extends Module {
    public static final MovementFixModule INSTANCE = new MovementFixModule();

    public MovementFixModule() {
        super("MovementFix", "Fixes your movement for anticheats.", ModuleCategory.Movement);
    }

    @EventHook
    public void onRenderTick(RenderTickEvent event) {
        if (mc.player == null) return;

        if (RotationUtil.yawChanged) {
            mc.player.yBob = RotationUtil.getCamYaw();
        }
        if (RotationUtil.pitchChanged) {
            mc.player.xBob = RotationUtil.getCamPitch();
        }
    }
}
