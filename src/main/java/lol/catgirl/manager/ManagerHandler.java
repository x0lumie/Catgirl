package lol.catgirl.manager;

import lol.catgirl.Catgirl;
import lombok.Getter;

public final class ManagerHandler {

    public static final TargetManager targetManager = new TargetManager();
    public static final CommandManager commandManager = new CommandManager();

    public static void init() {
        Catgirl.LOGGER.info("Initializing Managers...");
    }
}
