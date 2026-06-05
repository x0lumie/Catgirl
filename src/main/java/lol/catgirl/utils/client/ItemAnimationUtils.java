package lol.catgirl.utils.client;

import com.mojang.math.Axis;
import lol.catgirl.module.combat.AuraModule;
import lol.catgirl.module.render.AnimationsModule;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

public class ItemAnimationUtils {

    @Setter
    @Getter
    public static boolean isBlocking;

    @Setter
    @Getter
    public static ItemStack spoofedItem;

    public static float height = -0.1f;

    public static void animate(PoseStack matrices, float swingProgress, float f) {
        float sine = (float) Math.sin(Mth.sqrt(swingProgress) * Math.PI);

        var animationsModule = AnimationsModule.INSTANCE;

        switch (animationsModule.mode.getValue()) {

            case Exhibition -> {
                matrices.translate(0.1, 0, -0.1);
                matrices.mulPose(Axis.XP.rotationDegrees(-sine * 50));
                matrices.mulPose(Axis.YP.rotationDegrees(-sine * 30));
            }

            case Vanilla -> {
                matrices.translate(0.1, 0, -0.1);
                matrices.mulPose(Axis.YP.rotationDegrees(45.0f + f * -20.0f));
                matrices.mulPose(Axis.ZP.rotationDegrees(sine * -20.0f));
                matrices.mulPose(Axis.XP.rotationDegrees(sine * -80.0f));
                matrices.mulPose(Axis.YP.rotationDegrees(-45.0f));
            }

            case Stab -> {
                if(AuraModule.target == null) return;

                float stab = (float) Math.sin(Mth.sqrt(swingProgress) * Math.PI);

                matrices.translate(0.1, 0.05, -0.8f * stab);
                matrices.mulPose(Axis.XP.rotationDegrees(-70.0f - (f * 10.0f)));
                matrices.mulPose(Axis.YP.rotationDegrees(stab * 15.0f));
            }

            case Spin -> {
                float spin = -(System.currentTimeMillis() / 2 % 360);
                matrices.translate(-0.1, 0, -0.2);
                matrices.mulPose(Axis.ZP.rotationDegrees(spin));
            }
        }
    }
}
