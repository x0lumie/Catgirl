package lol.catgirl.module.movement;

import lol.catgirl.accessor.IAbstractHorse;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PostTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class EntityControlModule extends Module {
    public static final EntityControlModule INSTANCE = new EntityControlModule();

    public EntityControlModule() {
        super("EntityControl", "Allows you to control entities without saddles.", ModuleCategory.Movement);
    }

    @EventHook
    public void onTick(PostTickEvent event) {
        if (mc.level != null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof AbstractHorse) {
                    ((IAbstractHorse)entity).setSaddled(true);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.level != null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof AbstractHorse) {
                    ((IAbstractHorse)entity).setSaddled(false);
                }
            }
        }
    }
}
