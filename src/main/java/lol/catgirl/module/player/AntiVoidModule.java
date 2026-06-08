package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PacketUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;

public final class AntiVoidModule extends Module {
    public static final AntiVoidModule INSTANCE = new AntiVoidModule();

    public enum Mode {
        Position,
        Collide,
        Packet,
        Flag
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Position);
    public final SliderProperty distance = new SliderProperty("Distance", 3.0f, 1.0f, 6.0f, 0.1f);

    public AntiVoidModule() {
        super("AntiVoid", "Prevents you from going into the void.", ModuleCategory.Player);
        addSettings(mode);
    }

    private float newMotionY = 0;

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        if(!PlayerUtils.isOverVoid()) return;

        switch (mode.getValue()) {
            case Position -> {
                if (mc.player.fallDistance > distance.getValue().floatValue()) {
                    event.posY = event.posY + mc.player.fallDistance;
                }
            }

            case Packet -> {
                if (mc.player.fallDistance > distance.getValue().floatValue()) {
                    PacketUtils.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.onGround(), false));
                }
            }

            case Collide -> {
                if (mc.player.fallDistance > distance.getValue().floatValue()
                    && mc.player.getY() + mc.player.getDeltaMovement().y < Mth.floor(mc.player.getY())
                )   {
                    MoveUtils.setMotionY(newMotionY);

                    if (newMotionY == 0) {
                        mc.player.setOnGround(true);
                        event.onGround = true;
                    }
                }
            }

            case Flag -> {
                event.posY = -999;
            }
        }
    }


    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
