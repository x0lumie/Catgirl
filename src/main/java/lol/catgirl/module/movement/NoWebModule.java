package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.movement.noweb.NoWebMode;
import lol.catgirl.module.movement.noweb.impl.*;
import lol.catgirl.module.movement.speed.SpeedMode;
import lol.catgirl.property.impl.EnumProperty;

import java.util.EnumMap;
import java.util.Map;

public final class NoWebModule extends Module {
    public static final NoWebModule INSTANCE = new NoWebModule();

    public enum Mode {
        Intave,
        Grim,
        Matrix
    }

    public final EnumProperty<Mode> mode = new EnumProperty("Mode", Mode.Intave);

    private final Map<Mode, NoWebMode> noWebModes; {
        noWebModes = new EnumMap<>(Mode.class);

        noWebModes.put(Mode.Grim, new GrimNoWebMode());
        noWebModes.put(Mode.Intave, new IntaveNoWebMode());
        noWebModes.put(Mode.Matrix, new MatrixNoWebMode());

    }

    public NoWebModule() {
        super("NoWeb", "Stops you from slowing down inside webs.", ModuleCategory.Movement);
        addSettings(mode);
    }

    @EventHook
    public void onTick(PreUpdateEvent event) {
        if (mc.player == null) return;

        NoWebMode currentMode = noWebModes.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onTick(event);
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
