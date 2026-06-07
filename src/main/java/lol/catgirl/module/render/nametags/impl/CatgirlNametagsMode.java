package lol.catgirl.module.render.nametags.impl;

import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.module.client.AntiBotModule;
import lol.catgirl.module.render.nametags.NametagsMode;
import lol.catgirl.ui.click.imgui.ImGuiImpl;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.render.RenderUtils;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.*;

public final class CatgirlNametagsMode implements NametagsMode {
    @Override
    public void onRenderTick(RenderTickEvent event) {
        if (mc.level == null || mc.player == null) return;

        var currentFont = ResourceManager.getSelectedFont();

        for (Entity entity : mc.level.entitiesForRendering()) {

            if (!(entity instanceof Player en)
                    || en == mc.player
                    || !en.isAlive()
                    || AntiBotModule.INSTANCE.isBot(en)
                    || !RenderUtils.isEntityInView(en)) {
                continue;
            }

            Vec3 pos = en.getPosition(event.partialTicks);

            Vec3 worldPos = new Vec3(
                    pos.x,
                    pos.y + en.getBbHeight() + 0.4,
                    pos.z
            );

            Vec3 screenPos = RenderUtils.worldToScreen(worldPos);
            if (screenPos == null) continue;

            float health = en.getHealth();
            String healthStr = String.format("%.1f", health);
            String name = en.getName().getString() + " " + healthStr + "HP";

            float fontSize = 9f;

            double textWidth = DrawUtil.getStringWidth(name, fontSize, currentFont);
            double textHeight = DrawUtil.getFontHeight(fontSize, currentFont);

            float x = (float) screenPos.x;
            float y = (float) screenPos.y;

            float paddingX = 4f;
            float paddingY = 3f;

            float boxX1 = x - ((float) textWidth / 2f) - paddingX;
            float boxY1 = y - ((float) textHeight / 2f) - paddingY;
            float boxX2 = x + ((float) textWidth / 2f) + paddingX;
            float boxY2 = y + ((float) textHeight / 2f) + paddingY;

            DrawUtil.begin();

            Color shadowColor = new Color(0, 0, 0, 100);
            DrawUtil.roundedRect(boxX1 + 1f, boxY1 + 1f,
                    boxX2 + 1f, boxY2 + 1f, 3.5f, shadowColor);

            Color mainColor = new Color(10, 10, 10, 230);
            DrawUtil.roundedRect(boxX1, boxY1, boxX2, boxY2, 3.5f, mainColor);

            float healthRatio = Math.max(0f, Math.min(1f, health / en.getMaxHealth()));
//            Color healthColor = Color.getHSBColor(healthRatio * 0.33f, 0.85f, 0.9f);

//            DrawUtil.roundedRect(boxX1 + 2f, boxY2 - 1.5f, boxX1 + 2f + ((boxX2 - boxX1 - 4f) * healthRatio), boxY2 - 0.5f, 0.5f, healthColor);

            float drawTextX = x - ((float) textWidth / 2f);
            float drawTextY = y + ((float) textHeight / 2f) - 0f;

            DrawUtil.drawString(name, drawTextX, drawTextY, fontSize, ColorUtils.getClientTheme(), currentFont);

            DrawUtil.end();
        }
    }
}