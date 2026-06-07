package lol.catgirl.module.client;


import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketReceivedEvent;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class AutoAuthModule extends Module {
    public static final AutoAuthModule INSTANCE = new AutoAuthModule();

    public AutoAuthModule() {
        super("AutoAuth",
                "Automatically authenticates on cracked servers.",
                ModuleCategory.Client
        );
    }

    private static final String[] REGISTER_KEYWORDS = {
            "/register",    // English
            "/registrar",   // Spanish/Portuguese
            "/reg",         // Short form
            "/зарег",       // Russian (zareg)
            "/rejestracja", // Polish
            "/cadastrar",   // Portuguese
            "/kayit",       // Turkish
            "/enregistrer"  // French
    };

    private static final String[] LOGIN_KEYWORDS = {
            "/login",
            "/войти",
            "/ingresar",
            "/connexion"
    };

    private boolean registered, loggedIn;

    private String password = "aMR93R3Q4WGg4ofgmordv";

    @EventHook
    public void onPacket(PacketReceivedEvent event) {
        if (mc.player == null) return;

        if (!(event.packet instanceof ClientboundSystemChatPacket packet)) return;
        String message = packet.content().getString().toLowerCase();

        if (!registered) {
            for (String keyword : REGISTER_KEYWORDS) {
                if (message.contains(keyword)) {
                    mc.player.connection.sendCommand(
                            keyword.substring(1) + " " +
                                    password + " " +
                                    password
                    );
                    registered = true;
                    toggle();
                    return;
                }
            }
        }

        for (String keyword : LOGIN_KEYWORDS) {
            if (message.contains(keyword)) {
                mc.player.connection.sendCommand(
                        "login " + password
                );
                loggedIn = true;
                return;
            }
        }
    }

    @Override
    public void onDisable() {
        registered = false;
        loggedIn = false;
    }
}
