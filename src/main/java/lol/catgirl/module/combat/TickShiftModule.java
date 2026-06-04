package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.utils.client.GameTimer;

import lol.catgirl.module.Module;

public final class TickShiftModule extends Module {
    public static final TickShiftModule INSTANCE = new TickShiftModule();

    public TickShiftModule() {
        super("TickShift", "Shift the ticks.", ModuleCategory.Combat);
    }

    private int counter = 0;

    @EventHook
    public void onPreMotion(PreMotionEvent event) {

        if (counter == 0) GameTimer.setSpeed(0.1f);
        else GameTimer.setSpeed(0.5f);
        counter = (counter + 1) % 20;
    }

    @Override
    public void onDisable() {
        GameTimer.reset();
        counter = 0;
    }
}