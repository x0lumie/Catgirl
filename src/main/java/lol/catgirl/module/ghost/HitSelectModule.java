package lol.catgirl.module.ghost;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PlayerAttackPostEvent;
import lol.catgirl.event.impl.PlayerAttackPreEvent;
import lol.catgirl.utils.player.MoveUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.*;

import java.util.concurrent.ThreadLocalRandom;

public final class HitSelectModule extends Module {
    public static final HitSelectModule INSTANCE = new HitSelectModule();

    public enum HitMode {
        Pause, Active
    }

    public enum Preference {
        Burst, Crit, Combo, Timer
    }

    public final EnumProperty<HitMode> mode = new EnumProperty<>("Mode", HitMode.Active);
    public final EnumProperty<Preference> preference = new EnumProperty<>("Preference", Preference.Burst);
    public final SliderProperty delayMin = new SliderProperty("Delay Min", 350, 100, 1500, 1);
    public final SliderProperty delayMax = new SliderProperty("Delay Max", 450, 100, 1500, 1);
    public final SliderProperty waitForFirstHit = new SliderProperty("Wait For First Hit", 0, 0, 3000, 1);
    public final SliderProperty hitLaterInTrades = new SliderProperty("Hit Later In Trades", 0, 0, 3000, 1);
    public final BoolProperty disableDuringKB = new BoolProperty("Disable During KB", false);
    public final BoolProperty onlyWhileDamaged = new BoolProperty("Only While Damaged", false);
    public final SliderProperty cancelInCombat = new SliderProperty("Cancel Rate Dombat", 80, 0, 100, 1);
    public final SliderProperty cancelMissed = new SliderProperty("Cancel Rate Missed", 0, 0, 100, 1);
    public final BoolProperty fakeSwing = new BoolProperty("Fake Swing", false);

    public boolean currentShouldAttack = false;

    private long lastAttackTime = -1L;
    private boolean firstHitReceived = false;
    private long lastTradeHitTime = -1L;
    private int prevHurtTime = 0;
    private long fightStartTime = -1L;
    private boolean hasDealtFirstHit = false;

    public HitSelectModule() {
        super("HitSelect",
                "Restricts attacks to effective moments",
                ModuleCategory.Combat);

        addSettings(
                mode,
                preference,
                delayMin,
                delayMax,
                waitForFirstHit,
                hitLaterInTrades,
                disableDuringKB,
                onlyWhileDamaged,
                cancelInCombat,
                cancelMissed,
                fakeSwing
        );
    }

    @Override
    public void onEnable() {
        resetState();
    }

    @Override
    public void onDisable() {
        resetState();
    }

    private void resetState() {
        currentShouldAttack = false;
        lastAttackTime = -1L;
        firstHitReceived = false;
        lastTradeHitTime = -1L;
        prevHurtTime = 0;
        fightStartTime = -1L;
        hasDealtFirstHit = false;
    }

    @EventHook
    public void onAttackPre(PlayerAttackPreEvent event) {
        if(!this.isEnabled()) return;


        if (mode.getValue() == HitMode.Active && shouldCancelAttack()) {
            event.setCancelled(true);
        }
    }

    @EventHook
    public void onAttackPost(PlayerAttackPostEvent event) {
        if(!this.isEnabled()) return;

        lastAttackTime = System.currentTimeMillis();
        hasDealtFirstHit = true;
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if(!this.isEnabled()) return;

        if (mc.player == null) {
            return;
        }

        Player player = mc.player;

        long now = System.currentTimeMillis();

        boolean justGotHit = player.hurtTime > prevHurtTime;
        prevHurtTime = player.hurtTime;

        if (justGotHit) {
            firstHitReceived = true;
            lastTradeHitTime = now;
        }

        Entity target = mc.crosshairPickEntity;
        boolean hasTarget = target != null;

        if (hasTarget && fightStartTime < 0) {
            fightStartTime = now;
        } else if (!hasTarget) {
            fightStartTime = -1L;
            firstHitReceived = false;
            hasDealtFirstHit = false;
        }

        currentShouldAttack = evaluate(player, now);
    }

    private boolean evaluate(Player player, long now) {
        Entity target = mc.crosshairPickEntity;
        boolean inCombat = target != null;

        if (!hasDealtFirstHit && preference.getValue() != Preference.Timer) {
            return true;
        }

        if (waitForFirstHit.getValue() > 0 && !firstHitReceived) {
            long engagedFor = fightStartTime < 0
                    ? -1L
                    : now - fightStartTime;

            if (engagedFor < 0 || engagedFor < waitForFirstHit.getValue()) {
                return false;
            }
        }

        if (hitLaterInTrades.getValue() > 0 && lastTradeHitTime >= 0) {
            if (now - lastTradeHitTime < hitLaterInTrades.getValue()) {
                return false;
            }
        }

        boolean willDealDamage;

        switch (preference.getValue()) {

            case Burst -> {
                if (target == null) {
                    return roll((int) cancelMissed.getValue().floatValue());
                }

                willDealDamage = targetCanTakeDamage(target);
            }

            case Crit -> {
                if (player.getDeltaMovement().y > 0) {
                    return false;
                }

                if (player.onGround()) {

                    if (target == null) {
                        return roll((int) cancelMissed.getValue().floatValue());
                    }

                    willDealDamage = targetCanTakeDamage(target);

                } else {

                    if (disableDuringKB.getValue() && player.hurtTime > 0) {
                        return false;
                    }

                    if (onlyWhileDamaged.getValue() && !firstHitReceived) {
                        return false;
                    }

                    willDealDamage = player.getDeltaMovement().y < 0;
                }
            }

            case Combo -> {
                boolean moving = MoveUtils.isMoving();

                willDealDamage = player.hurtTime > 0 && !player.onGround() && moving;
            }

            case Timer -> {
                int min = delayMin.getValue().intValue();
                int max = delayMax.getValue().intValue();

                long delay = max > min
                        ? ThreadLocalRandom.current().nextLong(min, max + 1)
                        : min;

                return lastAttackTime < 0
                        || now - lastAttackTime >= delay;
            }

            default -> willDealDamage = true;
        }

        return willDealDamage || roll((int) (inCombat
                ? cancelInCombat.getValue().floatValue()
                : cancelMissed.getValue().floatValue()));
    }

    private boolean targetCanTakeDamage(Entity target) {
        return target.invulnerableTime <= 0;
    }

    private boolean roll(int rate) {
        return ThreadLocalRandom.current().nextInt(100) >= rate;
    }

    public void notifyMissedSwing() {
        lastAttackTime = System.currentTimeMillis();
    }

    public boolean shouldFakeSwing() {
        return fakeSwing.getValue() && isEnabled() && !currentShouldAttack;
    }

    public boolean canAutoAttack() {
        if (!isEnabled() || mode.getValue() == HitMode.Active) {
            return true;
        }
        return currentShouldAttack;
    }

    public boolean shouldCancelAttack() {
        if (!isEnabled() || mode.getValue() != HitMode.Active) {
            return false;
        }

        return !currentShouldAttack;
    }

    @Override
    public String getFinalSuffix() {
        int rate = mc.crosshairPickEntity != null
                ? cancelInCombat.getValue().intValue()
                : cancelMissed.getValue().intValue();

        return preference.getValue().name() + " " + rate + "%";
    }
}