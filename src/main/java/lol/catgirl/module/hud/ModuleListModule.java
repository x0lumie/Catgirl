package lol.catgirl.module.hud;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.InterfaceModule;
import lol.catgirl.setting.impl.BoolSetting;
import lol.catgirl.setting.impl.SliderSetting;
import lol.catgirl.utils.client.ColorUtil;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;

import java.awt.*;
import java.util.HashMap;

public class ModuleListModule extends Module {
    public static final ModuleListModule INSTANCE = new ModuleListModule();

    public final BoolSetting background = new BoolSetting("Background", true);
    public final BoolSetting bar = new BoolSetting("Bar", true);
    public final BoolSetting isLeft = new BoolSetting("Position Left", false);
    public final BoolSetting suffix = new BoolSetting("Suffix", true);
    public final BoolSetting shadow = new BoolSetting("Shadow", false);
    public final SliderSetting textSize = new SliderSetting("Text Size", 9, 6, 20, 1);
    public final SliderSetting suffixTextSize = new SliderSetting("Suffix Text Size", 9, 6, 20, 1).hide(() ->  !suffix.getValue());
    public final SliderSetting paddingX = new SliderSetting("Padding X", 2, 0, 10, 1);
    public final SliderSetting paddingY = new SliderSetting("Padding Y", 1, 0, 10, 1);
    public final SliderSetting spacing = new SliderSetting("Spacing", 11, 5, 25, 1);
    public final SliderSetting cornerRadius = new SliderSetting("Corner Radius", 2, 0, 15, 1);
    public final SliderSetting sidebarWidth = new SliderSetting("Sidebar Width", 1.2f, 0.5f, 6.0f, 0.1f).hide(() -> !bar.getValue());
    public final SliderSetting animSpeed = new SliderSetting("Animation Speed", 0.2f, 0.05f, 1.0f, 0.01f);
    public final BoolSetting excludeCombat = new BoolSetting("Exclude Combat", false);
    public final BoolSetting excludeMovement = new BoolSetting("Exclude Movement", false);
    public final BoolSetting excludePlayer = new BoolSetting("Exclude Player", false);
    public final BoolSetting excludeRender = new BoolSetting("Exclude Render", false);
    public final BoolSetting excludeHud = new BoolSetting("Exclude Hud", false);
    public final BoolSetting excludeClient = new BoolSetting("Exclude Client", false);
    public final BoolSetting excludeGhost = new BoolSetting("Exclude Ghost", false);

    public ModuleListModule() {
        super("ModuleList", "Shows a hud with currently enabled modules.", ModuleCategory.Hud);
        addSettings(
                background, animSpeed, shadow, bar, isLeft, suffix, textSize, suffixTextSize,
                paddingX, paddingY, spacing, cornerRadius, sidebarWidth,
                excludeCombat, excludeMovement, excludePlayer, excludeHud,
                excludeRender, excludeCombat, excludeGhost, excludeClient
        );
    }

    private static final HashMap<String, Double> xOffsets = new HashMap<>();

    @EventHook
    public void onRender(RenderTickEvent event) {
        DrawUtil.begin();

        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int TEXT_SIZE = textSize.getValue().intValue();
        int SUFFIX_TEXT_SIZE = suffixTextSize.getValue().intValue();
        boolean background = this.background.getValue();
        boolean bar = this.bar.getValue();
        float animationSpeed = this.animSpeed.getValue();
        boolean isLeft = this.isLeft.getValue();
        int PADDING_X = this.paddingX.getValue().intValue();
        int PADDING_Y = this.paddingY.getValue().intValue();
        int SPACING = this.spacing.getValue().intValue();
        int CORNER_RADIUS = this.cornerRadius.getValue().intValue();
        float SIDEBAR_WIDTH = this.sidebarWidth.getValue().intValue();

        java.util.List<Module> modules = ModuleManager.modules.stream()
                .filter(mod -> {
                    boolean visible = mod.isEnabled()
                            || (isLeft ? xOffsets.getOrDefault(mod.getDisplayName(),
                            100.0) > (-getWidth(mod, TEXT_SIZE, SUFFIX_TEXT_SIZE) - 19)
                            : xOffsets.getOrDefault(mod.getDisplayName(), 100.0)
                              < (getWidth(mod, TEXT_SIZE, SUFFIX_TEXT_SIZE) + 19));

                    if (!visible) {
                        return false;
                    }

                    if (excludeCombat.getValue()
                            && mod.getCategory() == ModuleCategory.Combat) {
                        return false;
                    }

                    if (excludeMovement.getValue()
                            && mod.getCategory() == ModuleCategory.Movement) {
                        return false;
                    }

                    if (excludePlayer.getValue()
                            && mod.getCategory() == ModuleCategory.Player) {
                        return false;
                    }

                    if (excludeRender.getValue()
                            && mod.getCategory() == ModuleCategory.Render) {
                        return false;
                    }

                    if (excludeHud.getValue()
                            && mod.getCategory() == ModuleCategory.Hud) {
                        return false;
                    }

                    if (excludeClient.getValue()
                            && mod.getCategory() == ModuleCategory.Client) {
                        return false;
                    }

                    if (excludeGhost.getValue()
                            && mod.getCategory() == ModuleCategory.Ghost) {
                        return false;
                    }


                    return true;
                })
                .sorted((m1, m2) -> Double.compare(
                        getWidth(m2, TEXT_SIZE, SUFFIX_TEXT_SIZE),
                        getWidth(m1, TEXT_SIZE, SUFFIX_TEXT_SIZE)))
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
                y += 10;
            }
        } else {
            y = 10;
        }

        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            String name = mod.getDisplayName();
            String suffix = mod.suffix();
            if (!mod.isVisible.getValue()) continue;

            double totalWidth = getWidth(mod, TEXT_SIZE, SUFFIX_TEXT_SIZE);
            double targetX = mod.isEnabled() ? 0 : (isLeft ? -totalWidth - 20 : totalWidth + 20);

            double currentX = xOffsets.getOrDefault(name, targetX);
            currentX += (targetX - currentX) * animationSpeed;
            xOffsets.put(name, currentX);

            float alpha = isLeft ? (float) Math.clamp(1.0 - (Math.abs(currentX) / (totalWidth + 20)), 0, 1)
                    : (float) Math.clamp(1.0 - (currentX / (totalWidth + 20)), 0, 1);

            if (alpha <= 0.01f) continue;

            double x = isLeft ? (5 + currentX) : (screenWidth - totalWidth - PADDING_X - 5 + currentX);

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
                    double prevModuleWidth = getWidth(modules.get(i - 1), TEXT_SIZE, SUFFIX_TEXT_SIZE);
                    if (prevModuleWidth >= totalWidth) {
                        tr = 0;
                    }
                }
            } else {
                tr = isFirst ? CORNER_RADIUS : 0;
                br = isLast ? CORNER_RADIUS : 0;

                if (!isFirst) {
                    double prevModuleWidth = getWidth(modules.get(i - 1), TEXT_SIZE, SUFFIX_TEXT_SIZE);
                    if (prevModuleWidth >= totalWidth) {
                        tl = 0;
                    }
                }
            }

            Color bgColor = new Color(20, 20, 20, (int) (150 * alpha));
            if (background) {
                if (shadow.getValue()) {
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

            Color accentColor;

            long time = System.currentTimeMillis();

            Color PINK = new Color(255, 105, 180);
            Color PURPLE = new Color(155, 89, 255);

            switch (InterfaceModule.INSTANCE.colorMode.getValue()) {

                case Static -> {
                    accentColor = applyAlpha(PINK, alpha);
                }

                case Wave -> {
                    float wave = (float) (
                            (Math.sin((time / 350.0) + (i * 0.30)) + 1.0) / 2.0
                    );

                    Color blended = DrawUtil.interpolate(PINK, PURPLE, wave);

                    accentColor = applyAlpha(blended, alpha);
                }

                case Pulse -> {
                    float pulse = (float) (
                            (Math.sin(time / 350.0) + 1.0) / 2.0
                    );

                    Color blended = DrawUtil.interpolate(PINK, PURPLE, pulse);

                    accentColor = applyAlpha(blended, alpha);
                }

                default -> {
                    accentColor = applyAlpha(PINK, alpha);
                }
            }
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
            Color suffixColor = applyAlpha(new Color(170, 170, 170), alpha);

            if (!suffix.isEmpty() && this.suffix.getValue()) {
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

    private double getWidth(Module mod, int TEXT_SIZE, int SUFFIX_TEXT_SIZE)
    {
        String name = mod.getDisplayName();

        double nameWidth = DrawUtil.getStringWidth(
                name,
                TEXT_SIZE,
                ResourceManager.getSelectedFont()
        );

        if (!this.suffix.getValue()) {
            return nameWidth;
        }

        String suffix = mod.suffix();
        double suffixWidth = DrawUtil.getStringWidth(
                " " + suffix,
                SUFFIX_TEXT_SIZE,
                ResourceManager.getSelectedFont()
        );
        return nameWidth + suffixWidth;
    }

    private static Color applyAlpha(Color color, float alpha) {
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int) (255 * alpha)
        );
    }
}
