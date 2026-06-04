package lol.catgirl.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class Command {
    private final String name, description, alias;

    public abstract void execute(String[] arguments);

    public boolean matches(String input) {
        if(input.equalsIgnoreCase(getName())) return true;
        String alias = getAlias();
        return input.equalsIgnoreCase(alias);
    }
}
