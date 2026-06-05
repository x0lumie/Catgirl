package lol.catgirl.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.ItemRendererEvent;
import lol.catgirl.module.render.AnimationsModule;
import lol.catgirl.utils.client.ItemAnimationUtils;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void hookRender(AbstractClientPlayer player,
                            float partialTicks,
                            float pitch,
                            InteractionHand hand,
                            float swingProgress,
                            ItemStack stack,
                            float equipProgress,
                            PoseStack poseStack,
                            SubmitNodeCollector collector,
                            int light,
                            CallbackInfo ci) {

        if (hand == InteractionHand.OFF_HAND && ItemAnimationUtils.isBlocking()) {
            ci.cancel();
            return;
        }

        ItemStack spoofed = ItemAnimationUtils.getSpoofedItem();

        if (hand == InteractionHand.MAIN_HAND
                && spoofed != null
                && !ItemStack.matches(spoofed, stack)) {

            ci.cancel();

            ((ItemInHandRendererInvoker) (Object) this)
                    .invokeRenderArmWithItem(
                            player,
                            partialTicks,
                            pitch,
                            hand,
                            swingProgress,
                            spoofed,
                            equipProgress,
                            poseStack,
                            collector,
                            light
                    );
        }
    }

    @Redirect(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z"
            )
    )
    private boolean hookUsingItem(AbstractClientPlayer player) {
        var stack = player.getMainHandItem();

        if (ItemAnimationUtils.isBlocking() && stack.is(ItemTags.SWORDS)) {
            return true;
        }

        return player.isUsingItem();
    }

    @Redirect(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"
            )
    )
    private InteractionHand hookActiveHand(AbstractClientPlayer player) {
        var stack = player.getMainHandItem();

        if (ItemAnimationUtils.isBlocking() && stack.is(ItemTags.SWORDS)) {
            return InteractionHand.MAIN_HAND;
        }

        return player.getUsedItemHand();
    }

    @Redirect(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUseItemRemainingTicks()I"
            )
    )
    private int hookUseTime(AbstractClientPlayer player) {
        var stack = player.getMainHandItem();

        if (ItemAnimationUtils.getSpoofedItem() != null
                || (ItemAnimationUtils.isBlocking()
                && stack.getUseAnimation() == ItemUseAnimation.BLOCK)) {
            return 7200;
        }

        return player.getUseItemRemainingTicks();
    }
    @Redirect(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;"
            )
    )
    private ItemUseAnimation hookUseAnimation(ItemStack stack) {
        if (ItemAnimationUtils.isBlocking() && stack.is(ItemTags.SWORDS)) {
            return ItemUseAnimation.BLOCK;
        }

        return stack.getUseAnimation();
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
                    shift = At.Shift.AFTER
            )
    )
    private void injectAnimation(AbstractClientPlayer player,
                                 float partialTicks,
                                 float pitch,
                                 InteractionHand hand,
                                 float swingProgress,
                                 ItemStack stack,
                                 float equipProgress,
                                 PoseStack poseStack,
                                 SubmitNodeCollector collector,
                                 int light,
                                 CallbackInfo ci) {

        AnimationsModule mod = AnimationsModule.INSTANCE;
        if (mod == null || !mod.isEnabled()) return;

        float swing = player.getAttackAnim(partialTicks);
        float smooth = Mth.sqrt(swing);

        float sin1 = Mth.sin(swing * swing * (float) Math.PI);
        float sin2 = Mth.sin(smooth * (float) Math.PI);

        float intensity = sin1 * 0.6F + sin2 * 0.4F;

        ItemAnimationUtils.animate(poseStack, swing, intensity);

//        poseStack.translate(0.0, ItemAnimationUtil.height, 0.0);
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void postItemRendererEvent(
            AbstractClientPlayer player,
            float partialTicks, float pitch,
            InteractionHand hand, float swingProgress, ItemStack stack,
            float equipProgress, PoseStack poseStack,
            SubmitNodeCollector collector, int light, CallbackInfo ci) {

        Catgirl.INSTANCE.eventBus.post(new ItemRendererEvent(hand, poseStack));
    }

}