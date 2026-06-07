package lol.catgirl.module.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PostTickEvent;
import lol.catgirl.event.impl.WorldJoinEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;

public final class SpammerModule extends Module {
    public static final SpammerModule INSTANCE = new SpammerModule();

    public final EnumProperty<Mode> mode = new EnumProperty<>("Text Mode", Mode.Preset);

    public final SliderProperty delay = new SliderProperty("Delay", 20, 0, 200, 1);
    public final BoolProperty autoDisable = new BoolProperty("Auto Disable", true);
    public final BoolProperty bypass = new BoolProperty("Bypass", true);
    public final SliderProperty bypassLength = new SliderProperty("Bypass Length", 16, 1, 256, 1).hide(() -> !bypass.getValue());

    public enum Mode {
        Preset,
        Custom
    }

    public SpammerModule() {
        super("Spammer",
                "Sends messages of your choosing in chat.",
                ModuleCategory.Client
        );
        addSettings(mode, delay, autoDisable, bypass, bypassLength);
    }

    @EventHook
    public void onWorldChange(WorldJoinEvent event) {
        if(mc.player == null || mc.level == null || !this.isEnabled()) {
            return;
        }

        if (autoDisable.getValue()) {
//            EquinoxClient.sendChatMessage("Spammer has been disabled due to world change.");
            toggle();
        }
    }

    private int timer;

    public String message() {
        switch (mode.getValue()) {
            case Preset -> {
                return "Catgirl Client on top!";
            }

            case Custom -> {
                try {
                    File dir = new File(mc.gameDirectory, "Catgirl");

                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    File file = new File(dir, "SpammerText.txt");

                    if (!file.exists()) {
                        file.createNewFile();

                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write("Custom spam message here");
                        }
                    }

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        StringBuilder builder = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line).append("\n");
                        }

                        return builder.toString().trim();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return "Failed to read spammerText.txt";
                }
            }
        }

        return "";
    }

    @EventHook
    public void onTick(PostTickEvent event) {
        if(mc.player == null || mc.level == null || !this.isEnabled()) {
            return;
        }

        if(timer <= 0) {
            String text = message();

            if(bypass.getValue()) {
                String random = randomAlphabetic(bypassLength.getValue().intValue());
                text += " " + random;
            }

            if(text.length() > 256) {
                text = text.substring(0, 256);
            }

            mc.player.connection.sendChat(text);

            timer = delay.getValue().intValue();
        } else {
            timer--;
        }
    }

    private String randomAlphabetic(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }
}
