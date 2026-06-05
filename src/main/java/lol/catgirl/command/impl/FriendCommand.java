package lol.catgirl.command.impl;

import lol.catgirl.Catgirl;
import lol.catgirl.command.Command;
import lol.catgirl.manager.FriendManager;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friends", "Manage your client friends", "f");
    } 

    @Override
    public void execute(String[] args) {
        if(args.length < 1) {
            Catgirl.sendChatMessage("Usage: .friends <add/remove/list/clear>");
            return;
        } 

        String action = args[0].toLowerCase();

        switch(action) {
            case "add": {
                if(args.length < 2) {
                    Catgirl.sendChatMessage("Usage: .friends add <username>");
                    return;
                } 

                String name = args[1]; 
                FriendManager.add(name);
                Catgirl.sendChatMessage(name+" is now your friend!");
                break;
            } 

            case "remove": {
                if(args.length < 2) {
                    Catgirl.sendChatMessage("Usage: .friends remove <username>");
                    return;
                } 

                String name = args[1]; 
                FriendManager.remove(name);
                Catgirl.sendChatMessage(name+" was removed from friends.");
                break;                
            } 

            case "list": {
                if(FriendManager.getFriends().isEmpty()) {
                    Catgirl.sendChatMessage("You have no friends");
                    return;
                }

                Catgirl.sendChatMessage("Friends: ");
                for(String friend : FriendManager.getFriends()) {
                    Catgirl.sendChatMessage(" - " + friend);
                }
                break;
            }  

            case "clear": {
                FriendManager.clear();
                Catgirl.sendChatMessage("Cleared all friends");
                break;
            } 

            default: {
                Catgirl.sendChatMessage("Usage: .friends add <username>");
                break;
            }
        }
    }
}
