package lol.catgirl.module.client;

import com.google.gson.JsonArray;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.utils.client.NetworkUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class AutoTranslateModule extends Module {

    public static final AutoTranslateModule INSTANCE = new AutoTranslateModule();

    private final Executor executor = Executors.newSingleThreadExecutor();

    public enum Mode {
        Delay, Resend
    }

    public enum TargetLocaleMode {
        English, Spanish, French,
        German, Italian, Portuguese,
        Russian, Chinese,
        Japanese, Korean, Arabic
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Delay);

    public final EnumProperty<TargetLocaleMode> targetLocaleMode = new EnumProperty<>("Locale", TargetLocaleMode.English);

    public AutoTranslateModule() {
        super("AutoTranslate",
                "Automatically translates chat messages into your preferred locale.",
                ModuleCategory.Client);
        addSettings(mode, targetLocaleMode);
    }

    @EventHook
    public void onPacketReceive(PacketReceivedEvent event) {
        if (mc.player == null || mc.level == null) return;

        String text = extractChat(event.packet);
        if (text == null || text.isEmpty()) return;
        if (text.contains("\n")) return;

        switch (mode.getValue()) {

            case Delay -> {
                event.setCancelled(true);
                translateAndSend(text);
            }

            case Resend -> translateAndSend(text);
        }
    }

    private String extractChat(Object packet) {

        if (packet instanceof ClientboundPlayerChatPacket p) {
            // modern player chat
            return p.body().content().toString();
        }

        if (packet instanceof ClientboundSystemChatPacket p) {
            return p.content().getString();
        }

        if (packet instanceof ClientboundDisguisedChatPacket p) {
            return p.message().getString();
        }

        return null;
    }

    private void translateAndSend(String text) {

        executor.execute(() -> {
            try {
                String targetLang = targetLocaleMode.getValue().name().toLowerCase();

                String url = "https://translate.googleapis.com/translate_a/single?client=gtx"
                                + "&sl=auto" + "&tl=" + targetLang + "&dt=t&q=" +
                        URLEncoder.encode(text, StandardCharsets.UTF_8);

                JsonArray array = NetworkUtils.requestAsGsonArray(url);

                String translated = array
                        .get(0).getAsJsonArray()
                        .get(0).getAsJsonArray()
                        .get(0).getAsString();

                MutableComponent message = Component.literal(translated);

                if (!translated.equals(text)) {
                    message.append(" §7[T]");
                }

                mc.player.displayClientMessage(message, false);

            } catch (Exception ignored) {}
        });
    }

    @Override
    protected String getFinalSuffix() {
        return targetLocaleMode.getValue().toString();
    }
}