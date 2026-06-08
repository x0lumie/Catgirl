package lol.catgirl.utils.client;

import com.mojang.math.Axis;
import lol.catgirl.module.combat.AuraModule;
import lol.catgirl.module.render.AnimationsModule;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

import static lol.catgirl.utils.IMinecraft.mc;

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
                if (!ItemAnimationUtils.isBlocking()) return;
                matrices.translate(0.1, 0, -0.1);
                matrices.mulPose(Axis.XP.rotationDegrees(-sine * 50));
                matrices.mulPose(Axis.YP.rotationDegrees(-sine * 30));
            }

            case Vanilla -> {
                if (!ItemAnimationUtils.isBlocking()) return;
                matrices.translate(0.1, 0, -0.1);
                matrices.mulPose(Axis.YP.rotationDegrees(45.0f + f * -20.0f));
                matrices.mulPose(Axis.ZP.rotationDegrees(sine * -20.0f));
                matrices.mulPose(Axis.XP.rotationDegrees(sine * -80.0f));
                matrices.mulPose(Axis.YP.rotationDegrees(-45.0f));
            }

            case Stab -> {
                if (!ItemAnimationUtils.isBlocking()) return;
                float stab = (float) Math.sin(Mth.sqrt(swingProgress) * Math.PI);

                matrices.translate(0.1, 0.05, -0.8f * stab);
                matrices.mulPose(Axis.XP.rotationDegrees(-70.0f - (f * 10.0f)));
                matrices.mulPose(Axis.YP.rotationDegrees(stab * 15.0f));
            }

            case Spin -> {
                if (!ItemAnimationUtils.isBlocking()) return;
                float spin = -(System.currentTimeMillis() / 2 % 360);
                matrices.translate(-0.1, 0, -0.2);
                matrices.mulPose(Axis.ZP.rotationDegrees(spin));
            }
            case Lumie -> {
                float g = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);

                if (!ItemAnimationUtils.isBlocking()) {
                    matrices.mulPose(Axis.XP.rotationDegrees(50f));
                    matrices.mulPose(Axis.YP.rotationDegrees(-60f));
                    matrices.mulPose(Axis.ZP.rotationDegrees(110f + 20f * g));
                } else {
                    matrices.translate(0.1, 0, -0.1);
                    matrices.mulPose(Axis.XP.rotationDegrees(-sine * 50));
                    matrices.mulPose(Axis.YP.rotationDegrees(-sine * 30));
                    matrices.mulPose(Axis.ZP.rotationDegrees(-sine * 10));
                }
            }
        }
    }
}
