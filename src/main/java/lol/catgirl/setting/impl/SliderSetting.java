package lol.catgirl.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lol.catgirl.setting.Setting;
import lombok.Getter;

@Getter
public class SliderSetting extends Setting<Float> {

    private final float min;
    private final float max;
    private final float step;

    public SliderSetting(String name, float value, float min, float max, float step) {
        super(name, value);
        this.min = min;
        this.max = max;
        this.step = step;

        setValue(value);
    }

    @Override
    public void setValue(Float value) {
        value = Math.max(min, Math.min(max, value));

        value = Math.round(value / step) * step;

        super.setValue(value);
    }

    public void increase() {
        setValue(getValue() + step);
    }

    public void decrease() {
        setValue(getValue() - step);
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
            setValue(json.getAsFloat());
        } catch (Exception ignored) {
        }
    }
}
