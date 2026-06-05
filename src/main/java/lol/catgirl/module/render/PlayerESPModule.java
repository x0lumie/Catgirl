package lol.catgirl.module.render;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.manager.FriendManager;
import lol.catgirl.module.ModuleCategory;
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
        for (LivingEntity entity : TargetsModule.getTargetList()) {

            Color espColor;

            if (entity instanceof Player player && FriendManager.isFriend(player)) {
                espColor = new Color(0, 255, 0, alpha.getValue().intValue());
            } else {
                espColor = new Color(255, 0, 0, alpha.getValue().intValue());
            }

            RenderUtils.renderBoxC(entity, event, event.partialTicks, espColor);
        }
    }
}
