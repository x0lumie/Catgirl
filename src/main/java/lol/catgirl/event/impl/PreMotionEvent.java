package lol.catgirl.event.impl;

import lol.catgirl.event.Event;

public class PreMotionEvent extends Event {
    public double posX;
    public double posY;
    public double posZ;
    public float yaw;
    public float pitch;
    public float lastYaw;
    public float lastPitch;
    public boolean onGround;
    public boolean isSprinting;
    public boolean isSneaking;
    public boolean horizontalCollision;

    public PreMotionEvent(double posX, double posY, double posZ, float yaw, float pitch, float lastYaw, float lastPitch, boolean onGround, boolean isSprinting, boolean isSneaking, boolean horizontalCollision) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.lastYaw = lastYaw;
        this.lastPitch = lastPitch;
        this.onGround = onGround;
        this.isSprinting = isSprinting;
        this.isSneaking = isSneaking;
        this.horizontalCollision = horizontalCollision;
    }
}
