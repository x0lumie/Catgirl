package lol.catgirl.module.client;


import lol.catgirl.Catgirl;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.ui.notification.Notification;
import lol.catgirl.ui.notification.NotificationManager;
import lol.catgirl.utils.Utils;
import lombok.SneakyThrows;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;

public class StaffDetectorModule extends Module {
    public static final StaffDetectorModule INSTANCE = new StaffDetectorModule();

    public final BoolProperty warning = new BoolProperty("Warning Sound", true);
    private final List<String> staffs = new ArrayList<>();

    public StaffDetectorModule() {
        super("StaffDetector",
                "Alerts you on staff using the LiquidBounce staff-list",
                ModuleCategory.Client
                );
        addSetting(warning);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        load();
    }

    @EventHook
    public void onPacket(PacketSendEvent event) {
        if (event.getPacket() instanceof ClientboundPlayerInfoUpdatePacket packet) {
            if(packet.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.newEntries()) {
                    String name = entry.profile().name();

                    if(staffs.contains(name)) {
                        if (NotificationsModule.INSTANCE.isEnabled() && NotificationsModule.INSTANCE.mode.getValue() == NotificationsModule.Mode.None) {
                            Catgirl.sendChatMessage("Staff Detector has detected " + name + " as staff!");
                        }

                        if (NotificationsModule.INSTANCE.isEnabled() && NotificationsModule.INSTANCE.mode.getValue() == NotificationsModule.Mode.Exhibition) {
                            NotificationManager.post(this.getName(), "Staff " + name + " is in your game!", Notification.Type.WARNING);
                        }

                        if(warning.getValue()) {
                            Utils.warningSound();
                        }
                    }
                }
            }
        }
    }

    private static @Language("http-url-reference") @NonNull String coreGithubURL(@Language("http-url-reference") String fileName) {
        return "https://raw.githubusercontent.com/CCBlueX/LiquidCloud/refs/heads/main/LiquidBounce/staffs/" + fileName;
    }

    private String rootDomain(String of) {
        var domain = of.trim().toLowerCase();

        if (domain.endsWith(".")) {
            domain = domain.substring(0, -1);
        }

        final var parts = domain.split("\\.");
        if (parts.length <= 2) {
            return domain;
        }

        return parts[(parts.length - 1) - 1] + "." + parts[parts.length - 1];
    }

    @SneakyThrows
    private static URL url(@Language("http-url-reference") String x) {
        return new URI(x).toURL();
    }

    private void load() {
        staffs.clear();
        final var curServer = mc.getCurrentServer();
        if (curServer == null) return;
        final var addr = curServer.ip;
        @Language("http-url-reference") final var addrNormalized = rootDomain(dropPort(addr));

        @Language("http-url-reference") final var urlString = coreGithubURL(addrNormalized);

        new Thread(() -> {
            try {
                URL url = url(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int status = connection.getResponseCode();
                if (status != 200) {
                    Catgirl.sendChatMessage("Failed to download staff list: HTTP " + status);
                    return;
                }

                try (final var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    staffs.addAll(reader.lines().toList());
                }
            } catch (IOException e) {
                Catgirl.sendChatMessage("Error downloading staff list: " + e.getMessage());
            }
        }).start();
    }

    private static @NonNull String dropPort(@NonNull String addr) {
        final var idx = addr.indexOf(':');
        if (idx == -1) return addr;
        return addr.substring(0, idx);
    }
}
