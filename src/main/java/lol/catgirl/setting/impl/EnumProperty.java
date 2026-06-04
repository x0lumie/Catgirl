package lol.catgirl.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lol.catgirl.setting.Property;

public class EnumProperty<E extends Enum<E>> extends Property<E> {

    private final E[] values;

    public EnumProperty(String name, E defaultValue) {
        super(name, defaultValue);
        this.values = defaultValue.getDeclaringClass().getEnumConstants();
    }

    @Override
    public void setValue(E value) {
        if (value != null) {
            super.setValue(value);
        }
    }

    @SuppressWarnings("unchecked")
    public void setValueByEnum(Enum<?> value) {
        for (E mode : values) {
            if (mode.equals(value)) {
                setValue(mode);
                return;
            }
        }
    }

    public void next() {
        E current = getValue();
        int index = current.ordinal();

        if (index < values.length - 1) {
            setValue(values[index + 1]);
        } else {
            setValue(values[0]);
        }
    }

    public void previous() {
        E current = getValue();
        int index = current.ordinal();

        if (index > 0) {
            setValue(values[index - 1]);
        } else {
            setValue(values[values.length - 1]);
        }
    }

    public boolean is(E mode) {
        return getValue() == mode;
    }

    public E[] getModes() {
        return values;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue().name());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return;
        }

        try {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                String name = json.getAsString();

                for (E mode : values) {
                    if (mode.name().equalsIgnoreCase(name)) {
                        setValue(mode);
                        return;
                    }
                }
            }

            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
                int index = json.getAsInt();

                if (index >= 0 && index < values.length) {
                    setValue(values[index]);
                }
            }

        } catch (Exception ignored) {
        }
    }
}