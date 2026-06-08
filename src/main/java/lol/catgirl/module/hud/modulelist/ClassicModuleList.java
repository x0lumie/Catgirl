package lol.catgirl.module.hud.modulelist;

import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.client.InterfaceModule;
import lol.catgirl.module.hud.ModuleListModule;

import java.awt.*;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.render.nanovg.DrawUtil;

public class ClassicModuleList implements IMinecraft {

    public static void onRender(Render2DEvent event, ModuleListModule parent) {
        java.util.List<Module> enabledModules = new java.util.ArrayList<>();

        for (Module mod : ModuleManager.modules) {
            if (!mod.isEnabled()) {
                continue;
            }

            if (parent.excludeRender.getValue()
                    && mod.getCategory() == ModuleCategory.Render) {
                continue;
            }

            if (parent.excludeGhost.getValue()
                    && mod.getCategory() == ModuleCategory.Ghost) {
                continue;
            }

            if (parent.excludeCombat.getValue()
                    && mod.getCategory() == ModuleCategory.Combat) {
                continue;
            }

            if (parent.excludeClient.getValue()
                    && mod.getCategory() == ModuleCategory.Client) {
                continue;
            }

            if (parent.excludePlayer.getValue()
                    && mod.getCategory() == ModuleCategory.Player) {
                continue;
            }

            if (parent.excludeMovement.getValue()
                    && mod.getCategory() == ModuleCategory.Movement) {
                continue;
            }

            enabledModules.add(mod);
        }

        enabledModules.sort((a, b) -> {
            boolean showSuffix = parent.suffix.getValue();

            String aText = (a.getSuffix() != null && !a.getSuffix().isEmpty() && showSuffix)
                    ? a.getDisplayName() + " " + a.getSuffix()
                    : a.getDisplayName();

            String bText = (b.getSuffix() != null && !b.getSuffix().isEmpty() && showSuffix)
                    ? b.getDisplayName() + " " + b.getSuffix()
                    : b.getDisplayName();

            return Float.compare(
                    mc.font.width(bText),
                    mc.font.width(aText)
            );
        });

        int y = 5;

        for (int i = 0; i < enabledModules.size(); i++) {
            Module module = enabledModules.get(i);

            if (!module.isVisible.getValue()) continue;

            String name = module.getDisplayName();
            String suffix = module.getSuffix();
            boolean hasSuffix = (suffix != null && !suffix.isEmpty());

            boolean showSuffix = hasSuffix &&
                    parent.suffix.getValue();

            String fullDisplayString = showSuffix ? name + " " + suffix : name;
            float width = mc.font.width(fullDisplayString);

            float x = event.scaledWidth - width - 5;

            long time = System.currentTimeMillis();

            Color color = ColorUtils.getClientTheme(i);

            boolean animatedTheme = false;

            if (!animatedTheme) {
                Color start = color;
                Color end = start.darker().darker();

                switch (InterfaceModule.INSTANCE.colorMode.getValue()) {
                    case Wave -> {
                        float wave = (float) ((Math.sin((time / 350.0) + (i * 0.30)) + 1.0) / 2.0);
                        color = DrawUtil.interpolate(start, end, wave);
                    }
                    case Pulse -> {
                        float pulse = (float) ((Math.sin(time / 350.0) + 1.0) / 2.0);
                        color = DrawUtil.interpolate(start, end, pulse);
                    }
                    default -> color = start;
                }
            }

            int padding = 1;
            int fontHeight = mc.font.lineHeight;

            int rectX1 = (int) x - padding;
            int rectY1 = y - padding;
            int rectX2 = (int) (x + width) + padding;
            int rectY2 = y + fontHeight + padding;

            if (parent.background.getValue()) {
                event.context.fill(rectX1, rectY1, rectX2, rectY2, new Color(0, 0, 0, 120).getRGB());
            }
            if (parent.bar.getValue()) {
                event.context.fill(rectX2, rectY1, rectX2 + 1, rectY2, color.getRGB());
            }

            if (hasSuffix && parent.suffix.getValue()) {
                event.context.drawString(mc.font, name + " ", (int) x, y, color.getRGB(), true);
                float nameWidth = mc.font.width(name + " ");
                event.context.drawString(mc.font, suffix, (int) (x + nameWidth), y, Color.GRAY.getRGB(), true);
            } else {
                event.context.drawString(mc.font, name, (int) x, y, color.getRGB(), true);
            }

            y += fontHeight + 2;
        }
    }
}