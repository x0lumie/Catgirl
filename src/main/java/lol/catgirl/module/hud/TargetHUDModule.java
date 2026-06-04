package lol.catgirl.module.hud;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.TargetsModule;
import lol.catgirl.setting.impl.EnumProperty;
import lol.catgirl.utils.client.ColorUtil;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

public final class TargetHUDModule extends Module {
    public static final TargetHUDModule INSTANCE = new TargetHUDModule();

    public enum Mode {
        Exhibition,
        Astolfo,
        Novoline
    }

    private float x = 10f, y = 50f;

    private double novolineAnimatedWidth;
    private double novolineAnimatedHealth;
    private float animatedHealth;

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Exhibition);

    public TargetHUDModule() {
        super("TargetHUD", "Shows a hud with target data on it.", ModuleCategory.Hud);
        addSettings(mode);
    }

    private float dragX, dragY;
    private boolean dragging;

    public void dragging() {
        if (mc.screen instanceof ChatScreen) {

            double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().
                    getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().
                    getGuiScaledHeight() / (double) mc.getWindow().getHeight();

            boolean isMouseDown = GLFW.glfwGetMouseButton(mc.getWindow().handle(),
                    org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) ==
                    org.lwjgl.glfw.GLFW.GLFW_PRESS;

            if (isMouseDown) {
                if (!dragging) {
                    float hudWidth = 155f;
                    float hudHeight = 60f;

                    if (mouseX >= x && mouseX <= x + hudWidth && mouseY >= y && mouseY <= y + hudHeight) {
                        dragging = true;
                        dragX = (float) (mouseX - x);
                        dragY = (float) (mouseY - y);
                    }
                } else {
                    x = (float) (mouseX - dragX);
                    y = (float) (mouseY - dragY);
                }
            } else {
                dragging = false;
            }
        } else {
            dragging = false;
        }
    }

    private LivingEntity target;

    @EventHook
    public void onRender(Render2DEvent event) {
        if(mc.player == null) return;

        dragging();

        if (mc.screen instanceof ChatScreen) {
            target = mc.player;
        } else {
            target = TargetsModule.getTarget();
        }

        if (!(target instanceof Player)) {
            return;
        }

        if (target == null) return;

        switch (mode.getValue()) {
            case Exhibition -> {
                drawExhibition(event);
            }

            case Astolfo -> {
                drawAstolfo(event);
            } 
            
            case Novoline -> {
                drawNovoline(event);
            }
        }
    }

    private void drawNovoline(Render2DEvent event) {

        if (target == null) {
            novolineAnimatedWidth = 0;
            return;
        }

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

        Color themeColor = ColorUtil.getClientTheme();

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
    private void drawAstolfo(Render2DEvent event) {
        if (target == null) {
            return;
        }

        GuiGraphics graphics = event.context;

        float width = 155f;
        float height = 60f;

        float healthPercent = Mth.clamp(
                target.getHealth() / target.getMaxHealth(),
                0.0F, 1.0F
        );

        float targetHealthWidth = 120.0F * healthPercent;

        animatedHealth += (targetHealthWidth - animatedHealth) * 0.1F;

        Color theme = ColorUtil.getClientTheme();

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
    private void drawExhibition(Render2DEvent event) {
        if (target == null) {
            return;
        }

        GuiGraphics graphics = event.context;

        String name = target.getScoreboardName();

        double fontWidth = DrawUtil.getStringWidth(
                name,
                9f,
                ResourceManager.FontResources.regular
        );

        double boxWidth = 40 + fontWidth;

        double dynamicBarMax = Math.min(fontWidth, 60.0);
        dynamicBarMax = Math.ceil(dynamicBarMax / 10.0) * 10.0;

        if (dynamicBarMax < 60.0) {
            dynamicBarMax = 60.0;
        }

        float width = (float) Math.max(boxWidth, 120f);
        float height = 40f;

        DrawUtil.begin();

        DrawUtil.roundedRect(
                x - 2.5f, y - 2.5f, x + width + 2.5f,
                y + height + 2.5f,
                0f,
                new Color(10, 10, 10, 255)
        );

        DrawUtil.roundedRect(
                x - 2.0f, y - 2.0f, x + width + 2.0f, y + height + 2.0f,
                0f,
                new Color(60, 60, 60, 255)
        );

        DrawUtil.roundedRect(
                x - 1.5f, y - 1.5f, x + width + 1.5f,
                y + height + 1.5f,
                0f,
                new Color(40, 40, 40, 255)
        );

        DrawUtil.roundedRect(
                x - 0.5f, y - 0.5f, x + width + 0.5f,
                y + height + 0.5f, 0f,
                new Color(60, 60, 60, 255)
        );

        DrawUtil.roundedRect(
                x, y, x + width,
                y + height, 0f,
                new Color(22, 22, 22, 255)
        );

        DrawUtil.roundedRect(
                x + 2f, y + 2f, x + 38f, y + 38f, 0f,
                new Color(10, 10, 10, 255)
        );

        DrawUtil.roundedRect(
                x + 2.5f,
                y + 2.5f, x + 37.5f, y + 37.5f, 0f,
                new Color(48, 48, 48, 255)
        );

        DrawUtil.roundedRect(
                x + 3f, y + 3f, x + 37f, y + 37f,
                0f,
                new Color(17, 17, 17, 255)
        );

        DrawUtil.drawString(
                name, x + 39f, y + 9f, 8f, Color.WHITE,
                ResourceManager.FontResources.regular
        );

        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float absorption = target.getAbsorptionAmount();

        float progress = health / (maxHealth + absorption);
        float realHealthProgress = health / maxHealth;

        Color healthColor = getHealthColor(realHealthProgress);

        float barX = x + 39f;
        float barY = y + 12f;
        float barW = (float) dynamicBarMax;
        float barH = 3f;

        DrawUtil.roundedRect(
                barX - 0.5f, barY - 0.5f,
                barX + barW + 0.5f,
                barY + barH + 0.5f,
                0f, Color.BLACK
        );

        DrawUtil.roundedRect(
                barX, barY, barX + barW,
                barY + barH,
                0f,
                new Color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 35)
        );

        float activeHealthWidth = (float) (barW * progress);

        DrawUtil.roundedRect(
                barX, barY, barX + activeHealthWidth,
                barY + barH, 0f, healthColor
        );

        if (absorption > 0) {
            float absorptionWidth = (float) (barW * (absorption / (maxHealth + absorption)));

            DrawUtil.roundedRect(
                    barX + activeHealthWidth, barY,
                    barX + activeHealthWidth + absorptionWidth,
                    barY + barH,
                    0f,
                    new Color(255, 170, 0, 128)
            );
        }

        for (int i = 1; i < 10; i++) {
            float segmentX = barX + (barW / 10f) * i;

            DrawUtil.roundedRect(
                    segmentX, barY - 0.5f,
                    segmentX + 0.5f,
                    barY + barH + 0.5f, 0f,
                    Color.BLACK
            );
        }

        String subText = "HP: " + (int) health +
                " | Dist: " + (int) mc.player.distanceTo(target);

        DrawUtil.drawString(
                subText,
                x + 39f,
                y + 22f,
                6f,
                new Color(220, 220, 220, 255),
                ResourceManager.FontResources.regular
        );

        DrawUtil.end();

        float largestDimension = Math.max(
                target.getBbHeight(),
                target.getBbWidth()
        );

        int scaleFactor = (int) (
                16f / Math.max(largestDimension / 1.8F, 1.0F)
        );

        int x1 = (int) x + 3;
        int y1 = (int) y + 3;
        int x2 = (int) x + 37;
        int y2 = (int) y + 37;

        double mouseX = mc.mouseHandler.xpos()
                * (double) mc.getWindow().getGuiScaledWidth()
                / (double) mc.getWindow().getWidth();

        double mouseY = mc.mouseHandler.ypos()
                * (double) mc.getWindow().getGuiScaledHeight()
                / (double) mc.getWindow().getHeight();

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics, x1, y1, x2, y2, scaleFactor,
                0.0625F, (float) mouseX, (float) mouseY, target
        );

        if (target instanceof Player playerTarget) {

            java.util.List<ItemStack> itemsToRender = new ArrayList<>();

            for (int slotId = 39; slotId >= 36; --slotId) {
                ItemStack armorItem = playerTarget.getInventory().getItem(slotId);
                if (!armorItem.isEmpty()) {
                    itemsToRender.add(armorItem);
                }
            }

            ItemStack mainHand = playerTarget.getMainHandItem();
            if (!mainHand.isEmpty()) {
                itemsToRender.add(mainHand);
            }

            int itemSpacingX = 20;
            int itemY = (int) (y + 23);

            var enchantmentRegistry = mc.level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT);

            for (ItemStack item : itemsToRender) {
                itemSpacingX += 16;

                graphics.renderItem(
                        item,
                        (int) x + itemSpacingX,
                        itemY
                );

                graphics.renderItemDecorations(
                        mc.font,
                        item,
                        (int) x + itemSpacingX,
                        itemY
                );

                int enchantTextY = itemY;

                ItemEnchantments enchantments = item.getOrDefault(
                        DataComponents.ENCHANTMENTS,
                        ItemEnchantments.EMPTY
                );

                if (item.has(DataComponents.WEAPON)) {
                    int sharp = enchantments.getLevel(
                            enchantmentRegistry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS)
                    );

                    int fire = enchantments.getLevel(
                            enchantmentRegistry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT)
                    );

                    int unb = enchantments.getLevel(
                            enchantmentRegistry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING)
                    );

                    if (sharp > 0) {
                        drawEnchantText(graphics, "S" + sharp, x + itemSpacingX, enchantTextY);
                        enchantTextY += 5;
                    }

                    if (fire > 0) {
                        drawEnchantText(graphics, "F" + fire, x + itemSpacingX, enchantTextY);
                        enchantTextY += 5;
                    }

                    if (unb > 0) {
                        drawEnchantText(graphics, "U" + unb, x + itemSpacingX, enchantTextY);
                    }

                } else if (item.has(DataComponents.EQUIPPABLE)) {
                    int prot = enchantments.getLevel(
                            enchantmentRegistry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION)
                    );

                    int unb = enchantments.getLevel(
                            enchantmentRegistry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING)
                    );

                    if (prot > 0) {
                        drawEnchantText(graphics, "P" + prot, x + itemSpacingX, enchantTextY);
                        enchantTextY += 5;
                    }

                    if (unb > 0) {
                        drawEnchantText(graphics, "U" + unb, x + itemSpacingX, enchantTextY);
                    }
                }
            }
        }
    }

    private void drawEnchantText(GuiGraphics graphics, String text, float x, float y) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(0.5f, 0.5f);
        graphics.drawString(mc.font, text, (int) (x * 2), (int) (y * 2), Color.white.getRGB(), true);
        graphics.pose().popMatrix();
    }

    private Color getHealthColor(float progress) {
        if (progress > 0.5f) {
            return interpolateColor(Color.YELLOW, Color.GREEN, (progress - 0.5f) * 2f);
        } else {
            return interpolateColor(Color.RED, Color.YELLOW, progress * 2f);
        }
    }

    private Color interpolateColor(Color color1, Color color2, float fraction) {
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
