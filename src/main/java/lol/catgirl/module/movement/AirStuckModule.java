package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PreMotionEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.Module;
import lol.catgirl.utils.player.MoveUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.client.Minecraft;

public final class AirStuckModule extends Module {
    public static AirStuckModule INSTANCE = new AirStuckModule();

    public AirStuckModule() {
        super("AirStuck",
                "Makes you get stuck mid-air",
                ModuleCategory.Movement
        );
    }

    @EventHook
    public void onPacketSend(PacketSendEvent event) {
        if (mc.player == null || !this.isEnabled()) return;

        mc.player.setDeltaMovement(0.0, 0.0, 0.0);

        if (event.getPacket() instanceof ServerboundInteractPacket packet) {
            if (packet.entityId == mc.player.getId()) {
                event.setCancelled(true);
            }
        }

        if (event.getPacket() instanceof ServerboundUseItemOnPacket) {
            if (!(mc.player.containerMenu.getCarried().getItem() instanceof BlockItem)) {
                event.setCancelled(true);

            }
        }

        if (event.getPacket() instanceof ServerboundMovePlayerPacket packet) {
            if (packet instanceof ServerboundMovePlayerPacket.Rot rot) {
                rot.yRot = Minecraft.getInstance().player.getYRot();
                rot.xRot = Minecraft.getInstance().player.getXRot();
                rot.onGround = Minecraft.getInstance().player.onGround();
            }

            if (packet instanceof ServerboundMovePlayerPacket.PosRot posRot) {
                posRot.yRot = Minecraft.getInstance().player.getYRot();
                posRot.xRot = Minecraft.getInstance().player.getXRot();
                posRot.x = Minecraft.getInstance().player.getX();
                posRot.y = Minecraft.getInstance().player.getY();
                posRot.z = Minecraft.getInstance().player.getZ();
                posRot.onGround = mc.player.onGround();
            }

            if (packet instanceof ServerboundMovePlayerPacket.Pos pos) {
                pos.x = Minecraft.getInstance().player.getX();
                pos.y = Minecraft.getInstance().player.getY();
                pos.z = Minecraft.getInstance().player.getZ();
                pos.onGround = mc.player.onGround();
            }

            if (packet instanceof ServerboundMovePlayerPacket.StatusOnly statusOnly) {
                statusOnly.onGround = mc.player.onGround();

            }
        }
    }

    @EventHook
    public void onPreMotion(PreMotionEvent event) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.setDeltaMovement(0.0, 0.0, 0.0);
            MoveUtils.setMotionX(0);
            MoveUtils.setMotionY(0);
            MoveUtils.setMotionZ(0);
        }
    }
}