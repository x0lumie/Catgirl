package lol.catgirl.module.ghost;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PlayerAttackPreEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.*;
import lol.catgirl.utils.client.MathUtils;

public final class AutoComboModule extends Module {
    public static final AutoComboModule INSTANCE = new AutoComboModule();

    public enum Mode {
        WTap, STap, ShiftTap, SprintReset, JumpReset
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.WTap);
    public final SliderProperty minTap = new SliderProperty("Min tap", 60, 10, 500, 1);
    public final SliderProperty maxTap = new SliderProperty("Max tap", 100, 10, 500, 1);
    public final SliderProperty minCooldown = new SliderProperty("Min cooldown", 50, 0, 500, 1);
    public final SliderProperty maxCooldown = new SliderProperty("Max cooldown", 80, 0, 500, 1);
    public final BoolProperty stopSprint = new BoolProperty("Stop sprint", true);
    public final BoolProperty onGroundOnly = new BoolProperty("On ground only", true);

    public AutoComboModule() {
        super("AutoCombo",
                "Automatically combo's people doing more knockback.",
                ModuleCategory.Ghost
        );
        addSettings(
                mode, minTap, maxTap, minCooldown, maxCooldown,
                stopSprint, onGroundOnly
        );
    }

    private long tapEnd, cooldownEnd;

    @EventHook
    private void onAttack(PlayerAttackPreEvent event) {
        if(!this.isEnabled() || mc.player == null || mc.level == null) {
            return;
        }

        if(onGroundOnly.getValue() && !mc.player.onGround()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < cooldownEnd) return;

        long tapTime = MathUtils.randomLong(
                minTap.getValue().longValue(),
                maxTap.getValue().longValue()
        );

        long cooldown = MathUtils.randomLong(
                minCooldown.getValue().longValue(),
                maxCooldown.getValue().longValue()
        );

        tapEnd = now + tapTime;
        cooldownEnd = now + tapTime + cooldown;
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if(!this.isEnabled() || mc.player == null || mc.level == null) {
            return;
        }

        if(mc.screen != null) return;

        long now = System.currentTimeMillis();

        var forward = mc.options.keyUp;
        var back = mc.options.keyDown;
        var sprint = mc.options.keySprint;
        var sneak = mc.options.keyShift;
        var jump = mc.options.keyJump;

        if (now >= tapEnd) return;

        switch (mode.getValue()) {
            case WTap -> {
                forward.setDown(false);
                sprint.setDown(false);

                if (stopSprint.getValue()) {
                    mc.player.setSprinting(false);
                }
            }

            case STap -> {
                back.setDown(false);
            }

            case ShiftTap -> {
                sneak.setDown(true);
            }

            case SprintReset -> {
                sprint.setDown(false);
                mc.player.setSprinting(false);
            }

            case JumpReset -> {
                if (mc.player.onGround()) {
                    jump.setDown(true);
                }
            }
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
