package lol.catgirl.module.render;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.EnumSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.EnumSet;

public class FullbrightModule extends Module {

    public static final FullbrightModule INSTANCE = new FullbrightModule();

    public enum Mode {
        Gamma,
        Effect
    }

    public final EnumSetting<Mode> mode = new EnumSetting<>("Mode", Mode.Effect);

    private double oldGamma = 1.0;

    public FullbrightModule() {
        super("Fullbright", "Allows you to see darker areas better.", ModuleCategory.Render);
    }

    @Override
    public void onEnable() {

        if (mode.getValue() == Mode.Gamma) {
            oldGamma = mc.options.gamma().get();
            mc.options.gamma().set(16.0);
        }

        if (mode.getValue() == Mode.Effect) {
            if (mc.player != null) {
                mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, false, false));
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null && mode.getValue() == Mode.Gamma) {
            mc.options.gamma().set(oldGamma);
        }

        if (mc.player != null && mode.getValue() == Mode.Effect) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}