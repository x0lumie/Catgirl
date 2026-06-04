package lol.catgirl.command.impl;

import lol.catgirl.Catgirl;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.Module;
import lol.catgirl.command.Command;
import lol.catgirl.setting.Setting;
import lol.catgirl.setting.impl.*;

import java.util.Optional;

public class ModuleCommand extends Command {
    public ModuleCommand() {
        super("module", "Manage settings with commands.", "m");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            Catgirl.sendChatMessage("Usage: .<module> <setting> <value>");
            return;
        }

        String moduleName = args[0];
        String settingName = args[1];
        String value = args[2];

        Optional<Module> module = Optional.ofNullable(ModuleManager.getInstance().getModuleByName(moduleName));

        if (module.isEmpty()) {
            Catgirl.sendChatMessage("Module not found: " + moduleName);
            return;
        }

        Setting<?> targetBaseSetting = null;

        String normalizedInput = normalize(settingName);

        for (Setting<?> baseSetting : module.get().getSettings()) {
            if (normalize(baseSetting.getName()).equals(normalizedInput)) {
                targetBaseSetting = baseSetting;
                break;
            }
        }

        if (targetBaseSetting == null) {
            Catgirl.sendChatMessage("Setting not found: " + settingName);
            return;
        }

        try {

            switch (targetBaseSetting) {
                case BoolSetting booleanSetting -> booleanSetting.setValue(Boolean.parseBoolean(value));
                case SliderSetting numberSetting -> {

                    float parsedValue = Float.parseFloat(value);

                    numberSetting.setValue(parsedValue);
                }
                case @SuppressWarnings("all")EnumSetting enumSetting -> {
                    @SuppressWarnings("all")
                    final var current = (Enum) enumSetting.getValue();
                    final var enumClass = current.getDeclaringClass();

                    Enum<?> newValue = null;

                    for (Enum<?> constant : (Enum<?>[]) enumClass.getEnumConstants()) {
                        if (constant.name().equalsIgnoreCase(value)) {
                            newValue = constant;
                            break;
                        }
                    }

                    if (newValue == null) {
                        Catgirl.sendChatMessage("Invalid mode");
                        for (Enum<?> constant : (Enum<?>[]) enumClass.getEnumConstants()) {
                            Catgirl.sendChatMessage("§7- " + constant.name());
                        }
                        return;
                    }

                    enumSetting.setValue(newValue);
                }
                default -> {
                    Catgirl.sendChatMessage("Unsupported setting type.");
                    return;
                }
            }

            Catgirl.sendChatMessage("§aSet §7" + module.get().getName() + " §a" + settingName + " §ato §7" + value);

        } catch (Exception e) {
            Catgirl.sendChatMessage("Invalid value for setting.");
        }
    }

    private String normalize(String input) {
        return input
                .replace(" ", "")
                .replace("_", "")
                .toLowerCase();
    }
}
