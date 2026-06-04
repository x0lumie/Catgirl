package lol.catgirl;

import lol.catgirl.event.EventBus;
import lol.catgirl.event.Handler;
import lol.catgirl.manager.ModuleManager;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Catgirl implements ModInitializer {
	public static final String MOD_ID = "catgirl";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String NAME = "Catgirl";
	public static final String VERSION = "1.0";
	public static final String windowTitle = NAME + " v" + VERSION;

	public static Catgirl INSTANCE;
	public final EventBus eventBus = new EventBus();

	@Override
	public void onInitialize() {
		INSTANCE = this;
		Handler.initialize();
		ModuleManager.getInstance().init();

	}
}