package lol.catgirl.mixin;

import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.EntityRemovedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Shadow @Mutable public abstract Entity getEntity(int id);

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo info) {
        if (getEntity(entityId) != null) {
            Catgirl.INSTANCE.eventBus.post(new EntityRemovedEvent(getEntity(entityId)));
        }
    }
}