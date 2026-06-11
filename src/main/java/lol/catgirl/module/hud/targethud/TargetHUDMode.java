package lol.catgirl.module.hud.targethud;

import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.utils.IMinecraft;
import net.minecraft.world.entity.LivingEntity;

public interface TargetHUDMode extends IMinecraft {
    default void onRender(Render2DEvent event, LivingEntity target) {}

}
