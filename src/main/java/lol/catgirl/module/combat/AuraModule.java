package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PlayerRotationEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.TargetsModule;
import lol.catgirl.setting.impl.SliderProperty;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public class AuraModule extends Module {

    public static SliderProperty killRange = new SliderProperty("Kill Range", 3, 3, 6, 0.1f);
    public static SliderProperty rotationSpeed = new SliderProperty("Rotation Speed", 2, 1, 5, 0.5f);

    public static final AuraModule INSTANCE = new AuraModule();

    public AuraModule() {
        super("Aura", "Automatically kills enemies in a specified vicinity.", ModuleCategory.Combat);
        addSettings(killRange, rotationSpeed);
    }

    public static LivingEntity target;

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null) return;

        target = TargetsModule.getTarget();

        if (target == null) return;

        attack();
    }

    @EventHook
    public void onPlayerRotation(PlayerRotationEvent event) {
        if (mc.player == null || target == null) return;

        float[] targetRotations = RotationUtils.getRotations(
                new float[]{event.yaw, event.pitch},
                mc.player.getEyePosition(),
                target
        );

        float smoothedYaw = RotationUtils.smoothRotation(
                event.yaw,
                targetRotations[0],
                rotationSpeed.getValue() / 2
        );
        float smoothedPitch = RotationUtils.smoothRotation(
                event.pitch,
                targetRotations[1],
                rotationSpeed.getValue() / 2
        );

        float[] finalRotations = RotationUtils.getFixedRotation(
                new float[]{smoothedYaw, smoothedPitch},
                new float[]{event.yaw, event.pitch}
        );

        event.yaw = finalRotations[0];
        event.pitch = finalRotations[1];
    }

    private void attack() {
        if (mc.player == null || mc.gameMode == null || target == null) return;

        if (mc.player.distanceTo(target) > killRange.getValue()) return;

        if (mc.player.getAttackStrengthScale(0.5f) < 1.0f) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}