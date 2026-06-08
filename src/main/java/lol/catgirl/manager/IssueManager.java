package lol.catgirl.manager;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ChatEvent;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.file.impl.ModulesFile;
import lol.catgirl.utils.client.TickingTimer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.profiling.jfr.event.PacketEvent;

import static lol.catgirl.utils.IMinecraft.mc;

public class IssueManager {
    private static int lastSlot = -1;
    private static float lastYaw, lastPitch;
    private final TickingTimer saveTimer = new TickingTimer();

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        // Putting this here because I don't give a flying fuck.
        if (saveTimer.hasTimeElapsed(30000)) {
            new ModulesFile("default").saveToFile();
            saveTimer.reset();
        }
    }

    @EventHook
    public void onPacketSend(PacketSendEvent event) {
        // AimModulo360 Fix
        if (event.getPacket() instanceof ServerboundMovePlayerPacket packet) {

            float yaw = packet.yRot;
            if (yaw < 360.0f && yaw > -360.0f) {
                packet.yRot = yaw + 720f;
            }
        }

        // Bad Slot Packets Fix
        if (event.getPacket() instanceof ServerboundSetCarriedItemPacket packet) {
            int slot = packet.getSlot();
            if (slot == lastSlot && slot != -1) {
                event.setCancelled(true);
            }

            lastSlot = packet.getSlot();
        }

        // Aim Dupe Look Fix
        if (event.getPacket() instanceof ServerboundMovePlayerPacket packet && packet.hasRotation()) {
            if (lastYaw == packet.yRot && lastPitch == packet.xRot) {
                packet.yRot = packet.yRot + 0.001f;
            }
            lastYaw = packet.yRot;
            lastPitch = packet.xRot;
        }
    }
}
