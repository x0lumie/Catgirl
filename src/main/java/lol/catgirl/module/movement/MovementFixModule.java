package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.utils.player.RotationUtils;

public final class MovementFixModule extends Module {
    public static final MovementFixModule INSTANCE = new MovementFixModule();

    public MovementFixModule() {
        super("MovementFix", "Fixes your movement for anticheats.", ModuleCategory.Movement);
    }

    @EventHook
    public void onRenderTick(RenderTickEvent event) {
        if (mc.player == null) return;

        if (RotationUtils.yawChanged) {
            mc.player.yBob = RotationUtils.getCamYaw();
        }
        if (RotationUtils.pitchChanged) {
            mc.player.xBob = RotationUtils.getCamPitch();
        }
    }
}
