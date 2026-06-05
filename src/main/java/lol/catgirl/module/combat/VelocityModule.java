package lol.catgirl.module.combat;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.event.impl.PlayerAttackPreEvent;
import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

import java.util.concurrent.ThreadLocalRandom;

public final class VelocityModule extends Module {
    public static final VelocityModule INSTANCE = new VelocityModule();

    private boolean hitProcessed;
    private int jumpTicks;


    public enum Mode {
        Cancel,
        JumpReset,
        Intave,
        Matrix
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Cancel);
    public final BoolProperty polar = new BoolProperty("Polar", false).hide(()-> !(mode.getValue() == Mode.JumpReset));
    public final BoolProperty ignoreOnFire = new BoolProperty("Ignore on fire", true);

    public VelocityModule() {
        super("Velocity", "Uses heavy dick and balls to drag across the floor to reduce velocity.", ModuleCategory.Combat);
        addSettings(mode, ignoreOnFire);
    }

    private boolean intaveOnAttack;
    private boolean intaveIsHitSlow;

    @EventHook
    public void onPacket(PacketReceivedEvent event) {
        if(mc.player == null) return;
        if(mc.player.isOnFire() && ignoreOnFire.getValue()) return;


        if (event.packet instanceof ClientboundSetEntityMotionPacket packet) {

            if (packet.getId() == mc.player.getId()) {
                switch (mode.getValue()) {
                    case Cancel -> {
                        event.setCancelled(true);
                    }

                    case JumpReset -> {
                        // ontick
                    }

                    case Intave -> {
                        if (mc.player.onGround()) {
                            if (intaveOnAttack && intaveIsHitSlow
                                    && mc.player.isSprinting()) {
                                double motionX = packet.movement.x;
                                double motionZ = packet.movement.z;
                                motionZ *= 0.06;
                                motionX *= 0.06;
                                MoveUtils.setMotionX(motionX);
                                MoveUtils.setMotionZ(motionZ);
                                mc.player.setSprinting(false);
                            }

                            intaveOnAttack = false;
                            intaveIsHitSlow = false;
                        }
                    }

                    case Matrix -> {
                        // IDK if this works anymore
                        double motionX = packet.movement.x;
                        double motionZ = packet.movement.z;
                        motionZ *= 0.06;
                        motionX *= 0.06;
                        MoveUtils.setMotionX(motionX);
                        MoveUtils.setMotionZ(motionZ);
                    }
                }
            }
        }
    }

    @EventHook
    public void onSlowdown(PlayerUseMultiplierEvent event) {
        if(!(mode.getValue() == Mode.Intave)) return;
        intaveIsHitSlow = true;
    }

    @EventHook
    public void onAttack(PlayerAttackPreEvent event) {
        if(!(mode.getValue() == Mode.Intave)) return;
        intaveOnAttack = true;
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null) return;
        if (mc.player.isOnFire() && ignoreOnFire.getValue()) return;
        if(!(mode.getValue() == Mode.JumpReset)) return;

        if (mc.player.hurtTime > 0) {
            if (!hitProcessed) {
                if (polar.getValue()) {
                    if (ThreadLocalRandom.current().nextDouble() <= 0.75D) {
                        jumpTicks = ThreadLocalRandom.current().nextInt(0, 5);
                    }
                } else {
                    jumpTicks = 0;
                }
                hitProcessed = true;
            }
        } else {
            hitProcessed = false;
        }

        if (jumpTicks >= 0) {
            if (jumpTicks == 0) {
                if (mc.player.onGround()) {
                    PlayerUtils.jump();
                }
                jumpTicks = -1;
            } else {
                jumpTicks--;
            }
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
