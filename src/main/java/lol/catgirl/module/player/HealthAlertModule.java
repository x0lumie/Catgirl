package lol.catgirl.module.player;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.Utils;

public class HealthAlertModule extends Module {
    public static final HealthAlertModule INSTANCE = new HealthAlertModule();

    public final SliderProperty minHealth = new SliderProperty("Min. Health", 4f, 0, 20f, 0.1f);
    public final SliderProperty minSoups = new SliderProperty("Min. Soups", 3f, 0f, 36f, 1);

    private boolean alerted = false;

    public HealthAlertModule() {
        super("HealthAlert", "Alerts you on your health or soups.", ModuleCategory.Player);
        addSettings(minHealth, minSoups);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null) return;

        float health = mc.player.getHealth();
        int soups = countSoups();

        boolean lowHealth = health <= minHealth.getValue();
        boolean lowSoup = soups <= minSoups.getValue();

        if (!lowHealth && !lowSoup) {
            alerted = false;
            return;
        }

        if (alerted) return;
        alerted = true;

        if (lowHealth) {
            Catgirl.sendChatMessage("Your health is low!");
//            NotificationRenderer.INSTANCE.push("Health Alert", "Your health is low!");
        }

        if (lowSoup) {
            Catgirl.sendChatMessage("You're low on soup! (" + soups + " left)");
//            NotificationRenderer.INSTANCE.push("Health Alert", "You're low on soup! (" + soups + " left)");

        }

        Utils.warningSound();
    }

    private int countSoups() {
        int count = 0;

        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            if (!mc.player.getInventory().getItem(i).isEmpty()
                    && mc.player.getInventory().getItem(i).getItem() == net.minecraft.world.item.Items.MUSHROOM_STEW) {
                count += mc.player.getInventory().getItem(i).getCount();
            }
        }

        return count;
    }
}
