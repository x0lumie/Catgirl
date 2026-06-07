package lol.catgirl.module.render;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.manager.FriendManager;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.AntiBotModule;
import lol.catgirl.module.client.TargetsModule;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.module.Module;
import lol.catgirl.utils.render.RenderUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.awt.*;

public final class PlayerESPModule extends Module {
    public static final PlayerESPModule INSTANCE = new PlayerESPModule();

    public final SliderProperty alpha = new SliderProperty("Alpha", 255, 0, 255, 1);

    public PlayerESPModule() {
        super("PlayerESP",
                "Shows a ESP box around players, or friends.",
                ModuleCategory.Render
        );
        addSettings(alpha);
    }

    @EventHook
    public void onRenderWorld(Render3DEvent event) {
        Color espColor;

        var antibot = AntiBotModule.INSTANCE;

        for (Player actor : mc.level.players()) {
            if(antibot.isEnabled() && antibot.isBot(actor)) {
                continue;
            }

            if(actor == mc.player) continue;

            if (FriendManager.isFriend(actor)) {
                espColor = new Color(0, 255, 0, alpha.getValue().intValue());
            } else {
                espColor = new Color(255, 0, 0, alpha.getValue().intValue());
            }

            if(espColor != null) {
                RenderUtils.renderBoxC(actor, event, event.partialTicks, espColor);
            }
        }
    }
}
