package lol.catgirl.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lol.catgirl.Catgirl;
import lol.catgirl.file.Serializable;
import lol.catgirl.module.client.InterfaceModule;
import lol.catgirl.setting.Property;
import lol.catgirl.setting.impl.BoolProperty;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.keybind.KeybindRegistry;
import lol.catgirl.utils.keybind.Keybindable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
@Setter
@AllArgsConstructor
public class Module implements Toggleable, IMinecraft, Keybindable, Serializable {
    private final String name;
    private final ModuleCategory category;
    private final String description;
    private boolean enabled = false;
    private boolean expanded;
    private int key;
    private String suffix;

    public BoolProperty isVisible = new BoolProperty("Is Visible", true);

    public Module(String name, String description, ModuleCategory category) {
        this.name = name;
        this.category = category;
        this.description = description;
        KeybindRegistry.subscribe(this);
        addSetting(isVisible);
    }

    @Getter
    @Setter
    public ArrayList<Property<?>> properties = new ArrayList<>();

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void setEnabled(boolean enabled) {
        if(this.enabled != enabled) {
            this.enabled = enabled;

            if (enabled) {
                onEnable();
                Catgirl.INSTANCE.eventBus.subscribe(this);

            } else {
                onDisable();
                Catgirl.INSTANCE.eventBus.unsubscribe(this);

            }
        }
    }

    @Override
    public void toggle() {
        setEnabled(!enabled);
    }

    public int setKey() {
        return key;
    }

    @Override
    public void onBindPress() {
        toggle();
    }

    @Override
    public String getKeybindId() {
        return name;
    }

    //AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
    public final String getSuffix() {
        String raw = getFinalSuffix();
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        return format(raw);
    }

    protected String getFinalSuffix() {
        return this.suffix;
    }

    public void addSetting(Property<?> property) {
        properties.add(property);
    }

    @SafeVarargs
    public final void addSettings(Property<?>... properties) {
        this.properties.addAll(Arrays.asList(properties));
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", enabled);
        object.addProperty("expanded", expanded);
        object.addProperty("key", key);

        JsonObject settingsObject = new JsonObject();
        for (Property<?> property : properties) {
            settingsObject.add(property.getName(), property.toJson());
        }
        object.add("settings", settingsObject);

        return object;
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json == null || json.isJsonNull() || !json.isJsonObject()) {
            return;
        }

        JsonObject object = json.getAsJsonObject();

        JsonElement keyElement = object.get("key");
        if (keyElement != null && keyElement.isJsonPrimitive()) {
            try {
                setKey(keyElement.getAsInt());
            } catch (Exception ignored) {
            }
        }

        JsonElement expandedElement = object.get("expanded");
        if (expandedElement != null && expandedElement.isJsonPrimitive()) {
            try {
                expanded = expandedElement.getAsBoolean();
            } catch (Exception ignored) {
            }
        }

        JsonObject settingsObject = null;
        JsonElement settingsElement = object.get("settings");
        if (settingsElement != null && settingsElement.isJsonObject()) {
            settingsObject = settingsElement.getAsJsonObject();
        }

        for (Property<?> property : properties) {
            JsonElement settingElement = null;

            if (settingsObject != null && settingsObject.has(property.getName())) {
                settingElement = settingsObject.get(property.getName());
            } else if (object.has(property.getName())) {
                settingElement = object.get(property.getName());
            }

            if (settingElement != null) {
                try {
                    property.fromJson(settingElement);
                } catch (Exception e) {
                    Catgirl.LOGGER.info("Failed to load setting {} for module:" + property.getName() +" "+ name);
                }
            }
        }

        JsonElement enabledElement = object.get("enabled");
        if (enabledElement != null && enabledElement.isJsonPrimitive()) {
            try {
                boolean shouldBeEnabled = enabledElement.getAsBoolean();

                if (shouldBeEnabled != this.enabled) {
                    this.toggle();
                }
            } catch (Exception ignored) {
            }
        }
    }

    // sorry cpu );
    public String getDisplayName() {
        return format(name);
    }

    private String format(String text) {
        if (text == null || text.isEmpty()) return "";

        InterfaceModule.NamingStyle style = InterfaceModule.INSTANCE.namingStyle.getValue();

        String spaced = text.replaceAll("([a-z])([A-Z])", "$1 $2");

        return switch (style) {
            case Lowercase -> text.toLowerCase();
            case LowercaseSpaced -> spaced.toLowerCase();
            case Normal -> text;
            case NormalSpaced -> spaced;
        };
    }
}
