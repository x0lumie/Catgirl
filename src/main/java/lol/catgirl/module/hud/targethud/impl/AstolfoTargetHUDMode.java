package lol.catgirl.module.hud.targethud.impl;

import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.module.hud.TargetHUDModule;
import lol.catgirl.module.hud.targethud.TargetHUDMode;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.awt.*;

@AllArgsConstructor
public class AstolfoTargetHUDMode implements TargetHUDMode {
    public final TargetHUDModule module;

    private float animatedHealth;

    public AstolfoTargetHUDMode(TargetHUDModule module) {
        this.module = module;
    }

    @Override
    public void onRender(Render2DEvent event, LivingEntity target) {
        if (target == null) {
            return;
        }

        GuiGraphics graphics = event.context;


        float x = module.x;
        float y = module.y;

        float width = 155f;
        float height = 60f;

        float healthPercent = Mth.clamp(
                target.getHealth() / target.getMaxHealth(),
                0.0F, 1.0F
        );

        float targetHealthWidth = 120.0F * healthPercent;

        animatedHealth += (targetHealthWidth - animatedHealth) * 0.1F;

        Color theme = ColorUtils.getClientTheme();

        DrawUtil.begin();
        DrawUtil.roundedRect(
                x, y, x + width,
                y + height, 0f,
                new Color(5, 5, 5, 150)
        );
        DrawUtil.end();

        String name = target.getName().getString();

        graphics.drawString(
                mc.font, name, (int) (x + 31),
                (int) (y + 7),
                -1, true
        );

        String healthText = String.format("%.1f ❤", target.getHealth() / 2.0F);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(2.2F, 2.2F);

        graphics.drawString(
                mc.font, healthText, 15, 9,
                theme.getRGB(), true
        );
        graphics.pose().popMatrix();

        if (!target.getOffhandItem().isEmpty()) {
            graphics.renderItem(
                    target.getItemInHand(InteractionHand.OFF_HAND),
                    (int) x + 137, (int) y + 2
            );

            graphics.renderItemDecorations(
                    mc.font,
                    target.getItemInHand(InteractionHand.OFF_HAND),
                    (int) x + 137,
                    (int) y + 2
            );
        }

        if (target instanceof Player player) {
            float characterSize = 26.5F;

            double mouseX = mc.mouseHandler.xpos() * (double)
                    mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double mouseY = mc.mouseHandler.ypos() * (double)
                    mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    (int) x + 2, (int) y + 2,
                    (int) x + 32, (int) y + 56,
                    (int) characterSize,
                    0.0625F,
                    (float) mouseX, (float) mouseY,
                    player
            );
        }

        DrawUtil.begin();
        float healthBarPosY = y + 47.0F;
        float barHeight = 8F;

        DrawUtil.roundedRect(
                x + 30, healthBarPosY,
                x + 150, healthBarPosY + barHeight,
                0f, theme.darker().darker().darker()
        );

        DrawUtil.roundedRect(
                x + 30, healthBarPosY,
                x + 30 + animatedHealth, healthBarPosY + barHeight,
                0f, theme
        );
        DrawUtil.end();
    }
}
