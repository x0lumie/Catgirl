package lol.catgirl.manager;

import lol.catgirl.Catgirl;
import lol.catgirl.command.Command;
import lol.catgirl.command.impl.*;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ChatEvent;
import lombok.Getter;

import java.util.*;

public class CommandManager {

    @Getter
    private final Map<String, Command> commands = new HashMap<>();

    public final BindCommand bindCommand = new BindCommand();
    public final ToggleCommand toggleCommand = new ToggleCommand();
    public final HelpCommand helpCommand = new HelpCommand();
    public final ModuleCommand moduleCommand = new ModuleCommand();
    public final ConfigCommand configCommand = new ConfigCommand();
    public final FriendCommand friendCommand = new FriendCommand();

    public CommandManager() {
        addCommands(
                bindCommand,
                toggleCommand,
                helpCommand,
                moduleCommand,
                configCommand,
                friendCommand
        );
    }

    public void addCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public void addCommands(Command... commands) {
        for (Command command : commands) {
            addCommand(command);
        }
    }

    public boolean processor(String input) {
        if(input == null || input.isBlank()) {
            return false;
        }

        if(!input.startsWith(getSuffix())) {
            return false;
        }
        input = input.substring(getSuffix().length()).trim();
        String[] parts = input.split("\\s+");
        String commandName = parts[0];
        Command command = null;

        for (Command cmd : commands.values()) {
            if(cmd.matches(commandName)) {
                command = cmd;
                break;
            }
        }

        if(command == null) {
            Catgirl.sendChatMessage("Command does not exist.");
            return true;
        }

        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
        command.execute(args);
        return true;
    }

    public boolean handleMessages(String text) {
        return text.startsWith(getSuffix()) && processor(text);
    }


    @EventHook
    public void onChatMessage(ChatEvent event) {
        if (handleMessages(event.context)) {
            event.setCancelled(true);
        }
    }

    public static String getSuffix() {
        return ".";
    }
}
