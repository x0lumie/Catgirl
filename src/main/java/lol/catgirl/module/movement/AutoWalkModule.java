package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.utils.player.PlayerUtils;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class AutoWalkModule extends Module {
    public static final AutoWalkModule INSTANCE = new AutoWalkModule();

    public final BoolProperty autoJump = new BoolProperty("Auto Jump", false);

    public AutoWalkModule() {
        super("AutoWalk", "Automatically walks " +
                "forward for you.", ModuleCategory.Movement);
    }

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        mc.options.keyUp.setDown(true);

        if (autoJump.getValue() && mc.player.onGround()) {
            PlayerUtils.jump();

        }
    }

    @Override
    public void onDisable() {
        mc.options.keyUp.setDown(false);
    }
}
