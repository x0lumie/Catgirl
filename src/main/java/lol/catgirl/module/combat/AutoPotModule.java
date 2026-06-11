package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.EventPriority;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PlayerRotationEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public final class AutoPotModule extends Module {
    public static final AutoPotModule INSTANCE = new AutoPotModule();

    public final SliderProperty delay = new SliderProperty("Delay", 200, 0, 1000, 25);
    public final SliderProperty healthThreshold = new SliderProperty("Health Threshold", 14, 1, 20, 0.5f);
    public final SliderProperty rotationSpeed = new SliderProperty("Rotation Speed", 10, 1, 10, 0.5f);

    public AutoPotModule() {
        super("AutoPot", "Automatically throws buff splash potions when low.", ModuleCategory.Combat);
        addSettings(delay, healthThreshold, rotationSpeed);
    }

    private long lastThrow = 0L;

    private boolean overrideRotations = false;
    private float targetPitch = 90f;

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.screen != null | !isEnabled()) return;

        if (mc.player.getHealth() > healthThreshold.getValue()) return;
        if (System.currentTimeMillis() - lastThrow < delay.getValue().longValue()) return;

        int slot = findPotionInHotbar();

        if (slot == -1) {
            return;
        }

        throwPotion(slot);
    }

    private void throwPotion(int hotbarSlot) {
        if (mc.player == null || mc.gameMode == null) return;

        int previousSlot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(hotbarSlot);

        targetPitch = 90f;
        overrideRotations = true;

        mc.gameMode.useItem(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND);

        mc.player.getInventory().setSelectedSlot(previousSlot);
        overrideRotations = false;
        lastThrow = System.currentTimeMillis();
    }

    @EventHook(priority = EventPriority.HIGHEST)
    public void onPlayerRotation(PlayerRotationEvent event) {
        if (!overrideRotations) return;
        RotationUtils.setRotationSpeed(rotationSpeed.getValue());
        event.pitch = targetPitch;
    }

    // ─── potion detection ────────────────────────────────────────────────────────

    private int findPotionInHotbar() {
        for (int i = 0; i < 9; i++) {
            if (isBuff(mc.player.getInventory().getItem(i))) return i;
        }
        return -1;
    }

    private boolean isBuff(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.is(Items.SPLASH_POTION)) return false;

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return false;

        // check registered potion type — compare Holder<Potion> directly
        var potion = contents.potion();
        if (potion.isPresent()) {
            var p = potion.get();
            if (p == Potions.HEALING
                    || p == Potions.STRONG_HEALING
                    || p == Potions.REGENERATION
                    || p == Potions.STRONG_REGENERATION
                    || p == Potions.LONG_REGENERATION
                    || p == Potions.SWIFTNESS
                    || p == Potions.STRONG_SWIFTNESS
                    || p == Potions.LONG_SWIFTNESS
                    || p == Potions.STRENGTH
                    || p == Potions.STRONG_STRENGTH
                    || p == Potions.LONG_STRENGTH
                    || p == Potions.FIRE_RESISTANCE
                    || p == Potions.LONG_FIRE_RESISTANCE) {
                return true;
            }
        }

        // fallback: check raw effects — compare Holder<MobEffect> directly
        for (var effect : contents.getAllEffects()) {
            var type = effect.getEffect();
            if (type == MobEffects.HEALTH_BOOST
                    || type == MobEffects.REGENERATION
                    || type == MobEffects.SPEED
                    || type == MobEffects.STRENGTH
                    || type == MobEffects.FIRE_RESISTANCE
                    || type == MobEffects.ABSORPTION
                    || type == MobEffects.SATURATION) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected String getFinalSuffix() {
        return String.valueOf(delay.getValue().intValue());
    }
}