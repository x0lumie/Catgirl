package lol.catgirl.command.impl;


import lol.catgirl.Catgirl;
import lol.catgirl.command.Command;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "HELPP MEEE!!!!!!!!", "?");
    }

    @Override
    public void execute(String[] args) {
        for (Command command : Catgirl.INSTANCE.commandManager.getCommands().values()) {
            Catgirl.sendChatMessage(command.getName() + " - " + command.getDescription() + " ["+command.getAlias()+"]");
        }
    }
}
