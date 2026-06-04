package lol.catgirl;

import lol.catgirl.event.EventBus;
import lol.catgirl.event.Handler;
import lol.catgirl.manager.ManagerHandler;
import lol.catgirl.manager.ModuleManager;
import net.fabricmc.api.ModInitializer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static lol.catgirl.utils.IMinecraft.mc;

public class Catgirl implements ModInitializer {
	public static final String MOD_ID = "catgirl";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String NAME = "Catgirl";
	public static final String VERSION = "1.0";
	public static final String windowTitle = NAME + " v" + VERSION;

	public static Catgirl INSTANCE;
	public EventBus eventBus;
	public Handler theHandler;

	@Override
	public void onInitialize() {
		INSTANCE = this;
		eventBus = new EventBus();
		theHandler = new Handler();

		Handler.initialize();
		ModuleManager.getInstance().init();
		ManagerHandler.init();

		eventBus.subscribe(ManagerHandler.commandManager);
		eventBus.subscribe(theHandler);
	}

	public static void sendChatMessage(String message) {
		if (mc.player == null) return;

		Component chat = Component.literal("[")
				.withStyle(ChatFormatting.LIGHT_PURPLE)
				.append(Component.literal("catgirl").withStyle(ChatFormatting.DARK_PURPLE))
				.append(Component.literal("] ").withStyle(ChatFormatting.LIGHT_PURPLE))
				.append(Component.literal(message).withStyle(ChatFormatting.WHITE));

		mc.player.displayClientMessage(chat, false);
	}
}