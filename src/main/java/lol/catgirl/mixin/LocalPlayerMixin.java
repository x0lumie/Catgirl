package lol.catgirl.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import lol.catgirl.Catgirl;
import lol.catgirl.event.impl.*;
import lol.catgirl.module.movement.MovementFixModule;
import lol.catgirl.module.player.NoSlowModule;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends
        AbstractClientPlayer implements IMinecraft {

    public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow protected abstract void sendIsSprintingIfNeeded();

    @Shadow protected abstract boolean isControlledCamera();

    @Shadow private double xLast;

    @Shadow private double yLast;

    @Shadow private double zLast;

    @Shadow private float yRotLast;

    @Shadow private float xRotLast;

    @Shadow public abstract boolean isShiftKeyDown();

    @Shadow private int positionReminder;

    @Shadow @Final public ClientPacketListener connection;

    @Shadow private boolean lastOnGround;

    @Shadow private boolean lastHorizontalCollision;

    @Shadow private boolean autoJumpEnabled;

    @Shadow @Final protected Minecraft minecraft;

    @Shadow public abstract boolean isUsingItem();

    @Shadow public abstract boolean isUnderWater();


    @Inject(
            at = @At(value = "HEAD"),
            method = "tick"
    )
    public void start(CallbackInfo ci) {
        RotationUtils.setCamYaw(mc.player.getYRot());
        RotationUtils.setCamPitch(mc.player.getXRot());

        RotationUtils.setLastRotationYaw(RotationUtils.getRotationYaw());
        RotationUtils.setLastRotationPitch(RotationUtils.getRotationPitch());

        PlayerRotationEvent rotationEvent = new PlayerRotationEvent(
                this.getYRot(),
                this.getXRot()
        );

        Catgirl.INSTANCE.eventBus.post(rotationEvent);

        RotationUtils.yawChanged = rotationEvent.yaw != this.getYRot();
        RotationUtils.pitchChanged = rotationEvent.pitch != this.getXRot();

        RotationUtils.setRotationYaw(rotationEvent.yaw);
        RotationUtils.setRotationPitch(rotationEvent.pitch);


        MovementFixModule movementFixModule = MovementFixModule.INSTANCE;
        if (movementFixModule != null && movementFixModule.isEnabled()) {
            mc.player.setYRot(RotationUtils.getRotationYaw());
            mc.player.setXRot(RotationUtils.getRotationPitch());
        }
    }

    @Inject(
            at = @At(value = "TAIL"),
            method = "tick"
    )

    public void end(CallbackInfo ci) {
        MovementFixModule movementFixModule = MovementFixModule.INSTANCE;

        if (movementFixModule != null && movementFixModule.isEnabled()) {
            mc.player.setYRot(RotationUtils.getCamYaw());
            mc.player.setXRot(RotationUtils.getCamPitch());
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V"), method = "tick")
    public void onPreUpdate(CallbackInfo ci) {

        Catgirl.INSTANCE.eventBus.post(new PreUpdateEvent());
    }

    /**
     * @author
     * @reason
     */

    @Overwrite
    private void sendPosition() {
        PreMotionEvent event = new PreMotionEvent(
                this.getX(),
                this.getBoundingBox().minY,
                this.getZ(),
                RotationUtils.getRotationYaw(),
                RotationUtils.getRotationPitch(),
                RotationUtils.getLastRotationYaw(),
                RotationUtils.getLastRotationPitch(),
                this.onGround(),
                this.isShiftKeyDown(),
                this.isSprinting(),
                this.horizontalCollision
        );

        this.sendIsSprintingIfNeeded();
        if (this.isControlledCamera()) {

            Catgirl.INSTANCE.eventBus.post(event);

            double d = event.posX - this.xLast;
            double e = event.posY - this.yLast;
            double f = event.posZ - this.zLast;
            double g = (double)(event.yaw - event.lastYaw);
            double h = (double)(event.pitch - event.lastPitch);
            ++this.positionReminder;
            boolean bl = Mth.lengthSquared(d, e, f) > Mth.square(2.0E-4) || this.positionReminder >= 20;
            boolean bl2 = g != 0.0 || h != 0.0;
            if (bl && bl2) {
                this.connection.send(new ServerboundMovePlayerPacket.PosRot(event.posX, event.posY, event.posZ, event.yaw, event.pitch, event.onGround, event.horizontalCollision));
            } else if (bl) {
                this.connection.send(new ServerboundMovePlayerPacket.Pos(event.posX, event.posY, event.posZ, event.onGround, event.horizontalCollision));
            } else if (bl2) {
                this.connection.send(new ServerboundMovePlayerPacket.Rot(event.yaw, event.pitch, event.onGround, event.horizontalCollision));
            } else if (this.lastOnGround != event.onGround ||  this.lastHorizontalCollision != event.horizontalCollision) {
                this.connection.send(new ServerboundMovePlayerPacket.StatusOnly(event.onGround, event.horizontalCollision));
            }

            if (bl) {
                this.xLast = event.posX;
                this.yLast = event.posY;
                this.zLast = event.posZ;
                this.positionReminder = 0;
            }

            if (bl2) {
                this.yRotLast = event.yaw;
                this.xRotLast = event.pitch;
            }

            this.lastOnGround = event.onGround;
            this.lastHorizontalCollision = event.horizontalCollision;
            this.autoJumpEnabled = (Boolean)this.minecraft.options.autoJump().get();
        }
        PostMotionEvent ery5r6u = new PostMotionEvent();
        Catgirl.INSTANCE.eventBus.post(ery5r6u);
    }

    @WrapOperation(method = "modifyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec2;scale(F)Lnet/minecraft/world/phys/Vec2;", ordinal = 1))
    private Vec2 hookCustomMultiplier(Vec2 instance, float value, Operation<Vec2> original) {
        final var event = new PlayerUseMultiplierEvent(value, value);
        Catgirl.INSTANCE.eventBus.post(event);
        if (event.isCancelled()) {
            return new Vec2(instance.x, instance.y);
        }
        return new Vec2(instance.x * event.sideways, instance.y * event.forward);
    }


    @ModifyExpressionValue(method = "isSlowDueToUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean hookSprintAffectStart(boolean original) {
        if (NoSlowModule.INSTANCE.isEnabled()) {
            return false;
        }

        return original;
    }

    @Inject(at = @At("HEAD"), method = "move", cancellable = true)
    private void moveHead(CallbackInfo ci) {
        PlayerMoveEvent event = new PlayerMoveEvent();

        Catgirl.INSTANCE.eventBus.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
