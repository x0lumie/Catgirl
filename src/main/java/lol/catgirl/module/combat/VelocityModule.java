package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.BoolSetting;
import lol.catgirl.setting.impl.EnumSetting;
import lol.catgirl.utils.player.PlayerUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

import java.util.concurrent.ThreadLocalRandom;

public final class VelocityModule extends Module {
    public static final VelocityModule INSTANCE = new VelocityModule();

    private boolean hitProcessed;
    private int jumpTicks;


    public enum Mode {
        Cancel,
        JumpReset,
        Intave
    }

    public enum IntaveMode {
        Blatent, Safe
    }

    public final EnumSetting<Mode> mode = new EnumSetting<>("Mode", Mode.Cancel);
    public final BoolSetting polar = new BoolSetting("Polar", false).hide(()-> !(mode.getValue() == Mode.JumpReset));
    public final EnumSetting<IntaveMode> intaveMode = new EnumSetting<>("Intave Mode", IntaveMode.Blatent).hide(()-> !(mode.getValue() == Mode.Intave));
    public final BoolSetting ignoreOnFire = new BoolSetting("Ignore on fire", true);

    public VelocityModule() {
        super("Velocity", "Uses heavy dick and balls to drag across the floor to reduce velocity.", ModuleCategory.Combat);
        addSettings(mode, intaveMode, ignoreOnFire);
    }

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
                        var motionx = packet.getMovement().x() / 8000.0D;
                        var motionz = packet.getMovement().z() / 8000.0D;

                        if (intaveMode.getValue() == IntaveMode.Blatent) {
                            if (mc.player.hurtTime > 0) {
                                if (mc.player.onGround()) {
                                    motionx *= 0.52;
                                    motionz *= 0.52;
                                } else {
                                    motionx *= 0.8;
                                    motionz *= 0.8;
                                }
                            }
                        } else {
                            if (mc.player.hurtTime > 0) {
                                motionx *= 0.6;
                                motionz *= 0.6;
                            }
                        }

                        event.setCancelled(true);
                        mc.player.setDeltaMovement(
                                motionx,
                                packet.getMovement().y() / 8000.0D,
                                motionz
                        );
                    }
                }
            }
        }
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
                    PlayerUtil.jump();
                }
                jumpTicks = -1;
            } else {
                jumpTicks--;
            }
        }
    }
}
