package lol.catgirl.utils.player;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import lol.catgirl.module.movement.MovementFixModule;
import lol.catgirl.utils.IMinecraft;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;

public class RotationUtils implements IMinecraft {
    public static boolean yawChanged;
    public static boolean pitchChanged;
    private static final NoiseUtils polarNoise = new NoiseUtils(2749);

    @Setter
    @Getter
    public static float rotationYaw, rotationPitch, lastRotationYaw, lastRotationPitch, camYaw, camPitch;

    @Getter
    @Setter
    private static HitResult currentHitResult;

    public static float[] regularAuraRotations(float[] currentRotations, Entity targetEntity, float speed) {
        float[] targetRotations = getRotations(currentRotations, mc.player.getEyePosition(), targetEntity);

        float smoothedYaw = getFixedRotation(targetRotations, currentRotations)[0];
        float smoothedPitch = getFixedRotation(targetRotations, currentRotations)[1];

        return new float[]{smoothRotation(currentRotations[0], smoothedYaw, speed), smoothRotation(currentRotations[1], smoothedPitch, speed)};
    }

    public static float[] puhfyAuraRotations(float[] currentRotations, final Entity entity, final float speed) {
        Vec3 eyePos = new Vec3(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(), mc.player.getZ());
        AABB box = entity.getBoundingBox();

        // i fucking love you puhfy
        double centerX = (box.minX + box.maxX) / 2.0;
        double centerY = (box.minY + box.maxY) / 2.0;
        double centerZ = (box.minZ + box.maxZ) / 2.0;

        double heightOffset = (box.maxY - box.minY) * 0.15;

        Vec3[] points = new Vec3[]{
                new Vec3(centerX, centerY - heightOffset, centerZ),
                new Vec3(centerX, centerY, centerZ),
                new Vec3(centerX, centerY + heightOffset, centerZ)
        };

        Vec3 bestPoint = null;
        double closestDist = Double.MAX_VALUE;

        for (Vec3 point : points) {
            double dist = eyePos.distanceTo(point);
            if (dist < closestDist) {
                closestDist = dist;
                bestPoint = point;
            }
        }

        if (bestPoint == null) return new float[]{currentRotations[0], currentRotations[1]};

        final float[] rotations = getRotationsToPoint(currentRotations, mc.player.getEyePosition(), bestPoint);

        float smoothedYaw = getFixedRotation(rotations, currentRotations)[0];
        float smoothedPitch = getFixedRotation(rotations, currentRotations)[1];

        return new float[]{smoothRotation(currentRotations[0], smoothedYaw, speed), smoothRotation(currentRotations[1], smoothedPitch, speed)};
    }

    public static float[] polarAuraRotations(float[] currentRotations, final Entity entity, float speed) {

        float[] targetRotations = puhfyAuraRotations(currentRotations, entity, speed);

        if (targetRotations[0] == currentRotations[0] && targetRotations[1] == currentRotations[1]) {
            return targetRotations;
        }

        int sped = 8;
        int existed = mc.player.tickCount * sped;

        float dist = mc.player.distanceTo(entity);
        float horizontalScale = (float) (1.9f + Math.max(-0.65, ((3 - dist) / 3)));
        float verticalScale   = (float) (1.5f + Math.max(-0.65, ((3 - dist) / 3)));

        double randomizedX = entity.getX() + (polarNoise.GetNoise(existed + 50,  existed + 250) / horizontalScale);
        double randomizedY = entity.getY() + 0.7 + (polarNoise.GetNoise(existed + 100, existed + 100) / verticalScale);
        double randomizedZ = entity.getZ() + (polarNoise.GetNoise(existed + 0,   existed + 150) / horizontalScale);

        Vec3 eye = mc.player.getEyePosition();
        Vec3 target3 = new Vec3(randomizedX, randomizedY, randomizedZ);
        Vec3 diff = target3.subtract(eye);

        double horizontal = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw   = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;
        float pitch = (float) Math.toDegrees(-Math.atan2(diff.y, horizontal));

        return getFixedRotation(new float[]{yaw, clampPitch(pitch)}, currentRotations);
    }

    public static boolean isFacing(Player self, Player target, float maxAngle) {
        Vec3 eye = self.getEyePosition();

        Vec3 dirToTarget = PlayerUtils.getClosestPoint(target).subtract(eye);

        double distXZ = Math.sqrt(dirToTarget.x * dirToTarget.x + dirToTarget.z * dirToTarget.z);

        float targetYaw = (float) Math.toDegrees(Math.atan2(dirToTarget.z, dirToTarget.x)) - 90f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dirToTarget.y, distXZ));

        float yawDiff = Math.abs(Mth.wrapDegrees(RotationUtils.getRotationYaw() - targetYaw));
        float pitchDiff = Math.abs(RotationUtils.getRotationPitch() - targetPitch);

        return yawDiff <= maxAngle && pitchDiff <= maxAngle;
    }

    public static float[] getFixedRotation(float[] rotations, float[] lastRotations) {
        double gcd = gcd();

        double deltaYaw = rotations[0] - lastRotations[0];
        double deltaPitch = rotations[1] - lastRotations[1];

        deltaYaw -= deltaYaw % gcd;
        deltaPitch -= deltaPitch % gcd;

        double fixedYaw = lastRotations[0] + deltaYaw;
        double fixedPitch = lastRotations[1] + deltaPitch;

        return new float[]{
                (float) fixedYaw,
                (float) Mth.clamp(fixedPitch, -90f, 90f)
        };
    }

    public static float smoothRotation(float current, float target, float speed) {
        speed = Mth.clamp(speed, 0.0f, 1.0f);

        float diff = Mth.wrapDegrees(target - current);

        if (Math.abs(diff) < 0.01f) {
            return target;
        }

        float smoothSpeed = speed * speed * (3.0f - 2.0f * speed);

        return current + diff * smoothSpeed;
    }

    public static double gcd() {
        double d = mc.options.sensitivity().get() * (double) 0.6F + (double) 0.2F;
        return d * d * d * 1.2;
    }


    public static float[] getRotations(float[] last, Vec3 eye, Entity entity) {
        Vec3 to = PlayerUtils.getClosestPoint(entity);
        Vec3 diff = to.subtract(eye);

        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float pitch = (float) Math.toDegrees(-Math.atan2(diff.y, dist));
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;

        yaw = unwrap(last[0], yaw);

        return new float[]{yaw, pitch};
    }

    public static float getLerpedPitch(float tickDelta, LivingEntity entity) {
        if (RotationUtils.pitchChanged) {
            return tickDelta == 1.0F ? RotationUtils.getRotationPitch() : Mth.lerp(tickDelta, RotationUtils.getLastRotationPitch(), RotationUtils.getRotationPitch());
        } else {
            return entity.getXRot(tickDelta);
        }
    }

    public static float[] getCentreRotations(float[] last, Vec3 eye, Entity entity) {
        Vec3 to = entity.getBoundingBox().getCenter();
        Vec3 diff = to.subtract(eye);

        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float pitch = (float) Math.toDegrees(-Math.atan2(diff.y, dist));
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;

        yaw = unwrap(last[0], yaw);

        return new float[]{yaw, pitch};
    }

    private static float unwrap(float oldYaw, float currentYaw) {
        float diff = currentYaw - (oldYaw % 360);

        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;

        return oldYaw + diff;
    }


    public static float[] getRotationsToBlock(Vec3 eye, BlockPos blockPos, Direction face) {
        Vec3 target = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
        target = target.add(new Vec3(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5));
        double diffX = target.x - eye.x;
        double diffY = target.y - eye.y;
        double diffZ = target.z - eye.z;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) ((pitch * 180F) / Math.PI + 90) * -1;
        yaw = (float) ((yaw * 180) / Math.PI) - 90;

        return new float[]{yaw, clampPitch(pitch)};
    }

    public static float[] getRotationsToPosition(Vec3 eye, Vec3 target) {
        Vec3 diff = target.subtract(eye);

        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float pitch = (float) Math.toDegrees(-Math.atan2(diff.y, dist));
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;

        return new float[]{yaw, clampPitch(pitch)};
    }

    public static float[] getBlockRotations(BlockPos blockPos, Direction facing) {
        Vec3 direction = blockPos.getCenter().add(Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(0.5).subtract(mc.player.getEyePosition()));

        float yaw = (float) Math.toDegrees(
                Math.atan2(
                        -direction.x,
                        direction.z
                )
        );

        float pitch = (float) Math.toDegrees(
                Math.atan2(
                        -direction.y,
                        Math.hypot(
                                direction.x,
                                direction.z
                        )
                )
        );


        yaw = unwrap(RotationUtils.getLastRotationYaw(), yaw);

        return new float[]{yaw, clampPitch(pitch)};
    }

    public static float[] getBlockRotations(BlockPos blockPos, Vec3 hitVec, Direction facing) {
        Vec3 direction = hitVec.subtract(mc.player.getEyePosition());

        float yaw = (float) Math.toDegrees(
                Math.atan2(
                        -direction.x,
                        direction.z
                )
        );

        float pitch = (float) Math.toDegrees(
                Math.atan2(
                        -direction.y,
                        Math.hypot(direction.x, direction.z)
                )
        );

        return new float[]{yaw, clampPitch(pitch)};
    }


    private static float clampPitch(float pitch) {
        return Mth.clamp(pitch, -90.0F, 90.0F);
    }


    public static void turnHead(float yaw, LivingEntityRenderState state) {
        float f = Mth.wrapDegrees(yaw - state.bodyRot);
        state.bodyRot += f * 0.3f;

        float h = 80.0f;
        if (Math.abs(f) > h) {
            state.bodyRot += f - Math.copySign(h, f);
        }

        float headRotation = Mth.wrapDegrees(yaw - state.bodyRot);
        state.yRot = headRotation;

    }

    public static float[] getRotationsToPoint(float[] last, Vec3 eye, Vec3 target) {
        Vec3 diff = target.subtract(eye);

        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float pitch = (float) Math.toDegrees(-Math.atan2(diff.y, dist));
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;

        yaw = unwrap(last[0], yaw);

        return new float[]{yaw, pitch};
    }

    public static float getMovementDirectionYaw() {
        var actualYaw = MovementFixModule.INSTANCE.isEnabled()
                ? RotationUtils.lastRotationYaw : RotationUtils.camYaw;
        final var inp = mc.player.input.keyPresses;
        if (inp.backward() && !inp.forward()) {
            actualYaw += 180f;
        }
        var forwardMultiplier =
                inp.backward()
                        && !inp.forward()
                        ? -0.5f : inp.forward()
                        && !inp.backward() ? 0.5f : 1f;

        if (inp.left() && !inp.right()) {
            actualYaw -= 90f * forwardMultiplier;
        }

        if (inp.right() && !inp.left()) {
            actualYaw += 90f * forwardMultiplier;
        }

        return actualYaw;
    }

    public static Vec3 getHitPosition() {
        if (currentHitResult != null) {
            return currentHitResult.getLocation();
        }
        return null;
    }

    public static boolean isLookingAtEntity() {
        return currentHitResult != null && currentHitResult.getType() == HitResult.Type.ENTITY;
    }

    public static Entity getTargetedEntity() {
        if (currentHitResult instanceof EntityHitResult entityHit) {
            return entityHit.getEntity();
        }
        return null;
    }

    public static BlockPos getTargetedBlockPos() {
        if (currentHitResult instanceof BlockHitResult blockHit) {
            return blockHit.getBlockPos();
        }
        return null;
    }

    private static final float TO_DEGREES = (float) (180.0 / Math.PI);

    public static Vec2 calculate(final Vec3 from, final Vec3 to) {
        final Vec3 diff = to.subtract(from);
        final double distance = Math.hypot(diff.x(), diff.z());

        // Mth.atan2 replaces MathHelper.atan2
        final float yaw = (float) (Mth.atan2(diff.z(), diff.x()) * TO_DEGREES) - 90.0F;
        final float pitch = (float) (-(Mth.atan2(diff.y(), distance) * TO_DEGREES));

        return new Vec2(yaw, pitch);
    }

    public static Vec2 calculate(final Entity entity) {
        if (mc.player == null) return Vec2.ZERO;

        AABB boundingBox = entity.getBoundingBox();
        double entityHeight = boundingBox.maxY - boundingBox.minY;

        // Custom position vectors are usually just .position() in modern versions
        Vec3 entityPos = entity.position();

        return calculate(entityPos.add(0, Math.max(0, Math.min(
                mc.player.getY() - entity.getY() + mc.player.getEyeHeight(),
                entityHeight * 0.9
        )), 0));
    }

    // Modern Minecraft uses Vec3 for everything that Vector3d used to handle
    public static Vec2 calculate(final Vec3 to) {
        if (mc.player == null) return Vec2.ZERO;
        return calculate(mc.player.position().add(0, mc.player.getEyeHeight(), 0), to);
    }

    public static Vec2 calculate(final BlockPos to) {
        if (mc.player == null) return Vec2.ZERO;
        // BlockPos center calculation done via .getBottomCenterWithOffset or explicitly adding 0.5
        return calculate(mc.player.position().add(0, mc.player.getEyeHeight(), 0), Vec3.atCenterOf(to));
    }

    public static Vec2 calculate(final Vec3 position, final Direction direction) {
        double x = position.x() + 0.5D + direction.getStepX() * 0.5D;
        double y = position.y() + 0.5D + direction.getStepY() * 0.5D;
        double z = position.z() + 0.5D + direction.getStepZ() * 0.5D;

        return calculate(new Vec3(x, y, z));
    }
}
