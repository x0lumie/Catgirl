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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuraModule extends Module {

    public static SliderProperty killRange = new SliderProperty("Kill Range", 3, 3, 6, 0.1f);

    public static final AuraModule INSTANCE = new AuraModule();

    public AuraModule() {
        super("Aura", "Automatically kills enemies in a specified vicinity.", ModuleCategory.Combat);
        addSettings(killRange);
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
        calculateRotations();
    }

    private void calculateRotations() {
        if (mc.player == null || target == null) return;

        RotationUtils.setRotations(RotationUtils.getRotations(
                new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()},
                mc.player.getEyePosition(),
                target
        ));
    }

    private void attack() {
        if (mc.player == null || mc.gameMode == null || target == null) return;

        if (mc.player.distanceTo(target) > killRange.getValue()) return;

        if (mc.player.getAttackStrengthScale(0.5f) < 1.0f) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}