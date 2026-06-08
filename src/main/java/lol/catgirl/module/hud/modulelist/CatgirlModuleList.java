package lol.catgirl.module.hud.modulelist;

import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.InterfaceModule;
import lol.catgirl.module.hud.ModuleListModule;
import lol.catgirl.module.hud.WatermarkModule;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;

import java.awt.*;
import java.util.HashMap;

public class CatgirlModuleList implements IMinecraft {
    private static final HashMap<String, Double> xOffsets = new HashMap<>();

    public static void onRender(RenderTickEvent event, ModuleListModule parent) {
        DrawUtil.begin();

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int TEXT_SIZE = parent.textSize.getValue().intValue();
        int SUFFIX_TEXT_SIZE = parent.suffixTextSize.getValue().intValue();
        boolean background = parent.background.getValue();
        boolean bar = parent.bar.getValue();
        float animationSpeed = parent.animSpeed.getValue();
        boolean isLeft = parent.isLeft.getValue();
        int PADDING_X = parent.paddingX.getValue().intValue();
        int PADDING_Y = parent.paddingY.getValue().intValue();
        int SPACING = parent.spacing.getValue().intValue();
        int CORNER_RADIUS = parent.cornerRadius.getValue().intValue();
        float SIDEBAR_WIDTH = parent.sidebarWidth.getValue().intValue();

        java.util.List<lol.catgirl.module.Module> modules = ModuleManager.modules.stream()
                .filter(mod -> {
                    boolean visible = mod.isEnabled()
                            || (isLeft ? xOffsets.getOrDefault(mod.getDisplayName(),
                            100.0) > (-getWidth(mod, TEXT_SIZE, SUFFIX_TEXT_SIZE, parent) - 19)
                            : xOffsets.getOrDefault(mod.getDisplayName(), 100.0)
                              < (getWidth(mod, TEXT_SIZE, SUFFIX_TEXT_SIZE, parent) + 19));

                    if (!visible) {
                        return false;
                    }

                    if (parent.excludeCombat.getValue()
                            && mod.getCategory() == ModuleCategory.Combat) {
                        return false;
                    }

                    if (parent.excludeMovement.getValue()
                            && mod.getCategory() == ModuleCategory.Movement) {
                        return false;
                    }

                    if (parent.excludePlayer.getValue()
                            && mod.getCategory() == ModuleCategory.Player) {
                        return false;
                    }

                    if (parent.excludeRender.getValue()
                            && mod.getCategory() == ModuleCategory.Render) {
                        return false;
                    }

                    if (parent.excludeHud.getValue()
                            && mod.getCategory() == ModuleCategory.Hud) {
                        return false;
                    }

                    if (parent.excludeClient.getValue()
                            && mod.getCategory() == ModuleCategory.Client) {
                        return false;
                    }

                    if (parent.excludeGhost.getValue()
                            && mod.getCategory() == ModuleCategory.Ghost) {
                        return false;
                    }


                    return true;
                })
                .sorted((m1, m2) -> Double.compare(
                        getWidth(m2, TEXT_SIZE, SUFFIX_TEXT_SIZE, parent),
                        getWidth(m1, TEXT_SIZE, SUFFIX_TEXT_SIZE, parent)))
                .toList();


        int y = 10;

        // i want to make it a switch case but it looked bad with it :plead:
        WatermarkModule watermarkModule = WatermarkModule.INSTANCE;
        if (watermarkModule.isEnabled() && isLeft) {
            if (watermarkModule.mode.getValue() == WatermarkModule.Mode.Catgirl) {
                y += 27;
            }
            if (watermarkModule.mode.getValue() == WatermarkModule.Mode.Catsense) {
                y += 17;
            }
            if (watermarkModule.mode.getValue() == WatermarkModule.Mode.Simple) {
                y += 16;
            }
            if (watermarkModule.mode.getValue() == WatermarkModule.Mode.Wurst) {
                y += 27;
            }

            if (watermarkModule.mode.getValue() == WatermarkModule.Mode.Classic) {
                y += 21;
            }

        } else {
            y = 10;
        }

        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            String name = mod.getDisplayName();
            String suffix = mod.getSuffix();
            if (!mod.isVisible.getValue()) continue;

            double totalWidth = getWidth(mod, TEXT_SIZE, SUFFIX_TEXT_SIZE, parent);
            double targetX = mod.isEnabled() ? 0 : (isLeft ? -totalWidth - 20 : totalWidth + 20);

            double currentX = xOffsets.getOrDefault(name, targetX);
            currentX += (targetX - currentX) * animationSpeed;
            xOffsets.put(name, currentX);

            float alpha = isLeft ? (float) Math.clamp(1.0 - (Math.abs(currentX) / (totalWidth + 20)), 0, 1)
                    : (float) Math.clamp(1.0 - (currentX / (totalWidth + 20)), 0, 1);

            if (alpha <= 0.01f) continue;

            double xPositioningOffset =
                    parent.xPositioningOffset.getValue().intValue();

            double x = isLeft
                    ? (xPositioningOffset + currentX) :
                    (screenWidth - totalWidth
                     - PADDING_X - xPositioningOffset + currentX);

            float bgTop = (float) (y - TEXT_SIZE * 0.8 - PADDING_Y);
            float bgBottom = bgTop + SPACING + 0.1f;

            float bgLeft = (float) (x - PADDING_X);
            float bgRight = (float) (x + totalWidth + PADDING_X);

            boolean isFirst = (i == 0);
            boolean isLast = (i == modules.size() - 1);

            float tl = CORNER_RADIUS;
            float tr = CORNER_RADIUS;
            float bl = CORNER_RADIUS;
            float br = CORNER_RADIUS;

            if (isLeft) {
                tl = isFirst ? CORNER_RADIUS : 0;
                bl = isLast ? CORNER_RADIUS : 0;

                if (!isFirst) {
                    double prevModuleWidth = getWidth(modules.get(i - 1),
                            TEXT_SIZE, SUFFIX_TEXT_SIZE, parent
                    );
                    if (prevModuleWidth >= totalWidth) {
                        tr = 0;
                    }
                }
            } else {
                tr = isFirst ? CORNER_RADIUS : 0;
                br = isLast ? CORNER_RADIUS : 0;

                if (!isFirst) {
                    double prevModuleWidth = getWidth(
                            modules.get(i - 1),
                            TEXT_SIZE, SUFFIX_TEXT_SIZE, parent
                    );
                    if (prevModuleWidth >= totalWidth) {
                        tl = 0;
                    }
                }
            }

            Color bgColor = new Color(20, 20, 20, (int) (150 * alpha));
            if (background) {
                if (parent.shadow.getValue()) {
                    DrawUtil.drawShadow(
                            bgLeft,
                            bgTop,
                            bgRight - bgLeft,
                            bgBottom - bgTop,
                            CORNER_RADIUS,
                            14f,
                            new Color(0, 0, 0, (int) (100 * alpha))
                    );
                }

                DrawUtil.roundedRectVarying(bgLeft, bgTop, bgRight, bgBottom, tl, tr, br, bl, bgColor);
            }

            Color accentColor = ColorUtils.getAnimatedColor(i, alpha);

            if (bar) {
                if (isLeft) {
                    DrawUtil.roundedRectVarying(
                            bgLeft, bgTop,
                            bgLeft + SIDEBAR_WIDTH, bgBottom,
                            tl, 0, 0, bl,
                            accentColor
                    );
                } else {
                    DrawUtil.roundedRectVarying(
                            bgRight - SIDEBAR_WIDTH,
                            bgTop, bgRight, bgBottom,
                            0, tr, br, 0,
                            accentColor
                    );
                }
            }

            Color mainColor = accentColor;
            Color suffixColor = ColorUtils.changeOpacity(
                    new Color(170, 170, 170),
                    (int) (255 * alpha)
            );

            if (!suffix.isEmpty() && parent.suffix.getValue()) {
                double suffixWidth = DrawUtil.getStringWidth(" " + suffix,
                        SUFFIX_TEXT_SIZE, ResourceManager.getSelectedFont()
                );
                DrawUtil.drawString(name, x, y,
                        TEXT_SIZE, mainColor,
                        ResourceManager.getSelectedFont());
                DrawUtil.drawString(" " +
                                suffix, x + (totalWidth - suffixWidth), y,
                        SUFFIX_TEXT_SIZE, suffixColor,
                        ResourceManager.getSelectedFont());
            } else {
                DrawUtil.drawString(name, x, y, TEXT_SIZE,
                        mainColor, ResourceManager.getSelectedFont());
            }

            y += SPACING * alpha;
        }
        DrawUtil.end();
    }

    private static double getWidth(Module mod, int TEXT_SIZE, int SUFFIX_TEXT_SIZE, ModuleListModule module)
    {
        String name = mod.getDisplayName();

        double nameWidth = DrawUtil.getStringWidth(
                name,
                TEXT_SIZE,
                ResourceManager.getSelectedFont()
        );

        if (!module.suffix.getValue()) {
            return nameWidth;
        }

        String suffix = mod.getSuffix();
        double suffixWidth = DrawUtil.getStringWidth(
                " " + suffix,
                SUFFIX_TEXT_SIZE,
                ResourceManager.getSelectedFont()
        );
        return nameWidth + suffixWidth;
    }
}
