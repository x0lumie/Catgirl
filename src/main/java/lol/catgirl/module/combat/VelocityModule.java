package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.event.impl.PlayerAttackPreEvent;
import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.combat.velocity.VelocityMode;
import lol.catgirl.module.combat.velocity.impl.CancelVelocityMode;
import lol.catgirl.module.combat.velocity.impl.JumpResetVelocityMode;
import lol.catgirl.module.combat.velocity.impl.MatrixSprintVelocityMode;
import lol.catgirl.module.combat.velocity.impl.MatrixVelocityMode;
import lol.catgirl.module.movement.SpeedModule;
import lol.catgirl.module.movement.speed.SpeedMode;
import lol.catgirl.module.movement.speed.impl.IntaveSpeedMode;
import lol.catgirl.module.movement.speed.impl.LegitSpeedMode;
import lol.catgirl.module.movement.speed.impl.MatrixSpeedMode;
import lol.catgirl.module.movement.speed.impl.StrafeSpeedMode;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class VelocityModule extends Module {
    public static final VelocityModule INSTANCE = new VelocityModule();

    public enum Mode {
        Cancel,
        JumpReset,
        Matrix,
        MatrixSprint
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Cancel);
    public final BoolProperty polar = new BoolProperty("Polar", false).hide(()-> !(mode.getValue() == Mode.JumpReset));
    public final BoolProperty ignoreOnFire = new BoolProperty("Ignore on fire", true);

    public VelocityModule() {
        super("Velocity", "Uses heavy dick and balls to drag across the floor to reduce velocity.", ModuleCategory.Combat);
        addSettings(mode, polar, ignoreOnFire);
    }

    private final Map<Mode, VelocityMode> velocityModes;

    {
        velocityModes = new EnumMap<>(Mode.class);

        velocityModes.put(Mode.JumpReset, new JumpResetVelocityMode(this));
        velocityModes.put(Mode.Cancel, new CancelVelocityMode());
        velocityModes.put(Mode.MatrixSprint, new MatrixSprintVelocityMode());
        velocityModes.put(Mode.Matrix, new MatrixVelocityMode());
    }

    @EventHook
    public void onPacket(PacketReceivedEvent event) {
        if(mc.player == null) return;
        if(mc.player.isOnFire() && ignoreOnFire.getValue()) return;

        VelocityMode currentMode = velocityModes.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onPacketRecieved(event);
        }
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null) return;
        if (mc.player.isOnFire() && ignoreOnFire.getValue()) return;

        VelocityMode currentMode = velocityModes.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onTick(event);
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
