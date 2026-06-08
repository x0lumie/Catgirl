package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public final class AntiHungerModule extends Module {
    public static final AntiHungerModule INSTANCE = new AntiHungerModule();

    public final BoolProperty sprint = new BoolProperty("Sprint", false);
    public final BoolProperty onGround = new BoolProperty("On Ground", false);

    public AntiHungerModule() {
        super("AntiHunger", "Prevents hunger but not fully.", ModuleCategory.Player);
        addSettings(sprint, onGround);
    }
    private boolean lastOnGround, ignorePacket;


    @Override
    public void onEnable() {
        lastOnGround = mc.player.onGround();
    }


    @EventHook
    private void onSendPacket(PacketSendEvent event) {
        if (ignorePacket && event.getPacket() instanceof ServerboundMovePlayerPacket) {
            ignorePacket = false;
            return;
        }

        if (mc.player.isPassenger() || mc.player.isInWater() || mc.player.isUnderWater()) return;

        if (event.getPacket() instanceof ServerboundPlayerCommandPacket packet && sprint.getValue()) {
            if (packet.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING)
                event.setCancelled(true);
        }

        if (event.getPacket() instanceof ServerboundMovePlayerPacket packet && onGround.getValue() && mc.player.onGround()
                && mc.player.fallDistance <= 0.0 && !mc.gameMode.isDestroying()) {
            packet.onGround = true;
        }
    }

    @EventHook
    private void onTick(PreUpdateEvent event) {
        if (mc.player.onGround() && !lastOnGround && onGround.getValue()) {
            ignorePacket = true;
        }

        lastOnGround = mc.player.onGround();
    }
}
