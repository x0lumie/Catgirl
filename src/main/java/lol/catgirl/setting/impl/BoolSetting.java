package lol.catgirl.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lol.catgirl.setting.Setting;

public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, Boolean value) {
        super(name, value);
    }

    public void toggle() {
        setValue(!getValue());
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json == null || json.isJsonNull() || !json.isJsonPrimitive()) {
            return;
        }

        try {
            setValue(json.getAsBoolean());
        } catch (Exception ignored) {
        }
    }
}
