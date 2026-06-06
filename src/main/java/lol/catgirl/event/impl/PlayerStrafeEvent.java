package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
public class PlayerStrafeEvent extends Event {

    private Vec3 movementInput;
    private float speed;
    private float yaw;

    public PlayerStrafeEvent(Vec3 movementInput, float speed, float yaw) {
        this.movementInput = movementInput;
        this.speed = speed;
        this.yaw = yaw;
    }
}