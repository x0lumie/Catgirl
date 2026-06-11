package lol.catgirl.module.hud.targethud.impl;

import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.module.hud.TargetHUDModule;
import lol.catgirl.module.hud.targethud.TargetHUDMode;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.awt.*;
import java.util.ArrayList;

@AllArgsConstructor
public class ExhibitionTargetHUDMode implements TargetHUDMode {
    public final TargetHUDModule module;

    @Override
    public void onRender(Render2DEvent event, LivingEntity target) {
        if (target == null) {
            return;
        }

        float x = module.x;
        float y = module.y;

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
            return module.interpolateColor(Color.YELLOW, Color.GREEN, (progress - 0.5f) * 2f);
        } else {
            return module.interpolateColor(Color.RED, Color.YELLOW, progress * 2f);
        }
    }
}
