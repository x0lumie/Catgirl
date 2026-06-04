package lol.catgirl.event.impl;

import lombok.AllArgsConstructor;
import net.minecraft.world.entity.Entity;

@AllArgsConstructor
public class EntityRemovedEvent {
    public Entity entity;
}
