package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.GameTimer;

public final class TimerModule extends Module {
    public static final TimerModule INSTANCE = new TimerModule();

    public final SliderProperty speed = new SliderProperty("Speed",
            1f, 0.1f, 10f, 1f);
//    public final BoolProperty tpsSync = new BoolProperty("TPS Sync", false);

    public TimerModule() {
        super("Timer",
                "Speeds up the game timer.",
                ModuleCategory.Player
        );
        addSetting(speed);
    }

    @Override
    public void onDisable() {
        GameTimer.reset();
        super.onDisable();
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        GameTimer.setSpeed(speed.getValue());
    }
}
