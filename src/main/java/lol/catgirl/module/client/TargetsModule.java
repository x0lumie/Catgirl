package lol.catgirl.module.client;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.manager.FriendManager;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.TickingTimer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TargetsModule extends Module {

    public enum Mode {
        Adaptive,
        Switch,
        Single
    }

    public enum Entities {
        Optimal,
        Players,
        All
    }

    public final EnumProperty<Mode> mode =
            new EnumProperty<>("Mode", Mode.Adaptive);
    public final EnumProperty<Entities> entities =
            new EnumProperty<>("Entities", Entities.Optimal);
    public static SliderProperty seekRange = new SliderProperty("Seek Range", 4.2f, 3, 6, 0.1f);

    public static final TargetsModule INSTANCE = new TargetsModule();

    public TargetsModule() {
        super("Targets", "Settings for selecting the targets.", ModuleCategory.Client);
        addSettings(mode, entities, seekRange);
        this.toggle();
    }

    @Getter
    @Setter
    private static LivingEntity target;
    @Getter
    private static List<LivingEntity> targetList = new CopyOnWriteArrayList<>();
    private static final TickingTimer switchTimer = new TickingTimer();
    private int targetIndex;

    @Override
    public void onEnable() {
        toggle();
    }

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.player == null) return;

        targetList = getTargets();

        if (targetList.isEmpty()) {
            target = null;
            return;
        }

        selectTarget();
    }

    private void selectTarget() {
        if (targetList.isEmpty()) {
            target = null;
            return;
        }

        switch (mode.getValue()) {
            case Single -> target = targetList.getFirst();

            case Switch -> {
                if (targetIndex >= targetList.size()) {
                    targetIndex = 0;
                }

                if (switchTimer.hasTimeElapsed(100)) {
                    targetIndex = (targetIndex + 1) % targetList.size();
                    switchTimer.reset();
                }
                target = targetList.get(targetIndex);
            }

            case Adaptive -> {
                target = targetList.stream()
                        .min(Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
                        .orElse(null);
            }
            default -> throw new IllegalStateException("Unexpected value: " + entities);
        }
    }

    private List<LivingEntity> getTargets() {
        if (mc.level == null || mc.player == null) return List.of();

        return StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(entity -> entity != mc.player)
                .filter(Entity::isAlive)
                .filter(entity -> entity.getHealth() > 0)
                .filter(entity -> mc.player.distanceTo(entity) <= seekRange.getValue())
                .filter(this::isValidEntity)
                .collect(Collectors.toList());
    }

    private boolean isValidEntity(LivingEntity entity) {
        if (entity instanceof Player player) {

            if (FriendManager.isFriend(player)) {
                return false;
            }

            if (AntiBotModule.INSTANCE.isEnabled()
                    && AntiBotModule.INSTANCE.isBot(player)) {
                return false;
            }
        }

        return switch (entities.getValue()) {
            case Optimal -> entity instanceof Player || entity instanceof Mob;
            case Players -> entity instanceof Player;
            case All -> true;
        };
    }
}
