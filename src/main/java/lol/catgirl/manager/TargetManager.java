package lol.catgirl.manager;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PreUpdateEvent;
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

import static lol.catgirl.utils.IMinecraft.mc;

public class TargetManager {

    // this is the one from simp and it works great! -lumie

    @Getter
    @Setter
    private static LivingEntity target;
    @Getter
    private static List<LivingEntity> targetList = new CopyOnWriteArrayList<>();
    private static final TickingTimer switchTimer = new TickingTimer();
    @Getter
    @Setter
    private static Enum mode;
    @Getter
    @Setter
    private static Enum entities;
    @Getter
    @Setter
    private static boolean dontTargetTeams;
    @Getter
    @Setter
    private static float seekRange;
    @Getter
    @Setter
    private static int switchTime;
    private int targetIndex;

    public TargetManager() {
        mode = Mode.Adaptive;
        entities = Entities.Optimal;
        dontTargetTeams = false;
        seekRange = 4.2f;
        switchTime = 2;
    }

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

        switch (mode) {
            case Mode.Single -> target = targetList.getFirst();

            case Mode.Switch -> {
                if (targetIndex >= targetList.size()) {
                    targetIndex = 0;
                }

                if (switchTimer.hasTimeElapsed(switchTime * 100)) {
                    targetIndex = (targetIndex + 1) % targetList.size();
                    switchTimer.reset();
                }
                target = targetList.get(targetIndex);
            }

            case Mode.Adaptive -> {
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
                .filter(entity -> mc.player.distanceTo(entity) <= seekRange)
                .filter(this::isValidEntity)
                .collect(Collectors.toList());
    }

    private boolean isValidEntity(LivingEntity entity) {
        return switch (entities) {
            case Entities.Optimal -> entity instanceof Player || entity instanceof Mob;
            case Entities.Players -> entity instanceof Player;
            case Entities.All -> true;
            default -> throw new IllegalStateException("Unexpected value: " + entities);
        };
    }

}
