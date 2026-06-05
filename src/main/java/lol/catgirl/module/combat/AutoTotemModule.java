package lol.catgirl.module.combat;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.EnumProperty;
import lol.catgirl.setting.impl.SliderProperty;
import lol.catgirl.utils.player.inventory.InventoryUtils;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AutoTotemModule extends Module {
    public static final AutoTotemModule INSTANCE = new AutoTotemModule();

    public enum Mode {
        Auto, Legit
    }

    public final EnumProperty<Mode> mode =
            new EnumProperty<>("Mode", Mode.Auto);

    public final SliderProperty delay = new SliderProperty("Delay", 200, 0, 500, 25).hide(()-> !(mode.getValue() == Mode.Legit));

    public AutoTotemModule() {
        super("AutoTotem",
                "Immediately equips a totem in pvp",
                ModuleCategory.Combat
        );
        addSettings(mode, delay);
    }

    private static final int OFFHAND = 45;

    private long lastSwap;
    private boolean waitingForInventory;
    private int targetSlot = -1;

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null || !this.isEnabled()) {
            return;
        }

        if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            return;
        }

        switch (mode.getValue()) {

            case Auto -> {
                int slot = findTotem();

                if (slot == -1) {
                    return;
                }

                InventoryUtils.move(slot, OFFHAND);
            }

            case Legit -> {
                if (System.currentTimeMillis() - lastSwap < delay.getValue().longValue()) {
                    return;
                }

                if (!(mc.screen instanceof InventoryScreen) && !waitingForInventory) {

                    targetSlot = findTotem();

                    if (targetSlot == -1) {
                        return;
                    }

                    mc.setScreen(new InventoryScreen(mc.player));

                    waitingForInventory = true;
                    return;
                }

                if (mc.screen instanceof InventoryScreen && waitingForInventory) {

                    InventoryUtils.move(targetSlot, OFFHAND);

                    mc.player.closeContainer();

                    waitingForInventory = false;
                    lastSwap = System.currentTimeMillis();
                }
            }
        }
    }

    private int findTotem() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);

            if (stack.is(Items.TOTEM_OF_UNDYING)) {

                if (i < 9) {
                    i += 36;
                }

                return i;
            }
        }

        return -1;
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
