package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.manager.TargetManager;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.EnumSetting;
import lol.catgirl.setting.impl.SliderSetting;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuraModule extends Module {
    public static final AuraModule INSTANCE = new AuraModule();

    // if u want my aura tell me and ill give it to you. :ribbon:

    public final EnumSetting<TargetManager.Mode> mode =
            new EnumSetting<>("Mode", TargetManager.Mode.Adaptive);
    public final EnumSetting<TargetManager.Entities> entities =
            new EnumSetting<>("Entities", TargetManager.Entities.Optimal);
    public static SliderSetting seekRange = new SliderSetting("Seek Range", 4.2f, 3, 6, 0.1f);
    public static SliderSetting killRange = new SliderSetting("Kill Range", 3, 3, 6, 0.1f);

    public AuraModule() {
        super("Aura", "Automatically kills enemies in a specified vicinity.", ModuleCategory.Movement);
        addSettings(mode, entities, seekRange, killRange); // dont forget this!
    }

    public static LivingEntity target;
    List<LivingEntity> targetList = new CopyOnWriteArrayList<>();

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null) return;

        TargetManager.setMode(mode.getValue());
        TargetManager.setEntities(entities.getValue());
        TargetManager.setSeekRange(seekRange.getValue());

        targetList = TargetManager.getTargetList();
        target = TargetManager.getTarget();

        calculateRotations();
        attack();
    }

    private void calculateRotations() {
        if (mc.player == null || target == null) return;

        RotationUtils.setRotations(RotationUtils.getRotations(new float[]{RotationUtils.getLastRotationYaw(), RotationUtils.getLastRotationPitch()}, mc.player.getEyePosition(), target));
    }

    private void attack() {
        if (mc.player == null || mc.gameMode != null) return;
        if (mc.player.distanceTo(target) > killRange.getValue()) return;

        if (!(mc.player.getAttackStrengthScale(0.5f) >= 1)) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

    }

}