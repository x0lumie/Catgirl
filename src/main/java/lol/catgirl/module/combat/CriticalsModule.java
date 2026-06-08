package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PlayerAttackPreEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.TargetsModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PacketUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public final class CriticalsModule extends Module {
    public static final CriticalsModule INSTANCE = new CriticalsModule();

    public enum Mode {
        Jump,
        Packet,
        IntaveSemi
    }

    public static final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Jump);
    public final BoolProperty ignoreOnFire = new BoolProperty("Ignore On Fire", false);
    public final BoolProperty onlyWhileSprinting = new BoolProperty("Only While Sprinting", true);

    public CriticalsModule() {
        super("Criticals", "Always get critical hits.", ModuleCategory.Combat);
        addSettings(mode, ignoreOnFire, onlyWhileSprinting);
    }

    @EventHook
    public void onAttackPre(PlayerAttackPreEvent event) {
        if (mc.player == null) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        if (mc.player.isOnFire() && ignoreOnFire.getValue()) {
            return;
        }

        if (!mc.player.isSprinting() && onlyWhileSprinting.getValue()) {
            return;
        }

        switch (mode.getValue()) {
            case Jump -> {
                if (AuraModule.target != null && mc.player.onGround()) {
                    PlayerUtils.jump();
                }
            }

            case IntaveSemi -> {
                if ( mc.player.onGround() && !MoveUtils.isMoving()) {

                    double liveX = mc.player.getX();
                    double liveY = mc.player.getY();
                    double liveZ = mc.player.getZ();

                    PacketUtils.sendPacket(new ServerboundMovePlayerPacket.Pos(liveX, liveY - 1.0E-9, liveZ, false, mc.player.horizontalCollision));
                    PacketUtils.sendPacket(new ServerboundMovePlayerPacket.Pos(liveX, liveY + 1.0E-9, liveZ, false, mc.player.horizontalCollision));

                }
            }


            case Packet -> {
                PacketUtils.sendPacket(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625D, z, false, false));
                PacketUtils.sendPacket(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
                PacketUtils.sendPacket(new ServerboundMovePlayerPacket.Pos(x, y + 0.001D, z, false, false));
                PacketUtils.sendPacket(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
            }
        }
    }

    private void critPacket(double yDelta, boolean posOnly) {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        if (posOnly) {
            PacketUtils.sendPacket(new ServerboundMovePlayerPacket.Pos(x, y + yDelta, z, false, mc.player.horizontalCollision));
        } else {
            PacketUtils.sendPacket(new ServerboundMovePlayerPacket.PosRot(x, y + yDelta, z, mc.player.getYRot(), mc.player.getXRot(), false, mc.player.horizontalCollision));
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
