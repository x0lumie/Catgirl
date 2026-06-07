package lol.catgirl.module.player.noslow.impl;

import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.module.player.noslow.NoSlowMode;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PacketUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class IntaveNoSlowMode implements NoSlowMode {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (mc.player == null) return;
        if (!PlayerUtils.isHoldingWeapon()) return;
        if (!(event.getPacket() instanceof ServerboundUseItemOnPacket packet)) return;
        if (!mc.player.isUsingItem() || !MoveUtils.isMoving()) return;

        BlockHitResult original = packet.getHitResult();
        BlockHitResult replaced = new BlockHitResult(
                Vec3.atCenterOf(original.getBlockPos()),
                original.getDirection(),
                original.getBlockPos(),
                original.isInside()
        );

        PacketUtils.sendPacket(new ServerboundUseItemOnPacket(
                packet.getHand(), replaced, packet.getSequence()
        ));
    }

    @Override
    public void onUsingItem(PlayerUseMultiplierEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;

        if (mc.player.isUsingItem() && PlayerUtils.isHoldingWeapon()) {
            event.setCancelled(true);
        }
    }
}
