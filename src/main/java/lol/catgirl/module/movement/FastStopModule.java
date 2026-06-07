package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PlayerMoveEvent;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.utils.player.MoveUtils;

public final class FastStopModule extends Module {
    public static final FastStopModule INSTANCE = new FastStopModule();

    public final BoolProperty onGround = new BoolProperty("On Ground", true);
    public final BoolProperty inAir = new BoolProperty("In Air", false);
    public final SliderProperty multiplier = new SliderProperty("Multiplier", 0.5f, 0, 1, 0.05f);

    public FastStopModule() {
        super("FastStop", "Quickly stops your movement.", ModuleCategory.Movement);
    }

    @EventHook
    public void onMove(PlayerMoveEvent event) {

        if ((mc.player.getDeltaMovement().x() != 0 || mc.player.getDeltaMovement().z() != 0) &&
                !MoveUtils.isMoving()) {
            if (!onGround.getValue() && !inAir.getValue()) {
                return;
            }

            if (!onGround.getValue() && mc.player.onGround()) {
                return;
            }

            if (!inAir.getValue() && !mc.player.onGround()) {
                return;
            }

            if (mc.player.hurtTime != 0) return;

            mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(this.multiplier.getValue(), 1, this.multiplier.getValue()));
        }
    }
}