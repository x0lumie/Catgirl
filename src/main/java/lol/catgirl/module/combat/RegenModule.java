package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.player.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public final class RegenModule extends Module {
    public static final RegenModule INSTANCE = new RegenModule();

    public final SliderProperty minHealth = new SliderProperty("Min health", 4f, 0, 20f, 0.1f);
    public final SliderProperty packetAmount = new SliderProperty("Packet amount", 50, 0, 1000, 1);
    public final BoolProperty ongroundCheck = new BoolProperty("On ground", false);
    public final SliderProperty minHunger = new SliderProperty("Min hunger", 4f, 0, 20f, 0.1f);
    public final BoolProperty packetOnGround = new BoolProperty("Packet on ground", false);

    public RegenModule() {
        super("Regen",
                "A 1.8 exploit that allows you to regenerate health.",
                ModuleCategory.Combat
        );
        addSettings(
                minHealth,
                packetAmount,
                ongroundCheck,
                minHunger,
                packetOnGround
        );
    }

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        if (mc.player == null || !this.isEnabled()) return;

        if (mc.player.getHealth() >= minHealth.getValue().floatValue()) return;

        if (ongroundCheck.getValue() && !mc.player.onGround()) return;

        if (mc.player.getFoodData().getFoodLevel() <= minHunger.getValue()) return;

        for (int i = 0; i < packetAmount.getValue().intValue(); i++) {

            PacketUtils.sendSilentPacket(
                    new ServerboundMovePlayerPacket.PosRot(
                            mc.player.getX(),
                            mc.player.getY(),
                            mc.player.getZ(),
                            mc.player.getYRot(),
                            mc.player.getXRot(),
                            packetOnGround.getValue() ? true : mc.player.onGround(),
                            mc.player.horizontalCollision)
            );
        }
    }
}
