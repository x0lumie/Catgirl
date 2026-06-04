package lol.catgirl.setting;

import lol.catgirl.file.Serializable;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

@Getter
@Setter
public abstract class Setting<T> implements Serializable {
    private final String name;
    private T value;

    public Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    private BooleanSupplier hidden = () -> false;

    public boolean isHidden() {
        return hidden.getAsBoolean();
    }

    @SuppressWarnings("unchecked")
    public <I extends Setting<?>> I hide(BooleanSupplier hidden) {
        this.hidden = hidden;
        return (I) this;
    }
}
