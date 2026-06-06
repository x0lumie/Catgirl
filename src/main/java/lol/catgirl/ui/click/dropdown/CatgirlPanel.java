package lol.catgirl.ui.click.dropdown;

import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.Property;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.ui.click.dropdown.property.DropdownProperty;
import lol.catgirl.ui.click.dropdown.property.impl.DropdownBoolProperty;
import lol.catgirl.ui.click.dropdown.property.impl.DropdownEnumProperty;
import lol.catgirl.ui.click.dropdown.property.impl.DropdownSliderProperty;
import lol.catgirl.utils.client.Animation;
import lol.catgirl.utils.client.Easing;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

import java.awt.*;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CatgirlPanel {

    private static final Map<Property<?>, DropdownProperty> propertyRenderers = new IdentityHashMap<>();
    private static final Map<Module, Animation> moduleAnimations = new IdentityHashMap<>();
    private static final Map<Module, Animation> toggleAnimations = new IdentityHashMap<>();

    private static final Map<ModuleCategory, Float> targetScrolls = new IdentityHashMap<>();
    private static final Map<ModuleCategory, Float> currentScrolls = new IdentityHashMap<>();
    private static final Map<ModuleCategory, Point.Float> panelPositions = new IdentityHashMap<>();
    private static final Map<ModuleCategory, Animation> collapseAnimations = new IdentityHashMap<>();
    private static final Set<ModuleCategory> collapsedPanels = new HashSet<>();

    private static ModuleCategory draggingCategory = null;
    private static float dragOffsetX = 0f;
    private static float dragOffsetY = 0f;

    private static Module hoveredModuleThisFrame = null;
    private static Module lastHoveredModule = null;
    private static final Animation tooltipAnimation = new Animation(Easing.DECELERATE, 70L);
    private static float interpolatedTooltipX = 0f;
    private static float interpolatedTooltipY = 0f;

    public static Point.Float getPosition(ModuleCategory category, float defaultX, float defaultY) {
        return panelPositions.computeIfAbsent(category, c -> new Point.Float(defaultX, defaultY));
    }

    public static boolean isCollapsed(ModuleCategory category) {
        return collapsedPanels.contains(category);
    }

    private static DropdownProperty getRenderer(Property<?> property) {
        return propertyRenderers.computeIfAbsent(property, CatgirlPanel::createRenderer);
    }

    private static DropdownProperty createRenderer(Property<?> property) {
        if (property instanceof BoolProperty bool) return new DropdownBoolProperty(bool);
        if (property instanceof SliderProperty slider) return new DropdownSliderProperty(slider);
        if (property instanceof EnumProperty<?> en) return new DropdownEnumProperty(en);
        throw new IllegalArgumentException("Unsupported property type: " + property.getClass().getName());
    }

    public static void draw(ModuleCategory category, float defaultX, float defaultY, float mouseX, float mouseY, float partialTick) {
        float width = 100;
        float headerHeight = 18;
        float maxBodyHeight = 200f;

        Point.Float pos = getPosition(category, defaultX, defaultY);
        if (draggingCategory == category) {
            float targetX = mouseX - dragOffsetX;
            float targetY = mouseY - dragOffsetY;
            pos.x += (targetX - pos.x) * 0.25f * (partialTick * 2f);
            pos.y += (targetY - pos.y) * 0.25f * (partialTick * 2f);
        }

        float x = pos.x;
        float y = pos.y;

        boolean collapsed = isCollapsed(category);
        Animation collapseAnim = collapseAnimations.computeIfAbsent(category, c -> new Animation(Easing.DECELERATE, 300L));
        collapseAnim.run(collapsed ? 0f : 1f);
        float collapseProgress = collapseAnim.getValue();

        float animatedBodyHeight = maxBodyHeight * collapseProgress;
        Color mainColor = new Color(0, 0, 0, 250);
        float radius = 6f;

        if (animatedBodyHeight <= 0.1f) {
            DrawUtil.roundedRect(x, y, x + width, y + headerHeight, radius, mainColor);
        } else {
            DrawUtil.roundedRectVarying(x, y, x + width, y + headerHeight + animatedBodyHeight, radius, radius, radius, radius, mainColor);
            DrawUtil.roundedRectVarying(x, y, x + width, y + headerHeight, radius, radius, 0f, 0f, mainColor);
        }

        String text = category.name();
        float textWidth = (float) DrawUtil.getStringWidth(text, 10f, ResourceManager.getSelectedFont());
        float centerX = x + (100f / 2f) - (textWidth / 2f);
        DrawUtil.drawString(text, centerX, y + 13, 10f, Color.WHITE, ResourceManager.getSelectedFont());

        if (collapseProgress < 0.01f) return;

        if (category == ModuleCategory.values()[0] || lastHoveredModule == null || lastHoveredModule.getCategory() == category) {
            hoveredModuleThisFrame = null;
        }

        String query = CatgirlDropdown.getSearchQuery();

        float totalContentHeight = 4f;
        for (Module m : ModuleManager.modules) {
            if (m.getCategory() != category) continue;
            if (!query.isEmpty() && !m.getName().toLowerCase().contains(query)) continue;

            totalContentHeight += 12f + 4f;

            Animation anim = moduleAnimations.get(m);
            float progress = (anim != null) ? anim.getValue() : (m.isExpanded() ? 1f : 0f);
            if (progress > 0.01f) {
                for (Property<?> property : m.getProperties()) {
                    if (property.isHidden()) continue;
                    float basePropHeight = property instanceof SliderProperty ? 17f : (property instanceof EnumProperty<?> ? 11f : 12f);
                    totalContentHeight += basePropHeight * progress;
                }
            }
        }

        float maxScroll = Math.max(0f, totalContentHeight - (maxBodyHeight - 4f));
        float targetScroll = targetScrolls.computeIfAbsent(category, c -> 0f);
        if (targetScroll < 0f) targetScroll = 0f;
        if (targetScroll > maxScroll) targetScroll = maxScroll;
        targetScrolls.put(category, targetScroll);

        float currentScroll = currentScrolls.computeIfAbsent(category, c -> 0f);
        currentScroll += (targetScroll - currentScroll) * 0.15f * (partialTick * 2f);
        if (Math.abs(targetScroll - currentScroll) < 0.1f) currentScroll = targetScroll;
        currentScrolls.put(category, currentScroll);

        long vg = DrawUtil.context;
        NanoVG.nvgSave(vg);

        NanoVG.nvgScissor(vg, x + 2, y + headerHeight + 1, 96, animatedBodyHeight - 2);

        float moduleY = (y + headerHeight + 4) - currentScroll;

        for (Module module : ModuleManager.modules) {
            if (module.getCategory() != category) continue;
            if (!query.isEmpty() && !module.getName().toLowerCase().contains(query)) continue;

            float rowHeight = 12f;
            String moduleNameText = module.getName();

            float minX = x + 2;
            float maxX = x + 98;
            float minY = moduleY - 2;
            float maxY = moduleY + rowHeight;

            DrawUtil.roundedRect(minX, minY, maxX, maxY, 2f, mainColor);

            boolean isWithinScissorBounds = moduleY >= (y + headerHeight) && (moduleY + rowHeight) <= (y + headerHeight + animatedBodyHeight);
            if (isWithinScissorBounds && mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY && draggingCategory == null) {
                hoveredModuleThisFrame = module;
                lastHoveredModule = module;
            }

            Animation toggleAnim = toggleAnimations.computeIfAbsent(module, mod -> new Animation(Easing.DECELERATE, 300L));
            toggleAnim.run(module.isEnabled() ? 1f : 0f);
            float enableProgress = toggleAnim.getValue();

            if (enableProgress > 0.001f) {
                float boxCenterX = (minX + maxX) / 2f;
                float boxCenterY = (minY + maxY) / 2f;
                float animMinX = boxCenterX + (minX - boxCenterX) * enableProgress;
                float animMaxX = boxCenterX + (maxX - boxCenterX) * enableProgress;
                float animMinY = boxCenterY + (minY - boxCenterY) * enableProgress;
                float animMaxY = boxCenterY + (maxY - boxCenterY) * enableProgress;

                DrawUtil.roundedRect(animMinX, animMinY, animMaxX, animMaxY, 2f, new Color(120, 60, 160, 120));
            }

            float moduleNameTextWidth = (float) DrawUtil.getStringWidth(moduleNameText, 8f, ResourceManager.getSelectedFont());
            float moduleNameCenterX = x + (100f / 2f) - (moduleNameTextWidth / 2f);
            DrawUtil.drawString(moduleNameText, moduleNameCenterX, moduleY + 8, 8f, Color.WHITE, ResourceManager.getSelectedFont());

            moduleY += rowHeight + 4;

            Animation anim = moduleAnimations.computeIfAbsent(module, mod -> new Animation(Easing.DECELERATE, 250L));
            anim.run(module.isExpanded() ? 1f : 0f);
            float progress = anim.getValue();

            if (progress > 0.01f) {
                for (Property<?> property : module.getProperties()) {
                    if (property.isHidden()) continue;

                    DropdownProperty dropdownProperty = getRenderer(property);
                    float renderY = moduleY;
                    if (property instanceof SliderProperty) {
                        renderY -= 4f;
                    }

                    dropdownProperty.draw(x + 2, renderY, mouseX, mouseY, partialTick);

                    if (property instanceof SliderProperty) {
                        moduleY += 17f * progress;
                    } else if (property instanceof EnumProperty<?>) {
                        moduleY += 11f * progress;
                    } else {
                        moduleY += 12f * progress;
                    }
                }
            }
        }

        float fadeHeight = 10f;
        NVGPaint paint = NVGPaint.calloc();
        NVGColor colorTransparent = NVGColor.calloc().r(0f).g(0f).b(0f).a(0f);
        NVGColor colorSolid = NVGColor.calloc()
                .r(mainColor.getRed() / 255f).g(mainColor.getGreen() / 255f)
                .b(mainColor.getBlue() / 255f).a(mainColor.getAlpha() / 255f);

        if (currentScroll > 1f) {
            NanoVG.nvgLinearGradient(vg, x + 2, y + headerHeight, x + 2, y + headerHeight + fadeHeight, colorSolid, colorTransparent, paint);
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRect(vg, x + 2, y + headerHeight, 96, fadeHeight);
            NanoVG.nvgFillPaint(vg, paint);
            NanoVG.nvgFill(vg);
        }

        if (currentScroll < maxScroll - 1f && maxScroll > 0f) {
            float panelBottomY = y + headerHeight + animatedBodyHeight;
            NanoVG.nvgLinearGradient(vg, x + 2, panelBottomY - fadeHeight, x + 2, panelBottomY, colorTransparent, colorSolid, paint);
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRect(vg, x + 2, panelBottomY - fadeHeight, 96, fadeHeight);
            NanoVG.nvgFillPaint(vg, paint);
            NanoVG.nvgFill(vg);
        }

        paint.free();
        colorTransparent.free();
        colorSolid.free();

        NanoVG.nvgRestore(vg);

        tooltipAnimation.run(hoveredModuleThisFrame != null ? 1f : 0f);
        float tooltipProgress = tooltipAnimation.getValue();

        if (tooltipProgress > 0.01f && lastHoveredModule != null) {
            String desc = lastHoveredModule.getDescription();
            if (desc != null && !desc.isEmpty()) {
                float fontSize = 7.5f;
                float textPadding = 5f;
                float maxTextWidth = (float) DrawUtil.getStringWidth(desc, fontSize, ResourceManager.getSelectedFont());

                float boxWidth = maxTextWidth + (textPadding * 2f);
                float boxHeight = fontSize + (textPadding * 2f) + 1f;

                float targetTooltipX = mouseX + 8f;
                float targetTooltipY = mouseY + 8f;
                interpolatedTooltipX += (targetTooltipX - interpolatedTooltipX) * 0.3f * (partialTick * 2f);
                interpolatedTooltipY += (targetTooltipY - interpolatedTooltipY) * 0.3f * (partialTick * 2f);

                int bgAlpha = (int) (240 * tooltipProgress);
                int textAlpha = (int) (255 * tooltipProgress);

                Color tooltipBg = new Color(10, 10, 10, bgAlpha);
                Color tooltipText = new Color(220, 220, 220, textAlpha);

                DrawUtil.roundedRect(interpolatedTooltipX, interpolatedTooltipY, interpolatedTooltipX + boxWidth, interpolatedTooltipY + boxHeight, 3f, tooltipBg);
                DrawUtil.drawString(desc, interpolatedTooltipX + textPadding, interpolatedTooltipY + textPadding + fontSize, fontSize, tooltipText, ResourceManager.getSelectedFont());
            }
        }
    }

    public static void handleScroll(ModuleCategory category, double deltaY) {
        if (isCollapsed(category)) return;
        float currentTarget = targetScrolls.computeIfAbsent(category, c -> 0f);
        targetScrolls.put(category, currentTarget - ((float) deltaY * 16f));
    }

    public static boolean mouseClicked(ModuleCategory category, float defaultX, float defaultY, float mouseX, float mouseY, int button) {
        float width = 100;
        float headerHeight = 18;
        float maxBodyHeight = 200f;

        Point.Float pos = getPosition(category, defaultX, defaultY);
        float x = pos.x;
        float y = pos.y;

        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight) {
            if (button == 0) {
                draggingCategory = category;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
                return true;
            } else if (button == 1) {
                if (collapsedPanels.contains(category)) {
                    collapsedPanels.remove(category);
                } else {
                    collapsedPanels.add(category);
                }
                return true;
            }
            return false;
        }

        Animation collapseAnim = collapseAnimations.get(category);
        float currentProgress = collapseAnim != null ? collapseAnim.getValue() : (isCollapsed(category) ? 0f : 1f);
        float currentBodyHeight = maxBodyHeight * currentProgress;

        if (currentProgress < 0.05f || mouseX < x + 2 || mouseX > x + width - 2 || mouseY < y + headerHeight || mouseY > y + headerHeight + currentBodyHeight) {
            return false;
        }

        String query = CatgirlDropdown.getSearchQuery();

        float currentScroll = currentScrolls.computeIfAbsent(category, c -> 0f);
        float moduleY = (y + headerHeight + 4) - currentScroll;
        float rowHeight = 12f;

        for (Module module : ModuleManager.modules) {
            if (module.getCategory() != category) continue;
            if (!query.isEmpty() && !module.getName().toLowerCase().contains(query)) continue;

            boolean isVisible = moduleY >= (y + headerHeight) && (moduleY + rowHeight) <= (y + headerHeight + currentBodyHeight);
            boolean hovering = isVisible && mouseX >= x + 2 && mouseX <= x + 98 && mouseY >= moduleY - 2 && mouseY <= moduleY + rowHeight;

            if (hovering) {
                if (button == 0) module.toggle();
                if (button == 1) module.setExpanded(!module.isExpanded());
                return true;
            }

            moduleY += rowHeight + 4;

            Animation anim = moduleAnimations.get(module);
            float progress = (anim != null) ? anim.getValue() : (module.isExpanded() ? 1f : 0f);

            if (progress > 0.01f) {
                for (Property<?> property : module.getProperties()) {
                    if (property.isHidden()) continue;

                    float basePropHeight = property instanceof SliderProperty ? 17f : (property instanceof EnumProperty<?> ? 11f : 12f);
                    float currentPropHeight = basePropHeight * progress;

                    float propertyStartY = moduleY;
                    if (property instanceof SliderProperty) {
                        propertyStartY -= 4f;
                    }
                    float propertyEndY = propertyStartY + currentPropHeight;

                    boolean isPropVisible = propertyEndY >= (y + headerHeight) && propertyStartY <= (y + headerHeight + currentBodyHeight);

                    if (isPropVisible && mouseX >= x + 2 && mouseX <= x + 98 && mouseY >= propertyStartY && mouseY <= propertyEndY) {
                        DropdownProperty dropdownProperty = getRenderer(property);
                        double localMouseX = mouseX - (x + 4);
                        double localMouseY = mouseY - propertyStartY;

                        if (dropdownProperty instanceof DropdownEnumProperty enumProperty) {
                            enumProperty.mouseClicked(localMouseX, localMouseY, button);
                        } else {
                            dropdownProperty.mouseClicked(localMouseX, localMouseY);
                        }
                        return true;
                    }

                    moduleY += currentPropHeight;
                }
            }
        }
        return false;
    }

    public static void mouseReleased() {
        draggingCategory = null;
        for (DropdownProperty dropdownProperty : propertyRenderers.values()) {
            dropdownProperty.mouseReleased();
        }
    }
}