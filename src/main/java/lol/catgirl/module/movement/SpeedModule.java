package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.movement.speed.SpeedMode;
import lol.catgirl.module.movement.speed.impl.*;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;

import java.util.EnumMap;
import java.util.Map;

public final class SpeedModule extends Module {
    public static final SpeedModule INSTANCE = new SpeedModule();

    public enum Mode {
        Legit,
        LegitExploit,
        Intave,
        Matrix,
        Strafe
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Intave);

    private final Map<Mode, SpeedMode> speedModes;

    {
        speedModes = new EnumMap<>(Mode.class);

        speedModes.put(Mode.Legit, new LegitSpeedMode());
        speedModes.put(Mode.Strafe, new StrafeSpeedMode());
        speedModes.put(Mode.Intave, new IntaveSpeedMode());
        speedModes.put(Mode.Matrix, new MatrixSpeedMode());

    }

    public SpeedModule() {
        super("Speed", "Allows you to go faster!!!", ModuleCategory.Movement);
        addSettings(mode);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null) return;

        SpeedMode currentMode = speedModes.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onTick(event);
        }
    }

    @Override
    public void onDisable() {
        SpeedMode currentMode = speedModes.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onDisable();
        }
    }

    @Override
    public void onEnable() {
        SpeedMode currentMode = speedModes.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onEnable();
        }
    }

    @Override
    public String getFinalSuffix() {
        return mode.getValue().toString();
    }
}