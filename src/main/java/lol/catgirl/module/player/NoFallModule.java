package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PlayerRotationEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.player.nofall.NoFallMode;
import lol.catgirl.module.player.nofall.impl.*;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;

import java.util.EnumMap;
import java.util.Map;

public final class NoFallModule extends Module {
    public static NoFallModule INSTANCE = new NoFallModule();

    public NoFallModule() {
        super("NoFall",
                "Negates or prevents taking fall damage.",
                ModuleCategory.Player
        );
        addSettings(mode, distance, useCobwebs, collectWater);
    }

    public enum Mode {
        MLG,
        OnGround,
        CubeCraft
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.MLG);
    public final SliderProperty distance = new SliderProperty("Distance", 4, 1, 255, 1);
    public final BoolProperty useCobwebs = new BoolProperty("Cobwebs", true).hide(()-> !(mode.getValue() == Mode.MLG));
    public final BoolProperty collectWater = new BoolProperty("Collect Water", true).hide(()-> !(mode.getValue() == Mode.MLG));

    private final Map<Mode, NoFallMode>
            nofallModes = new EnumMap<>(Mode.class);

    {
        nofallModes.put(Mode.MLG, new MLGNoFallMode(this));
        nofallModes.put(Mode.CubeCraft, new CubeCraftNoFallMode());
        nofallModes.put(Mode.OnGround, new OnGroundNoFallMode());
    }

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        if (!isEnabled()) return;
        if(mc.player == null || mc.level == null) {
            return;
        }

        NoFallMode currentMode = nofallModes.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onPreMotion(event);
        }
    }

    @EventHook
    public void onRotation(PlayerRotationEvent event) {
        if(!this.isEnabled()|| mc.player == null || mc.level == null) {
            return;
        }

        NoFallMode currentMode = nofallModes.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onRotation(event);
        }
    }

    @EventHook
    public void onPacket(PacketSendEvent event) {
        if(!this.isEnabled()|| mc.player == null || mc.level == null) {
            return;
        }

        NoFallMode currentMode = nofallModes.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onPacketSend(event);
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
