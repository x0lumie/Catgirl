package lol.catgirl.event.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lol.catgirl.event.Event;
import lombok.AllArgsConstructor;
import net.minecraft.world.InteractionHand;

@AllArgsConstructor
public class ItemRendererEvent extends Event {
    public final InteractionHand hand;
    public final PoseStack poseStack;


}
