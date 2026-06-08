package lol.catgirl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lol.catgirl.event.EventBus;
import lol.catgirl.event.Handler;
import lol.catgirl.file.impl.ModulesFile;
import lol.catgirl.manager.*;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
	public CommandManager commandManager;
	public SoundManager soundManager;
	private IssueManager issueManager;

	@Getter
	private final Gson GSON = new GsonBuilder().create();

	public static final Identifier PANORAMA_SKYBOX = Identifier.fromNamespaceAndPath("catgirl", "images/funny/gamer.png");

	@Override
	public void onInitialize() {
		INSTANCE = this;
		eventBus = new EventBus();
		theHandler = new Handler();
		commandManager = new CommandManager();
		issueManager = new IssueManager();
		soundManager = new SoundManager();

		Handler.initialize();
		ModuleManager.getInstance().init();
		FriendManager.initialize();

		soundManager.init();

		eventBus.subscribe(commandManager);
		eventBus.subscribe(issueManager);
		eventBus.subscribe(theHandler);

		if (doesFileExist("default")) {
			new ModulesFile("default").loadFromFile();
		}
	}

	private boolean doesFileExist(String name) {
		File dir = ModulesFile.BASE_DIRECTORY.resolve("configs/").toFile();
		File file = new File(dir, name + ".json");
		return file.exists();
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