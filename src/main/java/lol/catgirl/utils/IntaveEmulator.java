package lol.catgirl.utils;

import net.minecraft.world.phys.Vec3;
import java.util.*;

public class IntaveEmulator {

    private static class IntaveState {
        double verifiedX, verifiedY, verifiedZ;
        double motionX, motionY, motionZ;
        double baseMotionX, baseMotionY, baseMotionZ;
        boolean lastOnGround, onGround;
        boolean collidedHorizontally, collidedVertically;
        int reduceTicks;
        int flyingPacketTicks;
        double physicsVL = 0;
        int pastVelocity = 0;
        boolean inWater = false;
        boolean inLava = false;
        boolean inWeb = false;
        boolean elytraFlying = false;
        int keyForward = 0, keyStrafe = 0;
        float yaw = 0;
        boolean sprinting = false;
        boolean sneaking = false;
        int pastBlockPlacement = 10;
        int pastEntityUse = 10;
        int pastRiptideSpin = 0;
        int fireworkRocketsTicks = 0;
        double fireworkRocketsPower = 0;
    }

    private IntaveState serverState = new IntaveState();
    private IntaveState clientState = new IntaveState();

    private static final class Deviation {
        double horizontal;
        double vertical;

        Deviation(double h, double v) {
            this.horizontal = h;
            this.vertical = v;
        }
    }

    private Deviation getCurrentDeviation() {
        if (serverState.inWater || serverState.inLava) {
            return new Deviation(0.018, 0.02);
        }
        if (serverState.inWeb) {
            return new Deviation(0.13, 0.13);
        }
        if (serverState.elytraFlying) {
            return new Deviation(0.05, 0.03);
        }
        if (serverState.flyingPacketTicks > 0) {
            return new Deviation(0.05, 0.03);
        }
        if (serverState.sneaking) {
            return new Deviation(0.08, 0.08);
        }
        if (serverState.collidedHorizontally) {
            return new Deviation(0.027, 0.01);
        }
        return new Deviation(0.0007, 0.01);
    }


    public Vec3 getOptimalMotion(Vec3 originalKnockback,
                                 boolean isMoving, boolean isSprinting, boolean isSneaking, boolean inWater,
                                 boolean inLava, boolean inWeb, boolean isOnGround,
                                 boolean elytraFlying, float playerYaw)
    {

        serverState.inWater = inWater;
        serverState.inLava = inLava;
        serverState.inWeb = inWeb;
        serverState.sprinting = isSprinting;
        serverState.sneaking = isSneaking;
        serverState.elytraFlying = elytraFlying;
        serverState.yaw = playerYaw;
        serverState.lastOnGround = serverState.onGround;
        serverState.onGround = isOnGround;

        simulateIntavePhysics(originalKnockback);

        Deviation dev = getCurrentDeviation();

        double optimalX = calculateOptimalAxis(serverState.motionX, dev.horizontal, originalKnockback.x);
        double optimalY = calculateOptimalAxis(serverState.motionY, dev.vertical, originalKnockback.y);
        double optimalZ = calculateOptimalAxis(serverState.motionZ, dev.horizontal, originalKnockback.z);

        optimalX += (Math.random() - 0.5) * dev.horizontal * 0.3;
        optimalZ += (Math.random() - 0.5) * dev.horizontal * 0.3;
        optimalY += (Math.random() - 0.5) * dev.vertical * 0.3;

        if (isMoving) {
            optimalX = adjustForMovement(optimalX, playerYaw, isSprinting);
            optimalZ = adjustForMovement(optimalZ, playerYaw, isSprinting);
        }

        clientState.motionX = optimalX;
        clientState.motionY = optimalY;
        clientState.motionZ = optimalZ;

        return new Vec3(optimalX, optimalY, optimalZ);
    }

    private void simulateIntavePhysics(Vec3 knockback) {
        double gravity = 0.08;
        double drag = 0.98;
        double groundDrag = 0.91;

        serverState.baseMotionX = knockback.x;
        serverState.baseMotionY = knockback.y;
        serverState.baseMotionZ = knockback.z;

        if (serverState.onGround) {
            serverState.motionX = serverState.baseMotionX * groundDrag;
            serverState.motionZ = serverState.baseMotionZ * groundDrag;
        } else {
            serverState.motionX = serverState.baseMotionX * drag;
            serverState.motionZ = serverState.baseMotionZ * drag;
        }

        serverState.motionY = (serverState.baseMotionY - gravity) * drag;

        if (serverState.inWeb) {
            serverState.motionX *= 0.4;
            serverState.motionZ *= 0.4;
            serverState.motionY *= 0.8;
        }

        if (serverState.inWater) {
            serverState.motionX *= 0.8;
            serverState.motionZ *= 0.8;
            serverState.motionY *= 0.8;
        }

        if (serverState.flyingPacketTicks > 0) {
            serverState.flyingPacketTicks--;
        }
    }

    private double calculateOptimalAxis(double serverMotion, double deviation, double original) {

        double sign = Math.signum(serverMotion);
        double absServer = Math.abs(serverMotion);

        double minAllowed = Math.max(0, absServer - deviation);
        double maxAllowed = absServer + deviation;

        double targetReduction;

        if (serverState.inWeb) {
            targetReduction = absServer * (0.7 + Math.random() * 0.15);
        } else if (serverState.inWater) {
            targetReduction = absServer * (0.96 + Math.random() * 0.03);
        } else if (serverState.flyingPacketTicks > 0) {
            targetReduction = absServer * (0.90 + Math.random() * 0.08);
        } else if (!isMoving() && absServer > 0.1) {
            targetReduction = absServer * (0.85 + Math.random() * 0.10);
        } else {
            targetReduction = absServer * (0.98 + Math.random() * 0.02);
        }

        double finalAbs = Math.min(maxAllowed, Math.max(minAllowed, targetReduction));

        return sign * finalAbs;
    }

    private double adjustForMovement(double motion, float yaw, boolean sprinting) {

        double speedBonus = sprinting ? 0.02 : 0.01;
        double randomDir = (Math.random() - 0.5) * 0.005;

        return motion + speedBonus + randomDir;
    }

    private boolean isMoving() {
        return serverState.keyForward != 0 || serverState.keyStrafe != 0;
    }

    public double predictViolationLevel(Vec3 clientMotion, boolean isMoving) {
        Deviation dev = getCurrentDeviation();

        double diffX = Math.abs(clientMotion.x - serverState.motionX);
        double diffY = Math.abs(clientMotion.y - serverState.motionY);
        double diffZ = Math.abs(clientMotion.z - serverState.motionZ);

        double horizontalAbuse = Math.max(0, Math.max(diffX, diffZ) - dev.horizontal);
        double verticalAbuse = Math.max(0, diffY - dev.vertical);

        double movingMultiplier = isMoving ? 2.5 : 1.0;

        if (horizontalAbuse > 0.01 || verticalAbuse > 0.01) {
            return (horizontalAbuse * 100 + verticalAbuse * 50) * movingMultiplier;
        }

        return 0;
    }

    public void updateFlyingPacketState(boolean sentFlyingPacket) {
        if (sentFlyingPacket) {
            serverState.flyingPacketTicks = 3;
        }
    }

    public void updateMovementKeys(int forward, int strafe) {
        serverState.keyForward = forward;
        serverState.keyStrafe = strafe;
    }
}