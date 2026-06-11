package lol.catgirl.module.hud.targethud.impl;

import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.module.hud.TargetHUDModule;
import lol.catgirl.module.hud.targethud.TargetHUDMode;
import lol.catgirl.utils.client.ColorUtils;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.awt.*;

@AllArgsConstructor
public class NovolineTargetHUDMode implements TargetHUDMode {
    public final TargetHUDModule module;

    private double novolineAnimatedWidth;
    private double novolineAnimatedHealth;

    public NovolineTargetHUDMode(TargetHUDModule module) {
        this.module = module;
    }

    @Override
    public void onRender(Render2DEvent event, LivingEntity target) {

        if (target == null) {
            novolineAnimatedWidth = 0;
            return;
        }

        var x = module.x;
        var y = module.y;

        GuiGraphics graphics = event.context;

        String name = target.getScoreboardName();
        float nameWidth = mc.font.width(name);

        double baseWidth = 74;
        double targetWidth = baseWidth + nameWidth;
        int height = 42;

        novolineAnimatedWidth = lerp(novolineAnimatedWidth, targetWidth, 0.1);
        float renderWidth = (float) novolineAnimatedWidth;

        graphics.fill(
                (int) x,
                (int) y,
                (int) (x + renderWidth),
                (int) (y + height),
                new Color(40, 40, 40, 255).getRGB()
        );

        if (target instanceof Player player) {
            renderHead(graphics, player, x + 1, y + 1, 40);
        }

        graphics.drawString(
                mc.font, name, (int) x + 44, (int) y + 10,
                -1, true
        );

        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();

        double healthPercentage = Math.max(0.0, Math.min(1.0, health / maxHealth));
        double barWidth = 26 + nameWidth;

        novolineAnimatedHealth = lerp(novolineAnimatedHealth, healthPercentage, 0.1);

        graphics.fill(
                (int) x + 44,
                (int) y + 22,
                (int) (x + 44 + barWidth),
                (int) (y + 33),
                new Color(21, 21, 21, 150).getRGB()
        );

        Color themeColor = ColorUtils.getClientTheme();

        graphics.fill(
                (int) x + 44,
                (int) y + 22,
                (int) (x + 44 + (barWidth * novolineAnimatedHealth)),
                (int) y + 33,
                themeColor.darker().getRGB()
        );

        graphics.fill(
                (int) x + 44,
                (int) y + 22,
                (int) (x + 44 + (barWidth * healthPercentage)),
                (int) y + 33,
                themeColor.getRGB()
        );

        String healthText = String.format("%.1f%%", healthPercentage * 100);
        float textWidth = mc.font.width(healthText);

        graphics.drawString(
                mc.font,
                healthText,
                (int) (x + 44 + (barWidth / 2) - (textWidth / 2)),
                (int) y + 24,
                -1,
                true
        );
    }

    public Color interpolateColor(Color color1, Color color2, float fraction) {
        fraction = Math.max(0f, Math.min(1f, fraction));
        int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * fraction);
        int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * fraction);
        int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * fraction);
        return new Color(r, g, b);
    }

    private void renderHead(GuiGraphics graphics, Player target, float x, float y, float size) {
        int renderSize = (int) size;

        if (target instanceof AbstractClientPlayer player) {
            Identifier skin = player.getSkin().body().texturePath();

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, skin,
                    (int) x, (int) y, 8, 8,
                    renderSize, renderSize,
                    8, 8,
                    64, 64
            );
        }
    }

    private double lerp(double start, double end, double speed) {
        return start + (end - start) * speed;
    }

}
