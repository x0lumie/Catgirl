package lol.catgirl.module.ghost;

import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.*;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.NotificationsModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.ui.notification.Notification;
import lol.catgirl.ui.notification.NotificationManager;
import lol.catgirl.utils.client.TickingTimer;
import lol.catgirl.utils.render.RenderUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public final class MurderMysteryModule extends Module {
    public static final MurderMysteryModule INSTANCE = new MurderMysteryModule();

    public final BoolProperty announceMurderer = new BoolProperty("Announce Murderer", false);
    public final SliderProperty announceDelay = new SliderProperty("Announce Delay", 25, 0f, 500f, 1)
            .hide(()->!announceMurderer.getValue());
    ;
    public final BoolProperty alertOnBow = new BoolProperty("Alert Bows", false);
    public final BoolProperty murdererESP = new BoolProperty("Murderer ESP", true);

    private final TickingTimer announceTimer = new TickingTimer();

    public MurderMysteryModule() {
        super("MurderMystery", "Tools for the Murder Mystery gamemode.",
                ModuleCategory.Ghost);
        addSettings(announceMurderer, announceDelay, alertOnBow, murdererESP);
    }

    public static ArrayList<Player> murderers = new ArrayList<>();
    private final ArrayList<Player> bowUsers = new ArrayList<>();

    private final ArrayList<Item> items = new ArrayList<>(Arrays.asList(
            Items.IRON_SWORD, Items.ENDER_CHEST, Items.STONE_SWORD,
            Items.IRON_SHOVEL, Items.STICK, Items.WOODEN_AXE, Items.WOODEN_SWORD,
            Items.DEAD_BUSH, Items.SUGAR_CANE, Items.STONE_SHOVEL,
            Items.BLAZE_ROD, Items.DIAMOND_SHOVEL, Items.QUARTZ,
            Items.PUMPKIN_PIE, Items.GOLDEN_PICKAXE, Items.LEAD,
            Items.NAME_TAG, Items.CHARCOAL, Items.FLINT, Items.BONE, Items.CARROT,
            Items.GOLDEN_CARROT, Items.COOKIE, Items.DIAMOND_AXE, Items.ROSE_BUSH,
            Items.PRISMARINE_SHARD, Items.COOKED_BEEF, Items.NETHER_BRICK, Items.COOKED_CHICKEN,
            Items.MUSIC_DISC_BLOCKS, Items.GOLDEN_HOE, Items.LAPIS_LAZULI, Items.GOLDEN_SWORD,
            Items.DIAMOND_SWORD, Items.DIAMOND_HOE, Items.SHEARS, Items.SALMON, Items.RED_DYE, Items.BREAD,
            Items.OAK_BOAT, Items.GLISTERING_MELON_SLICE, Items.BOOK, Items.JUNGLE_SAPLING, Items.GOLDEN_AXE, Items.DIAMOND_PICKAXE, Items.GOLDEN_SHOVEL
    ));

    private final ArrayList<Item> bowItems = new ArrayList<>(Arrays.asList(
            Items.BOW, Items.CROSSBOW
    ));

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.player == null || mc.level == null) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player) {
                continue;
            }

            if (player.getName().getString().isBlank()) continue;

            Item heldItem = player.getMainHandItem().getItem();

            if (!murderers.contains(player) &&
                    (player.getMainHandItem().getItemName().getString().equalsIgnoreCase("knife")
                            || items.contains(heldItem))) {
                murderers.add(player);

                switch (NotificationsModule.INSTANCE.mode.getValue()) {
                    case Chat -> Catgirl.sendChatMessage("The Murderer is " + player.getName().getString() + "!");
                    case Exhibition -> NotificationManager.post(this.getDisplayName(), "The Murderer is " + player.getName().getString() + "!", Notification.Type.INFO);
                    case None -> {}
                }

                if (announceMurderer.getValue() && announceTimer.hasTimeElapsed(announceDelay.getValue(), true)) {
                    mc.player.connection.sendChat("I think " + player.getName().getString() + " is the murderer!!");
                }
            }

            if (alertOnBow.getValue() && bowItems.contains(heldItem) && !bowUsers.contains(player)) {
                bowUsers.add(player);

                switch (NotificationsModule.INSTANCE.mode.getValue()) {
                    case Chat -> Catgirl.sendChatMessage("The player " + player.getName().getString() + " is holding a bow");
                    case Exhibition -> NotificationManager.post(this.getDisplayName(), "The player " + player.getName().getString() + " is holding a bow", Notification.Type.INFO);
                    case None -> {}
                }
            }
        }
    }

    @EventHook
    public void onRender(Render3DEvent event) {
        Color murdererColor = Color.RED;

        for (Player player : mc.level.players()) {
            if (player == mc.player || !murderers.contains(player)) continue;

            RenderUtils.renderBoxC(player, event, event.partialTicks, murdererColor);
        }
    }

    @EventHook
    public void onWorldJoin(WorldJoinEvent event) {
        murderers.clear();
        bowUsers.clear();
        announceTimer.reset();
    }
}
