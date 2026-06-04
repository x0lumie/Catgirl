package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PlayerRotationEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.SliderProperty;
import java.util.Comparator;
import java.util.stream.StreamSupport;

public final class AntiFireballModule extends Module {
    public static final AntiFireballModule INSTANCE = new AntiFireballModule();

    private final SliderProperty range = new SliderProperty("Range", 4.5f, 1f, 8f, 0.1f);

    public AntiFireballModule() {
        super("AntiFireball",
                "Dodges incoming fireballs by hitting them.",
                ModuleCategory.Player
        );
        addSetting(range);
    }

    public static Entity target;
    private float[] rotations;

    @Override
    public void onDisable() {
        target = null;
        rotations = null;
    }

    public void getTarget() {
        target = StreamSupport.stream( // if this errors ignore it and reload gradle
                        mc.level.getEntities().getAll().spliterator(),
                        false
                )
                .filter(this::isValid)
                .min(Comparator.comparingDouble(
                        e -> mc.player.distanceTo(e)
                ))
                .orElse(null);
    }

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if(mc.player == null || mc.level == null || !this.isEnabled()) {
            return;
        }

        getTarget();

        if(target == null) return;
    //    if(mc.player.attackAnim > 0) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    @EventHook
    public void onRotation(PlayerRotationEvent event) {
        if(mc.player == null || mc.level == null || !this.isEnabled()) {
            return;
        }

        if(target == null) return;

        float[] lastRotations = new float[] {
                RotationUtils.getLastRotationYaw(),
                RotationUtils.getLastRotationPitch()
        };

        rotations = RotationUtils.getRotations(
                lastRotations,
                mc.player.getEyePosition(),
                target
        );

        rotations = RotationUtils.getFixedRotation(
                rotations, lastRotations
        );

        event.yaw = rotations[0];
        event.pitch = rotations[1];
    }

    private boolean isValid(Entity entity) {
        if (!(entity instanceof Fireball)) return false;

        if (mc.player.distanceTo(entity)
                > range.getValue().floatValue()) {
            return false;
        }

        return entity.isAlive();
    }
}