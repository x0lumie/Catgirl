package lol.catgirl.ui.click.menu;

import lol.catgirl.Catgirl;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.Module;
import lol.catgirl.module.hud.WatermarkModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.Animation;
import lol.catgirl.utils.client.Easing;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import lol.catgirl.utils.client.ColorUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MenuClickGui extends Screen {

    private MenuClickGuiCategory currentCategory = MenuClickGuiCategory.values()[0];

    public float frameW = 356f;
    public float frameH = 306f;
    private float posX = -1f;
    private float posY = -1f;

    private float targetX = -1f;
    private float targetY = -1f;
    private float targetW = 360f;
    private float targetH = 230f;

    private boolean isDragging = false;
    private boolean isResizing = false;
    private float dragStartX, dragStartY;
    private float initialWidth, initialHeight;

    private float scrollOffset = 0f;
    private float targetScrollOffset = 0f;

    private SliderProperty activeSlider = null;
    private float activeSliderX1 = 0f;
    private float activeSliderW = 0f;

    private final Map<MenuClickGuiCategory, Animation> categoryAnimations = new HashMap<>();

    public MenuClickGui() {
        super(Component.empty());
        for (MenuClickGuiCategory category : MenuClickGuiCategory.values()) {
            categoryAnimations.put(category, new Animation(Easing.DECELERATE, 200L));
        }
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float ticks) {
        if (posX == -1f || posY == -1f) {
            targetX = (this.width / 2f) - (frameW / 2f);
            targetY = (this.height / 2f) - (frameH / 2f);
            posX = targetX;
            posY = targetY;
            targetW = frameW;
            targetH = frameH;
        }

        if (isDragging) {
            targetX = mouseX - dragStartX;
            targetY = mouseY - dragStartY;
        } else if (isResizing) {
            targetW = Math.max(240f, initialWidth + (mouseX - dragStartX));
            targetH = Math.max(160f, initialHeight + (mouseY - dragStartY));
        }

        if (activeSlider != null) {
            float pct = (mouseX - activeSliderX1) / activeSliderW;
            pct = Math.max(0f, Math.min(1f, pct));
            float rawValue = activeSlider.getMin() + pct * (activeSlider.getMax() - activeSlider.getMin());
            activeSlider.setValue(rawValue);
        }

        float deltaFactor = 0.22f;
        posX += (targetX - posX) * deltaFactor;
        posY += (targetY - posY) * deltaFactor;
        frameW += (targetW - frameW) * deltaFactor;
        frameH += (targetH - frameH) * deltaFactor;

        float left = posX;
        float top = posY;
        float right = left + frameW;
        float bottom = top + frameH;

        float sidebarW = 110f;
        float radius = 12f;

        Color accentPink = WatermarkModule.PINK;
        Color thinBorderColor = new Color(accentPink.getRed(), accentPink.getGreen(), accentPink.getBlue(), 65);

        Color sidebarColor = new Color(17, 16, 21, 255);
        Color contentBgColor = new Color(24, 23, 30, 255);

        DrawUtil.begin();

        DrawUtil.roundedRect(left - 1f, top - 1f, right + 1f, bottom + 1f, radius + 0.5f, thinBorderColor);
        DrawUtil.roundedRect(left, top, right, bottom, radius, contentBgColor);
        DrawUtil.roundedRectVarying(
                left, top, left + sidebarW, bottom,
                radius, 0f, 0f, radius,
                sidebarColor
        );

        DrawUtil.roundedRect(left + sidebarW, top, left + sidebarW + 0.5f, bottom, 0f, new Color(32, 30, 40, 100));

        String titleText = Catgirl.NAME;
        float titleFontSize = 16f;

        float baseLeftPadding = left + 4f + 8f;
        float titleX = baseLeftPadding;
        float titleY = top + 26f;

        DrawUtil.drawString(
                titleText, titleX, titleY,
                titleFontSize,
                Color.WHITE,
                ResourceManager.FontResources.productSansBold
        );

        String versionText = Catgirl.VERSION;
        float versionFontSize = 7.5f;
        float titleWidth = (float) DrawUtil.getStringWidth(titleText, titleFontSize, ResourceManager.FontResources.productSansBold);

        float versionX = titleX + titleWidth + 4f;
        float versionY = titleY - 5f;

        DrawUtil.drawString(
                versionText, versionX, versionY,
                versionFontSize,
                new Color(130, 130, 135, 255),
                ResourceManager.FontResources.productSansBold
        );

        float startCategoryY = titleY + 14f;
        float categoryHeight = 20f;
        float categorySpacing = 0f;
        float categoryFontSize = 8f;

        MenuClickGuiCategory[] categories = MenuClickGuiCategory.values();
        for (int i = 0; i < categories.length; i++) {
            MenuClickGuiCategory category = categories[i];
            String name = category.name();

            float catX1 = left + 4f;
            float catY1 = startCategoryY + (i * (categoryHeight + categorySpacing));
            float catX2 = left + sidebarW - 8f;
            float catY2 = catY1 + categoryHeight;

            boolean isSelected = category == currentCategory;
            boolean isHovered = mouseX >= catX1 && mouseX <= catX2 && mouseY >= catY1 && mouseY <= catY2;

            Animation anim = categoryAnimations.get(category);
            anim.run(isSelected ? 1f : (isHovered ? 0.5f : 0f));
            float animProgress = anim.getValue();

            float startX = catX1 + 8f;
            float iconFontSize = categoryFontSize + 2f;
            float iconY = catY1 + (categoryHeight / 2f) + (iconFontSize / 2f) - 1f;
            String iconText = "";

            switch (name) {
                case "Combat" -> iconText = "a";
                case "Movement" -> iconText = "b";
                case "Player" -> iconText = "c";
                case "Visual", "Render" -> iconText = "g";
                case "Client" -> iconText = "e";
                case "Ghost" -> iconText = "f";
                case "Hud" -> iconText = "g";
            }


            float iconToTextPadding = 6f;

            if (animProgress > 0.01f) {
                int alphaVal = (int) (35f * animProgress);
                Color pillColor = new Color(
                        accentPink.getRed(),
                        accentPink.getGreen(),
                        accentPink.getBlue(),
                        alphaVal
                );

                float iconWidth = ("Search".equals(name)
                        ? (float) DrawUtil.getStringWidth("c", iconFontSize, ResourceManager.FontResources.moreIconsFont)
                        : (float) DrawUtil.getStringWidth(iconText, iconFontSize, ResourceManager.FontResources.icons)
                );

                float textWidth = (float) DrawUtil.getStringWidth(
                        name, categoryFontSize,
                        ResourceManager.FontResources.productSansBold
                );
                float pillPaddingX = 2f;
                float pillPaddingY = 2f;

                float pillX1 = startX - pillPaddingX;
                float pillX2 = startX + iconWidth + iconToTextPadding + textWidth + pillPaddingX;

                DrawUtil.roundedRect(
                        pillX1,
                        catY1 + pillPaddingY,
                        pillX2,
                        catY2 - pillPaddingY,
                        5f,
                        pillColor
                );
            }

            Color unselectedColor = isHovered ? new Color(200, 200, 200, 255) : new Color(130, 130, 135, 255);
            Color textColor = DrawUtil.interpolate(unselectedColor, Color.WHITE, isSelected ? animProgress : animProgress * 2f);

            if ("Search".equals(name)) {
                DrawUtil.drawString("c", startX, iconY, iconFontSize, textColor, ResourceManager.FontResources.moreIconsFont);
            } else {
                DrawUtil.drawString(iconText, startX, iconY, iconFontSize, textColor, ResourceManager.FontResources.icons);
            }

            float iconWidth = ("Search".equals(name)
                    ? (float) DrawUtil.getStringWidth("c", iconFontSize, ResourceManager.FontResources.moreIconsFont)
                    : (float) DrawUtil.getStringWidth(iconText, iconFontSize, ResourceManager.FontResources.icons)
            );

            float textX = startX + iconWidth + iconToTextPadding;
            float textY = catY1 + (categoryHeight / 2f) + (categoryFontSize / 2f) - 1f;

            DrawUtil.drawString(name, textX, textY, categoryFontSize, textColor, ResourceManager.FontResources.productSansBold);

            if (isSelected) {
                float startModuleY = top + 20f;
                float moduleHeight = 28f;
                float moduleSpacing = 4f;

                float nameFontSize = 8.5f;
                float descFontSize = 6.5f;

                float scissorX = left + sidebarW + 5f;
                float scissorY = top + 5f;
                float scissorW = frameW - sidebarW - 10f;
                float scissorH = frameH - 10f;

                DrawUtil.scissor(scissorX, scissorY, scissorW, scissorH);

                var modules = ModuleManager.getInstance().getModulesByCategory(category.name());

                float totalContentHeight = 0f;
                for (Module module : modules) {
                    totalContentHeight += moduleHeight + moduleSpacing;
                    if (module.isExpanded()) {
                        for (var setting : module.getProperties()) {
                            if (setting.isHidden()) continue;

                            totalContentHeight += ((setting instanceof SliderProperty)
                                    ? 24f : 14f) + 4f;
                        }
                        totalContentHeight += 2f;
                    }
                }

                float maxScroll = Math.max(0f, totalContentHeight - (frameH - 30f));
                if (targetScrollOffset > maxScroll) targetScrollOffset = maxScroll;
                if (targetScrollOffset < 0f) targetScrollOffset = 0f;

                scrollOffset += (targetScrollOffset - scrollOffset) * 0.16f;

                float currentY = startModuleY - scrollOffset;

                for (Module module : modules) {
                    float modX1 = left + sidebarW + 15f;
                    float modY1 = currentY;
                    float modX2 = right - 15f;

                    float dynamicCardHeight = moduleHeight;
                    if (module.isExpanded()) {
                        for (var setting : module.getProperties()) {
                            if (setting.isHidden()) continue;

                            dynamicCardHeight += ((setting instanceof SliderProperty) ? 24f : 14f) + 4f;
                        }
                        dynamicCardHeight += 2f;
                    }
                    float modY2 = modY1 + dynamicCardHeight;

                    currentY += dynamicCardHeight + moduleSpacing;

                    if (modY2 < top) continue;
                    if (modY1 > bottom) break;

                    boolean isInsideContentArea = mouseX >= scissorX && mouseX <= right && mouseY >= scissorY && mouseY <= bottom - 5f;
                    boolean isModHovered = isInsideContentArea && mouseX >= modX1 && mouseX <= modX2 && mouseY >= modY1 && mouseY <= (modY1 + moduleHeight);
                    boolean isEnabled = module.isEnabled();

                    Color modBgColor = isModHovered ? new Color(32, 30, 40, 255) : new Color(28, 26, 34, 255);
                    DrawUtil.roundedRect(modX1, modY1, modX2, modY2, 6f, modBgColor);

                    if (isEnabled) {
                        Color borderAccent = new Color(accentPink.getRed(), accentPink.getGreen(), accentPink.getBlue(), 40);
                        DrawUtil.roundedRect(modX1 - 0.5f, modY1 - 0.5f, modX2 + 0.5f, modY2 + 0.5f, 6.5f, borderAccent);
                    }

                    float nameTextX = modX1 + 10f;
                    float nameTextY = modY1 + 11f;
                    Color nameTextColor = isEnabled ? Color.WHITE : new Color(170, 170, 175, 255);

                    DrawUtil.drawString(module.getName(), nameTextX, nameTextY, nameFontSize, nameTextColor, ResourceManager.FontResources.productSansBold);

                    float descTextX = modX1 + 10f;
                    float descTextY = nameTextY + 11f;
                    Color descTextColor = isEnabled ? new Color(200, 200, 205, 160) : new Color(120, 120, 125, 255);

                    DrawUtil.drawString(module.getDescription(), descTextX, descTextY, descFontSize, descTextColor, ResourceManager.FontResources.productSansBold);

                    if (module.isExpanded()) {
                        float settingY = modY1 + moduleHeight + 2f;
                        float settingSpacing = 4f;

                        for (var setting : module.getProperties()) {
                            if (setting.isHidden()) continue;

                            float rowX1 = modX1 + 6f;
                            float rowX2 = modX2 - 6f;
                            float settingHeight = (setting instanceof SliderProperty) ? 24f : 14f;

                            switch (setting) {
                                case BoolProperty boolProperty -> {
                                    DrawUtil.drawString(boolProperty.getName(), rowX1 + 8f, settingY + (settingHeight / 2f) + (descFontSize / 2f) - 1f, descFontSize, Color.WHITE, ResourceManager.FontResources.productSansBold);

                                    float dotRadius = 3f;
                                    float dotX = rowX2 - 14f;
                                    float dotY = settingY + (settingHeight / 2f);
                                    Color toggleColor = boolProperty.getValue() ? ColorUtils.getClientTheme() : new Color(54, 52, 66, 255);

                                    if (boolProperty.getValue()) {
                                        DrawUtil.drawShadow(dotX - dotRadius, dotY - dotRadius, dotRadius * 2, dotRadius * 2, dotRadius, 6f, toggleColor);
                                    }
                                    DrawUtil.roundedRect(dotX - dotRadius, dotY - dotRadius, dotX + dotRadius, dotY + dotRadius, dotRadius, toggleColor);
                                }

                                case EnumProperty<?> enumProperty -> {
                                    DrawUtil.drawString(enumProperty.getName(), rowX1 + 8f, settingY + (settingHeight / 2f) + (descFontSize / 2f) - 1f, descFontSize, Color.WHITE, ResourceManager.FontResources.productSansBold);

                                    String modeStr = enumProperty.getValue().toString();
                                    float modeW = (float) DrawUtil.getStringWidth(modeStr, descFontSize, ResourceManager.FontResources.productSansBold);
                                    DrawUtil.drawString(modeStr, rowX2 - 8f - modeW, settingY + (settingHeight / 2f) + (descFontSize / 2f) - 1f, descFontSize, new Color(160, 160, 165, 255), ResourceManager.FontResources.productSansBold);
                                }

                                case SliderProperty sliderProperty -> {
                                    DrawUtil.drawString(sliderProperty.getName(), rowX1 + 8f, settingY + 4f, descFontSize, Color.WHITE, ResourceManager.FontResources.productSansBold);

                                    float rawVal = sliderProperty.getValue();
                                    String valStr = (rawVal == (int) rawVal) ? String.valueOf((int) rawVal) : String.format(java.util.Locale.US, "%.2f", rawVal);
                                    Color themeColor = ColorUtils.getClientTheme();

                                    float valW = (float) DrawUtil.getStringWidth(valStr, descFontSize, ResourceManager.FontResources.productSansBold);
                                    DrawUtil.drawString(valStr, rowX2 - 8f - valW, settingY + 4f, descFontSize, new Color(180, 180, 185, 255), ResourceManager.FontResources.productSansBold);

                                    float trackX1 = rowX1 + 8f;
                                    float trackX2 = rowX2 - 8f;
                                    float trackW = trackX2 - trackX1;
                                    float trackY = settingY + 13f;
                                    float trackHeight = 2f;

                                    DrawUtil.roundedRect(trackX1, trackY, trackX2, trackY + trackHeight, 1f, new Color(42, 40, 52, 255));

                                    double fillPct = (sliderProperty.getValue() - sliderProperty.getMin()) / (sliderProperty.getMax() - sliderProperty.getMin());
                                    float fillWidth = (float) (trackW * Math.max(0.0, Math.min(1.0, fillPct)));

                                    if (fillWidth > 0) {
                                        DrawUtil.roundedRect(trackX1, trackY, trackX1 + fillWidth, trackY + trackHeight, 1f, themeColor);
                                    }

                                    float knobRadius = 2f;
                                    float knobX = trackX1 + fillWidth;
                                    float knobY = trackY + (trackHeight / 2f);

                                    DrawUtil.drawShadow(knobX - knobRadius, knobY - knobRadius, knobRadius * 2f, knobRadius * 2f, knobRadius, 8f, themeColor);
                                    DrawUtil.roundedRect(knobX - knobRadius, knobY - knobRadius, knobX + knobRadius, knobY + knobRadius, knobRadius, themeColor);
                                }
                                default -> {}
                            }
                            settingY += settingHeight + settingSpacing;
                        }
                    }
                }
                DrawUtil.resetScissor();
            }
        }

        DrawUtil.roundedRect(right - 5f, bottom - 5f, right - 2f, bottom - 2f, 1f, new Color(255, 255, 255, 40));
        DrawUtil.end();

        super.render(ctx, mouseX, mouseY, ticks);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        var button = event.button();
        var mouseX = (float) event.x();
        var mouseY = (float) event.y();

        float left = posX;
        float top = posY;
        float right = left + frameW;
        float bottom = top + frameH;
        float sidebarW = 110f;

        if (button == 0) {
            if (mouseX >= right - 12f && mouseX <= right && mouseY >= bottom - 12f && mouseY <= bottom) {
                isResizing = true;
                dragStartX = mouseX;
                dragStartY = mouseY;
                initialWidth = frameW;
                initialHeight = frameH;
                return true;
            }

            float titleY = top + 26f;
            float startCategoryY = titleY + 14f;
            float categoryHeight = 20f;
            float categorySpacing = 0f;

            MenuClickGuiCategory[] categories = MenuClickGuiCategory.values();
            for (int i = 0; i < categories.length; i++) {
                float catX1 = left + 8f;
                float catY1 = startCategoryY + (i * (categoryHeight + categorySpacing));
                float catX2 = left + sidebarW - 8f;
                float catY2 = catY1 + categoryHeight;

                if (mouseX >= catX1 && mouseX <= catX2 && mouseY >= catY1 && mouseY <= catY2) {
                    currentCategory = categories[i];
                    targetScrollOffset = 0f;
                    return true;
                }
            }
        }

        float startModuleY = top + 20f;
        float moduleHeight = 28f;
        float moduleSpacing = 4f;
        float currentY = startModuleY - scrollOffset;

        for (Module module : ModuleManager.getInstance().getModulesByCategory(currentCategory.name())) {
            float modX1 = left + sidebarW + 15f;
            float modY1 = currentY;
            float modX2 = right - 15f;

            float baseCardBottom = modY1 + moduleHeight;
            float dynamicCardHeight = moduleHeight;
            if (module.isExpanded()) {
                for (var setting : module.getProperties()) {
                    if (setting.isHidden()) continue;

                    dynamicCardHeight += ((setting instanceof SliderProperty) ? 24f : 14f) + 4f;
                }
                dynamicCardHeight += 2f;
            }
            float modY2 = modY1 + dynamicCardHeight;

            currentY += dynamicCardHeight + moduleSpacing;

            if (mouseY < top + 5f || mouseY > bottom - 5f) continue;

            if (mouseX >= modX1 && mouseX <= modX2 && mouseY >= modY1 && mouseY <= baseCardBottom) {
                if (button == 0) {
                    module.toggle();
                } else if (button == 1) {
                    module.setExpanded(!module.isExpanded());
                }
                return true;
            }

            if (module.isExpanded() && mouseX >= modX1 + 6f && mouseX <= modX2 - 6f && mouseY > baseCardBottom) {
                float settingY = modY1 + moduleHeight + 2f;
                float settingSpacing = 4f;

                for (var setting : module.getProperties()) {
                    if (setting.isHidden()) continue;


                    float settingHeight = (setting instanceof SliderProperty) ? 24f : 14f;
                    if (mouseY >= settingY && mouseY <= settingY + settingHeight) {
                        float rowX1 = modX1 + 6f;
                        float rowX2 = modX2 - 6f;

                        switch (setting) {
                            case BoolProperty boolProperty -> {
                                if (button == 0) {
                                    boolProperty.setValue(!boolProperty.getValue());
                                    return true;
                                }
                            }
                            case EnumProperty<?> enumProperty -> {
                                if (button == 0) {
                                    enumProperty.next();
                                    return true;
                                } else if (button == 1) {
                                    enumProperty.previous();
                                    return true;
                                }
                            }
                            case SliderProperty sliderProperty -> {
                                if (button == 0) {
                                    float trackX1 = rowX1 + 8f;
                                    float trackX2 = rowX2 - 8f;
                                    float trackW = trackX2 - trackX1;

                                    if (mouseX >= trackX1 - 4f && mouseX <= trackX2 + 4f) {
                                        activeSlider = sliderProperty;
                                        activeSliderX1 = trackX1;
                                        activeSliderW = trackW;
                                        return true;
                                    }
                                }
                            }
                            default -> {}
                        }
                    }
                    settingY += settingHeight + settingSpacing;
                }
            }
        }

        if (button == 0 && mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= top + 45f) {
            isDragging = true;
            dragStartX = mouseX - posX;
            dragStartY = mouseY - posY;
            return true;
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= posX + 110f && mouseX <= posX + frameW && mouseY >= posY && mouseY <= posY + frameH) {
            targetScrollOffset -= (float) (scrollY * 24.0);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            isDragging = false;
            isResizing = false;
            activeSlider = null;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}