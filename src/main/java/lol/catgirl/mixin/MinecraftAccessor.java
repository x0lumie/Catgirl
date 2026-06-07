package lol.catgirl.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Accessor("mouseHandler")
    MouseHandler getMouseHandler();

    @Invoker("startUseItem")
    void invokeStartUseItem();

    @Invoker("attack")
    boolean invokeAttack();
}