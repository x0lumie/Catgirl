package lol.catgirl.module.ghost;

import com.mojang.blaze3d.platform.InputConstants;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.KeyEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.KeyUtil;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import org.lwjgl.glfw.GLFW;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class LungeModule extends Module {
    public static final LungeModule INSTANCE = new LungeModule();

    private int lastSlot = -1;
    private boolean swapped = false;
    private int ticks = 0;
    private int stuntick = 0;
    private boolean toggleRequested = false;

    public LungeModule() {
        super("Lunge",
                "Automatically lunges with a spear.",
                ModuleCategory.Ghost
        );
        addSetting(swapTick);
    }

    private final SliderProperty swapTick = new SliderProperty(
            "Swap tick",
            2, 0, 5, 1
    );

    private final InputConstants.Key AttackKey = InputConstants.getKey("key.mouse.left");

    @EventHook
    public void onKey(KeyEvent event) {
        if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == GLFW.GLFW_KEY_C) {
            if (!toggleRequested && stuntick == 0 && !swapped) {
                toggleRequested = true;
            }
        }
    }

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {

        if (toggleRequested) {
            if (stuntick == 0) {
                float cooldown = mc.player.getAttackStrengthScale(0.5F);

                if (cooldown < 1.0f) {
                    reset();
                    return;
                }


                lastSlot = mc.player.getInventory().getSelectedSlot();
                boolean swap = swapToSpear();

                if (!swap) {
                    reset();
                    return;
                }
                stuntick++;
            }
        }

        if (swapped && lastSlot != -1) {
            int tick = swapTick.getValue().intValue();
            ticks++;
            if (ticks >= tick) {
                reset();
            }
        }
    }

    private boolean swapToSpear() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack != null && !stack.isEmpty() && isLungeSpear(stack)) {
                boolean actuallyUsingItem = mc.player.isUsingItem();

                if (actuallyUsingItem) {
                    reset();
                    return false;
                }

                mc.player.getInventory().setSelectedSlot(i);
                KeyUtil.pressKey(mc.options.keyAttack);
                return (swapped = true);
            }
        }
        return false;
    }

    private boolean isLungeSpear(ItemStack stack) {
        return stack.is(ItemTags.SPEARS) &&
                stack.getEnchantments().entrySet().stream()
                        .anyMatch(entry -> entry.getKey().is(Enchantments.LUNGE));
    }

    private void reset() {
        if (lastSlot != -1) {
            mc.player.getInventory().setSelectedSlot(lastSlot);
            swapped = false;
        }
        lastSlot = -1;
        stuntick = 0;
        ticks = 0;
        toggleRequested = false;
    }
}