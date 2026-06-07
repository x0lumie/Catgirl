package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.module.player.nofall.NoFallMode;
import lol.catgirl.module.player.noslow.NoSlowMode;
import lol.catgirl.module.player.noslow.impl.*;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import net.minecraft.world.item.*;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

import java.util.EnumMap;
import java.util.Map;

public final class NoSlowModule extends Module {
    public static final NoSlowModule INSTANCE = new NoSlowModule();

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.NCP);
    public final BoolProperty waitFirstTick = new BoolProperty("Wait First Tick", false).hide(() -> mode.getValue() != Mode.Polar);

    public final BoolProperty matrixFood = new BoolProperty("Matrix Food", true).hide(()-> !(mode.getValue() == Mode.Matrix));
    public final BoolProperty matrixPotion = new BoolProperty("Matrix Potion", true).hide(()-> !(mode.getValue() == Mode.Matrix));
    public final BoolProperty matrixSword = new BoolProperty("Matrix Sword", true).hide(()-> !(mode.getValue() == Mode.Matrix));
    public final BoolProperty matrixBow = new BoolProperty("Matrix Bow", true).hide(()-> !(mode.getValue() == Mode.Matrix));
    public final SliderProperty matrixStrafeSpeed = new SliderProperty("Matrix Strafe Speed", 0.0265f, 0.01f, 0.1f, 0.0005f).hide(()-> !(mode.getValue() == Mode.Matrix));

    public NoSlowModule() {
        super("NoSlow", "Removes the slow-down when using certain items.", ModuleCategory.Movement);
        addSettings(mode,
                waitFirstTick,
                matrixFood,
                matrixPotion,
                matrixSword,
                matrixBow,
                matrixStrafeSpeed
        );
    }

    public enum Mode {
        NCP,
        Jump,
        Intave,
        Polar,
        Matrix
    }

    private final Map<Mode, NoSlowMode> noSlowMode;

    {
        noSlowMode = new EnumMap<>(Mode.class);

        noSlowMode.put(Mode.NCP, new NCPNoSlowMode());
        noSlowMode.put(Mode.Jump, new JumpNoSlowMode());
        noSlowMode.put(Mode.Intave, new IntaveNoSlowMode());
        noSlowMode.put(Mode.Polar, new PolarNoSlowMode(this));
        noSlowMode.put(Mode.Matrix, new MatrixNoSlowMode(this));
    }

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        NoSlowMode currentMode = noSlowMode.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onPreMotion(event);
        }
    }

    @EventHook
    public void onPacketSend(PacketSendEvent event) {
        NoSlowMode currentMode = noSlowMode.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onPacketSend(event);
        }
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        NoSlowMode currentMode = noSlowMode.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onTick(event);
        }
    }

    @EventHook
    public void onUsingItem(PlayerUseMultiplierEvent event) {
        NoSlowMode currentMode = noSlowMode.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onUsingItem(event);
        }
    }

    @EventHook
    public void onBruhTick(ClientTickEvent event) {
        NoSlowMode currentMode = noSlowMode.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onBruhTick(event);
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}