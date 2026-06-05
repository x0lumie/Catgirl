package lol.catgirl.command.impl;

import java.io.File;
import java.util.Arrays;

import lol.catgirl.command.Command;
import lol.catgirl.Catgirl;
import lol.catgirl.file.impl.ModulesFile;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", "Manage your client configs.", "c");
    }

    @Override
    public void execute(String[] args) {
        if(args.length == 0) {
            Catgirl.sendChatMessage("Usage: .config save/load/list/delete");
            return;
        } 

        String sub = args[0].toLowerCase(); 

        switch(sub) {
            case "save": {
                if(args.length < 2) {
                    Catgirl.sendChatMessage("Usage: .config save <config name>");
                    return;
                } 

                String name = args[1];
                new ModulesFile(name).saveToFile();
                Catgirl.sendChatMessage("Successfully saved config " + name + "!");
                break;
            } 

            case "load": {
                if(args.length < 2) {
                    Catgirl.sendChatMessage("Usage: .config load <config name>");
                    return;
                }                  

                String name = args[1]; 
                if(doesFileExist(name)) {
                    new ModulesFile(name).loadFromFile();
                    Catgirl.sendChatMessage("Successfully loaded config " + name + "!");
                } else {
                    Catgirl.sendChatMessage("The config profile " + name + " does not exist.");
                }
                break;
            } 

            case "list": {
                listConfigs();
                break;
            } 

            case "delete": {
                if (args.length < 2) {
                    Catgirl.sendChatMessage("Usage: .config delete <config name>");
                    return;
                }                      

                String name = args[1]; 
                if (deleteConfig(name)) {
                    Catgirl.sendChatMessage("Successfully deleted config profile " + name +".");
                } else {
                    Catgirl.sendChatMessage("The config " + name + " does not exist.");
                }
                break;
            } 

            default:
                Catgirl.sendChatMessage("Usage: .config delete <config name>");
        }
    } 

    private void listConfigs() {
        File dir = ModulesFile.BASE_DIRECTORY.resolve("configs/").toFile();

        File[] files = dir.listFiles((d, n) -> n.endsWith(".json")); 
        if(files == null || files.length == 0) {
            Catgirl.sendChatMessage("No configs found. ):");
            return;
        }

        Catgirl.sendChatMessage("Configs:");
        Arrays.stream(files)
        .forEach(f -> Catgirl.sendChatMessage(f.getName().replace(".json", "")));
    }

    private boolean deleteConfig(String name) {
        File dir = ModulesFile.BASE_DIRECTORY.resolve("configs/").toFile();
        File file = new File(dir, name + ".json"); 
        return file.exists() && file.delete();
    } 

    private boolean doesFileExist(String name) {
        File dir = ModulesFile.BASE_DIRECTORY.resolve("configs/").toFile();
        File file = new File(dir, name + ".json");
        return file.exists();
    } 
}
