package lol.catgirl.module.client;

import lol.catgirl.manager.FriendManager;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class FriendsModule extends Module {
    public static final FriendsModule INSTANCE = new FriendsModule();

    public FriendsModule() {
        super("Friends", "If to ignore friends or not.", ModuleCategory.Client);
    }

    // all this does is return on isFriend
}
