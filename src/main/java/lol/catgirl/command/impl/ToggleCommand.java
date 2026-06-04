package lol.catgirl.command.impl;

import lol.catgirl.command.Command;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.Module;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "Toggle modules by commands, Myau style!", "t");
    }

    @Override
    public void execute(String[] args) {

        if (args.length == 0) {
            return;
        }

        final String moduleName = args[0];
        final Module module = ModuleManager.getInstance().getModuleByName(moduleName);

        if (module != null) {
            module.toggle();
        }
    }
}
