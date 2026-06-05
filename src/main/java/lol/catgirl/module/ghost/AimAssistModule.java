package lol.catgirl.module.ghost;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.MouseEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.TargetsModule;
import lol.catgirl.utils.player.PlayerUtils;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import lol.catgirl.module.Module;
import lol.catgirl.property.impl.*;

public final class AimAssistModule extends Module {
    public static final AimAssistModule INSTANCE = new AimAssistModule();

    public final SliderProperty reach = new SliderProperty("Range", 6, 3, 50, 0.5f);
    public final SliderProperty angle = new SliderProperty("Angle", 60, 0, 360, 1);
    public final SliderProperty strength = new SliderProperty("Strength", 0.4f, 0.01f, 1.0f, 0.01f);
    public final SliderProperty fallOff = new SliderProperty("Falloff", 30, 1, 180, 1);
    public final BoolProperty onlyWeapons = new BoolProperty("Only weapons", true);
    public final BoolProperty onlyMoving = new BoolProperty("Only while moving", true);
    public final BoolProperty vertical = new BoolProperty("Vertical", false);

    public AimAssistModule() {
        super("AimAssist",
                "Automatically aims, works like a aim helper",
                ModuleCategory.Ghost);
        addSettings(
                reach,
                angle,
                strength,
                fallOff,
                onlyWeapons,
                onlyMoving,
                vertical
        );
    }

    @EventHook
    public void onMouseRotation(MouseEvent event) {
        LivingEntity target = TargetsModule.getTarget();

        if (target == null) return;
        if (!this.isEnabled()) return;

        double inputDeltaX = event.getCursorDeltaX();
        double inputDeltaY = event.getCursorDeltaY();
        boolean movingMouse = (Math.abs(inputDeltaX) > 0.001f || Math.abs(inputDeltaY) > 0.001f);
        if (!movingMouse && onlyMoving.getValue()) return;

        boolean weaponOk = !onlyWeapons.getValue()
                || PlayerUtils.isHoldingWeapon()
                || PlayerUtils.isHoldingMace();
        if (!weaponOk || mc.player.isUsingItem()) return;

        if (PlayerUtils.getBiblicallyAccurateDistanceToCentreOfEntity(target) >
                reach.getValue().doubleValue()) return;

        if (!isInAngle(target)) return;

        float[] current = {mc.player.getYRot(), mc.player.getXRot()};
        float[] toTarget = RotationUtils.getCentreRotations(
                current,
                mc.player.getEyePosition(),
                target
        );

        float deltaYaw = Mth.wrapDegrees(toTarget[0] - current[0]);
        float deltaPitch = toTarget[1] - current[1];

        float angularDist = (float) Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);

        float falloffDeg = fallOff.getValue().floatValue();
        float proximityFactor = Math.min(angularDist / falloffDeg, 1.0f);

        float s = strength.getValue().floatValue() * proximityFactor;

        event.setDeltaX(inputDeltaX + deltaYaw * s);

        if (vertical.getValue()) {
            event.setDeltaY(inputDeltaY + deltaPitch * s);
        }
    }

    private boolean isInAngle(LivingEntity target) {
        float playerYaw = mc.player.getYRot();
        float targetYaw = RotationUtils.getRotations(
                new float[]{playerYaw, mc.player.getXRot()},
                mc.player.getEyePosition(),
                target
        )[0];

        float diff = Mth.wrapDegrees(targetYaw - playerYaw);
        return Math.abs(diff) <= angle.getValue().floatValue() / 2f;
    }
}
