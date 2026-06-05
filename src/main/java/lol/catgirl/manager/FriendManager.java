package lol.catgirl.manager;

import lol.catgirl.module.client.FriendsModule;
import lol.catgirl.utils.IMinecraft;
import net.minecraft.world.entity.player.Player;

import java.io.*;
import java.util.*;

@SuppressWarnings("all")
public class FriendManager implements IMinecraft {
    private static final Set<String> friendsList = new HashSet<>();
    private static final File friendsFile = new File(mc.gameDirectory +
            "/Catgirl", "/Friends.txt");

    public static void add(String name) {
        if(name != null && !name.isEmpty()) {
            String lower = name.toLowerCase();
            friendsList.add(lower);
            saveFriends();
        }
    }

    public static void remove(String name) {
        if(name != null && !name.isEmpty()) {
            String lower = name.toLowerCase();
            friendsList.remove(lower);
            saveFriends();
        }
    }

    public static boolean isFriend(String name) {
        if(!FriendsModule.INSTANCE.isEnabled()) return false;

        return name != null && friendsList.contains(name.toLowerCase());
    }

    public static boolean isFriend(Player player) {
        if(!FriendsModule.INSTANCE.isEnabled()) return false;

        return player != null && isFriend(player.getGameProfile().name());
    }

    public static Set<String> getFriends() {
        return friendsList;
    }

    public static void clear() {
        friendsList.clear();
        saveFriends();
    }

    public static void saveFriends() {
        try {
            if(!friendsFile.getParentFile().exists()) {
                friendsFile.getParentFile().mkdirs();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(friendsFile))) {
                for (String friend : friendsList) {
                    writer.write(friend);
                    writer.newLine();
                }
            }
        } catch(IOException e) {

        }
    }

    public static void loadFriends() {
        friendsList.clear();
        if(friendsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(friendsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if(!line.trim().isEmpty()) {
                        friendsList.add(line.trim().toLowerCase());
                    }
                }
            } catch(IOException err) {
            }
        }
    }

    public static void initialize() {
        loadFriends();
    }
}