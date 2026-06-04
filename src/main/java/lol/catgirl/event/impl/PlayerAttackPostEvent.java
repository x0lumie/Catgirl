package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import lombok.AllArgsConstructor;
import net.minecraft.world.entity.Entity;

@AllArgsConstructor
public class PlayerAttackPostEvent extends Event {
    public Entity target;
}
