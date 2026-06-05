package lol.catgirl.module.render;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public class FullbrightModule extends Module {
    public static final FullbrightModule INSTANCE = new FullbrightModule();

    public FullbrightModule() {
        super("Fullbright",
                "Makes the game brighter.",
                ModuleCategory.Render
        );
    }

    @EventHook
    public void onTick(ClientTickEvent event) {

        if (mc.player != null) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        }
    }

    @Override
    public void onDisable() {

        if (mc.player != null) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}
