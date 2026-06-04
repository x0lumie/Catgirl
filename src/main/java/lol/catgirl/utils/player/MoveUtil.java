package lol.catgirl.utils.player;

import lol.catgirl.utils.IMinecraft;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MoveUtil implements IMinecraft {

    public static void setSpeed(double speed) {
        double dir = direction();
        mc.player.setDeltaMovement(-Math.sin(dir) * speed, mc.player.getDeltaMovement().y, Math.cos(dir) * speed);
    }

    public static void useDiagonalSpeed() {
        boolean isDiagonal = movementForward() != 0 && movementSideways() != 0;
        if (isDiagonal) {
            double currentSpeed = getSpeed();
            setSpeed(currentSpeed * 0.98);
        }
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = mc.player.getAbilities().getWalkingSpeed() * 2.873;

        if (mc.player.hasEffect(MobEffects.SLOWNESS)) {
            MobEffectInstance slowness = mc.player.getEffect(MobEffects.SLOWNESS);
            baseSpeed /= 1.0 + 0.2 * (slowness.getAmplifier() + 1);
        }

        if (mc.player.hasEffect(MobEffects.SPEED)) {
            MobEffectInstance speed = mc.player.getEffect(MobEffects.SPEED);
            baseSpeed *= 1.0 + 0.2 * (speed.getAmplifier() + 1);
        }

        return baseSpeed;
    }

    public static void boost(float yaw, double boost) {
        float g = yaw * (float) (Math.PI / 180.0);
        mc.player.addDeltaMovement(new Vec3(-Mth.sin(g) * boost, 0.0, Mth.cos(g) * boost));
    }

    public static void moveRelative(float forward, float strafe, float friction) {
        float f = forward * forward + strafe * strafe;
        if (f >= 1.0E-4F) {
            f = Mth.sqrt(f);
            if (f < 1.0F) f = 1.0F;
            f = friction / f;
            forward *= f;
            strafe *= f;
            float f1 = Mth.sin(mc.player.getYRot() * (float) Math.PI / 180.0F);
            float f2 = Mth.cos(mc.player.getYRot() * (float) Math.PI / 180.0F);
            mc.player.addDeltaMovement(new Vec3(
                    (double) (forward * f2 - strafe * f1),
                    0.0,
                    (double) (strafe * f2 + forward * f1)
            ));
        }
    }

    public static void strafeSpeed(double speed) {
        double forward = mc.player.zza;
        double strafe = mc.player.xxa;
        float yaw = mc.player.getYRot();

        if (forward == 0 && strafe == 0) {
            mc.player.setDeltaMovement(0, mc.player.getDeltaMovement().y, 0);
            return;
        }

        if (forward != 0) {
            if (strafe > 0) {
                yaw += (forward > 0 ? -45 : 45);
            } else if (strafe < 0) {
                yaw += (forward > 0 ? 45 : -45);
            }

            strafe = 0;
            forward = forward > 0 ? 1 : -1;
        }

        double rad = Math.toRadians(yaw + 90.0);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double motionX = forward * speed * cos + strafe * speed * sin;
        double motionZ = forward * speed * sin - strafe * speed * cos;

        Vec3 current = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motionX, current.y, motionZ);
    }

    public static void setMotionY(double d) {
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(new Vec3(motion.x, d, motion.z));
    }

    public static void strafe(double speed) {
        if (isMoving()) {
            mc.player.setDeltaMovement(
                    -Math.sin(direction()) * speed, mc.player.getDeltaMovement().y,
                    Math.cos(direction()) * speed);
        } else {
            stop();
        }
    }

    public static void strafe() {
        strafe(getSpeed());
    }

    public static void stop() {
        mc.player.setDeltaMovement(0, mc.player.getDeltaMovement().y, 0);
    }

    public static float movementSideways() {
        return mc.player.input.getMoveVector().x;
    }

    public static double getSpeed() {
        return Math.sqrt(mc.player.getDeltaMovement().x * mc.player.getDeltaMovement().x +
                mc.player.getDeltaMovement().z * mc.player.getDeltaMovement().z);
    }

    public static float movementForward() {
        return mc.player.input.getMoveVector().y;
    }

    public static void setMotionX(double d) {
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(new Vec3(d, motion.y, motion.z));
    }

    public static void setMotionZ(double d) {
        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(new Vec3(motion.x, motion.y, d));
    }

    public static float direction() {
        float rotationYaw = mc.player.getYRot();
        if (mc.player.zza < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (mc.player.zza  < 0) {
            forward = -0.5F;
        } else if (mc.player.zza  > 0) {
            forward = 0.5F;
        }

        if (mc.player.xxa > 0) {
            rotationYaw -= 90 * forward;
        }
        if (mc.player.xxa < 0) {
            rotationYaw += 90 * forward;
        }
        return (float) Math.toRadians(rotationYaw);
    }

    public static boolean isMoving() {
        return mc.player.zza > 0 || mc.player.zza < 0 || mc.player.xxa > 0 || mc.player.xxa < 0;
    }

    public static double predictedMotion(double motion, int ticks) {
        if (ticks == 0) {
            return motion;
        } else {
            double predicted = motion;
            for(int i = 0; i < ticks; ++i) {
                predicted = (predicted - 0.08) * (double)0.98F;
            }
            return predicted;
        }
    }

    private static final double[] MOD_DEPTH_STRIDER = { 1.0, 0.75, 0.65, 0.5 };

    public static double getAllowedHorizontalDistance(boolean allowSprint) {
        var player = mc.player;
        if (player == null) return 0.0;

        boolean useBaseModifiers = false;
        double horizontalDistance;

        boolean inWeb = PlayerUtil.isInWeb();

        if (inWeb) {
            horizontalDistance = 0.105;
        } else if (!player.isInWater() && !player.isInLava()) {
            if (player.isCrouching()) {
                horizontalDistance = 0.0663;
            } else {
                horizontalDistance = 0.221;
                useBaseModifiers = true;
            }
        } else {
            horizontalDistance = 0.115;
            int depthStriderLevel = EnchantmentHelper.getEnchantmentLevel((Holder<Enchantment>) Enchantments.DEPTH_STRIDER, player);
            if (depthStriderLevel > 0) {
                horizontalDistance *= MOD_DEPTH_STRIDER[depthStriderLevel];
                useBaseModifiers = true;
            }
        }

        if (useBaseModifiers) {
            if (allowSprint && player.isSprinting()) {
                horizontalDistance *= 1.3;
            }
            MobEffectInstance speed = player.getEffect(MobEffects.SPEED);
            if (speed != null) {
                horizontalDistance *= 1.0 + 0.2 * (speed.getAmplifier() + 1);
            }
            if (player.hasEffect(MobEffects.SLOWNESS)) {
                horizontalDistance = 0.29;
            }
        }
        return horizontalDistance;
    }

    public static double getAllowedHorizontalDistance() {
        return getAllowedHorizontalDistance(true);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double getSpeedByBPS(double bps) {
        return bps / 20.0;
    }
}