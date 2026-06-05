package lol.catgirl.module.client;

import lol.catgirl.property.impl.BoolProperty;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class AntiBotModule extends Module {

    public static final AntiBotModule INSTANCE = new AntiBotModule();

    public final BoolProperty checkInTablist = new BoolProperty("Tablist Check", true);
    public final BoolProperty checkValidName = new BoolProperty("Check Valid Name", true);
    public final BoolProperty checkAge = new BoolProperty("Check Age", true);
    public final BoolProperty checkLatency = new BoolProperty("Check Latency", true);

    private static final String VALID_USERNAME_REGEX = "^[a-zA-Z0-9_]{1,16}$";

    public AntiBotModule() {
        super("AntiBot",
                "Filters un-needed entities.",
                ModuleCategory.Client
        );
        addSettings(checkInTablist, checkValidName, checkAge, checkLatency);
    }

    public boolean isBot(Player player) {
        if (player == null || mc.player == null)
            return true;

        if (player == mc.player)
            return false;

        PlayerInfo entry = mc.getConnection().getPlayerInfo(player.getUUID());

        if (checkAge.getValue() && player.tickCount < 20)
            return true;

        if (checkInTablist.getValue() && entry == null)
            return true;

        if (checkLatency.getValue() && entry != null && entry.getLatency() == 0)
            return true;

        if (checkValidName.getValue() && entry != null) {
            String name = entry.getProfile().name();

            return name == null ||
                    !name.matches(VALID_USERNAME_REGEX) ||
                    name.contains(" ") ||
                    name.contains("NPC");
        }

        return false;
    }


}
