package lol.catgirl.event.impl;

import lol.catgirl.event.Event;

public class PlayerRotationEvent extends Event {
    public float yaw, pitch;

    public PlayerRotationEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
}

