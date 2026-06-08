package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.SliderProperty;

public final class AntiAFKModule extends Module {
    public static final AntiAFKModule INSTANCE = new AntiAFKModule();

    private final SliderProperty delay = new SliderProperty("Delay", 15.0F, 1.0F, 60.0F, 1.0F);

    private long lastAction;

    public AntiAFKModule() {
        super("AntiAFK", "Stops you getting kicked for going afk.", ModuleCategory.Player);

        addSetting(delay);
    }

    @Override
    public void onEnable() {
        lastAction = System.currentTimeMillis();
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        long delayMs = (long) (delay.getValue() * 1000L);

        if (System.currentTimeMillis() - lastAction >= delayMs) {
            float yaw = mc.player.getYRot() + 5.0F;

            mc.player.setYRot(yaw);
            mc.player.setYHeadRot(yaw);
            mc.player.setYBodyRot(yaw);

            lastAction = System.currentTimeMillis();
        }
    }
}