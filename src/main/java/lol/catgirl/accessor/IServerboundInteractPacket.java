package lol.catgirl.accessor;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;

public interface IServerboundInteractPacket {
    Entity catgirl$getEntity();
    ServerboundInteractPacket.ActionType catgirl$getType();
}