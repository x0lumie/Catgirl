package lol.catgirl.module.player.noslow.impl;

import lol.catgirl.event.impl.PlayerUseMultiplierEvent;
import lol.catgirl.module.player.NoSlowModule;
import lol.catgirl.module.player.noslow.NoSlowMode;
import lol.catgirl.utils.player.PacketUtils;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

@AllArgsConstructor
public final class PolarNoSlowMode implements NoSlowMode {
    public final NoSlowModule module;

    @Override
    public void onUsingItem(PlayerUseMultiplierEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isFallFlying() || mc.player.isShiftKeyDown()) return;

        event.setCancelled(true);
        if (mc.player.isUsingItem()) {
            if (mc.player.getUseItemRemainingTicks() <= 1 && module.waitFirstTick.getValue()) return;

            int slot = mc.player.getInventory().getSelectedSlot();
            PacketUtils.sendPacket(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                    BlockPos.ZERO,
                    Direction.DOWN
            ));
            PacketUtils.sendPacket(new ServerboundSetCarriedItemPacket((slot + 1) % 9));
            PacketUtils.sendPacket(new ServerboundSetCarriedItemPacket(slot));
            PacketUtils.sendPacket(new ServerboundUseItemPacket(
                    InteractionHand.MAIN_HAND, 0, mc.player.getYRot(), mc.player.getXRot()
            ));
        }
    }
}
