package lol.catgirl.utils.player;

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
import net.minecraft.world.phys.Vec3;

public class RotationUtil implements IMinecraft {
    public static boolean yawChanged;
    public static boolean pitchChanged;

    @Setter
    @Getter
    public static float rotationYaw, rotationPitch, lastRotationYaw, lastRotationPitch, camYaw, camPitch;

    public static void setRotations (float[] rotations) {
        rotationYaw = rotations[0];
        rotationPitch = rotations[1];
    }

    public static boolean isFacing(Player self, Player target, float maxAngle) {
        Vec3 eye = self.getEyePosition();

        Vec3 dirToTarget = PlayerUtil.getClosestPoint(target).subtract(eye);

        double distXZ = Math.sqrt(dirToTarget.x * dirToTarget.x + dirToTarget.z * dirToTarget.z);

        float targetYaw = (float) Math.toDegrees(Math.atan2(dirToTarget.z, dirToTarget.x)) - 90f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dirToTarget.y, distXZ));

        float yawDiff = Math.abs(Mth.wrapDegrees(RotationUtil.getRotationYaw() - targetYaw));
        float pitchDiff = Math.abs(RotationUtil.getRotationPitch() - targetPitch);

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
        float diff = target - current;
        diff = Mth.wrapDegrees(diff);
        if (speed < 1f) speed = 1f;
        return current + diff / speed;
    }


    public static double gcd() {
        double d = mc.options.sensitivity().get() * (double)0.6F + (double)0.2F;
        return d * d * d * 1.2;
    }


    public static float[] getRotations(float[] last, Vec3 eye, Entity entity) {
        Vec3 to = PlayerUtil.getClosestPoint(entity);
        Vec3 diff = to.subtract(eye);

        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float pitch = (float) Math.toDegrees(-Math.atan2(diff.y , dist));
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;

        yaw = unwrap(last[0], yaw);

        return new float[]{yaw, pitch};
    }
    public static float getLerpedPitch(float tickDelta, LivingEntity entity) {
        if (RotationUtil.pitchChanged) {
            return tickDelta == 1.0F ? RotationUtil.getRotationPitch() : Mth.lerp(tickDelta, RotationUtil.getLastRotationPitch(), RotationUtil.getRotationPitch());
        } else {
            return entity.getXRot(tickDelta);
        }
    }
    public static float[] getCentreRotations(float[] last, Vec3 eye, Entity entity) {
        Vec3 to = entity.getBoundingBox().getCenter();
        Vec3 diff = to.subtract(eye);

        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float pitch = (float) Math.toDegrees(-Math.atan2(diff.y , dist));
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


        yaw = unwrap(RotationUtil.getLastRotationYaw(),yaw);

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
                ? RotationUtil.lastRotationYaw : RotationUtil.camYaw;
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
}
