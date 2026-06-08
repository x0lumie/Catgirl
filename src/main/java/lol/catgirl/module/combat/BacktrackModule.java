package lol.catgirl.module.combat;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.event.impl.PostMotionEvent;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.manager.LagManager;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.TargetsModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.MathUtils;
import lol.catgirl.utils.render.RenderUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BacktrackModule extends Module {

    public static SliderProperty minDelayProperty = new SliderProperty("Min Delay", 50.0f, 0.0f, 5000.0f, 10.0f);
    public static SliderProperty maxDelayProperty = new SliderProperty("Max Delay", 200.0f, 0.0f, 5000.0f, 10.0f);
    public static SliderProperty activateDist = new SliderProperty("Activate Distance", 0.0f, 0.0f, 10.0f, 0.1f);
    public static SliderProperty deactivateDist = new SliderProperty("Deactivate Distance", 10.0f, 0.0f, 10.0f, 0.1f);
    public static EnumProperty<Mode> modeProperty = new EnumProperty<>("Mode", Mode.Constant);
    public BoolProperty cancelClientPacketsProperty = new BoolProperty("Cancel Client Packets", true);
    public BoolProperty swingCheckProperty = new BoolProperty("Swing Check", true);
    public BoolProperty releaseOnDamageProperty = new BoolProperty("Release On Damage", true);

    public enum Mode {
        Constant,
        Hit,
        Zero
    }

    public static final BacktrackModule INSTANCE = new BacktrackModule();

    public Player target;
    private Player lastTarget;
    public Vec3 realPosition = null;
    private int ping;

    public BacktrackModule() {
        super("Backtrack", "Uses lag to gain a reach advantage using a delay to the target's position.", ModuleCategory.Combat);
        addSettings(minDelayProperty, maxDelayProperty, activateDist, deactivateDist, modeProperty, cancelClientPacketsProperty, swingCheckProperty, releaseOnDamageProperty);
    }

    @Override
    public void onDisable() {
        LagManager.dispatch();
        LagManager.disable();
        target = null;
        lastTarget = null;
        realPosition = null;
    }

    @EventHook
    public void onPostMotion(PostMotionEvent event) {
        setSuffix(ping + " ms");
        if (mc.player.isDeadOrDying()) {
            LagManager.dispatch();
            LagManager.disable();
            return;
        }

        if (!(TargetsModule.getTarget() instanceof Player p)) {
            LagManager.dispatch();
            LagManager.disable();
            target = null;
            lastTarget = null;
            realPosition = null;
            return;
        }

        target = p;

        // Reset realPosition when target changes
        if (target != lastTarget) {
            realPosition = target.position();
            lastTarget = target;
        }

        if (swingCheckProperty.getValue() && !mc.player.swinging)
            return;

        // Clamp min/max
        if (maxDelayProperty.getValue() < minDelayProperty.getValue())
            maxDelayProperty.setValue(minDelayProperty.getValue());
        if (minDelayProperty.getValue() > maxDelayProperty.getValue())
            minDelayProperty.setValue(maxDelayProperty.getValue());

        double realDist = realPosition.distanceTo(mc.player.getEyePosition());
        boolean inRange = realDist >= activateDist.getValue() && realDist <= deactivateDist.getValue();
        boolean damageOk = !releaseOnDamageProperty.getValue() || mc.player.hurtTime == 0;

        if (inRange && shouldActive(target) && damageOk) {
            ping = (int) MathUtils.randomInt(minDelayProperty.getValue().intValue(), maxDelayProperty.getValue().intValue());
            LagManager.spoof(ping, true, true, true, true,
                    cancelClientPacketsProperty.getValue(),
                    cancelClientPacketsProperty.getValue());
        } else {
            LagManager.dispatch();
            LagManager.disable();
        }
    }

    @EventHook
    public void onPacketReceived(PacketReceivedEvent event) {
        final Packet<?> packet = event.packet;

        if (target == null || realPosition == null) {
            return;
        }

        if (packet instanceof ClientboundMoveEntityPacket move) {
            if (move.getEntity(mc.level) != null && move.getEntity(mc.level).getId() == target.getId()) {
                realPosition = new Vec3(
                        realPosition.x + move.getXa() / 4096D,
                        realPosition.y + move.getYa() / 4096D,
                        realPosition.z + move.getZa() / 4096D
                );
            }
        } else if (packet instanceof ClientboundTeleportEntityPacket teleport) {
            if (teleport.id() == target.getId()) {
                Vec3 pos = teleport.change().position();
                realPosition = new Vec3(pos.x, pos.y, pos.z);
            }
        }
    }

    @EventHook
    public void onRender3DEvent(Render3DEvent event) {
        if (target == null || realPosition == null) return;

        AABB box = target.getBoundingBox();
        float offMinX = (float)(box.minX - target.getX()) - 0.12f;
        float offMaxX = (float)(box.maxX - target.getX()) + 0.12f;
        float offMinY = (float)(box.minY - target.getY()) - 0.12f;
        float offMaxY = (float)(box.maxY - target.getY()) + 0.12f;
        float offMinZ = (float)(box.minZ - target.getZ()) - 0.12f;
        float offMaxZ = (float)(box.maxZ - target.getZ()) + 0.12f;

        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(realPosition.x - camX, realPosition.y - camY, realPosition.z - camZ);

        Matrix4f mat = ms.last().pose();
        BufferBuilder vb = RenderUtils.beginQuads();

        RenderUtils.fillBox(vb, mat, offMinX, offMaxX, offMinY, offMaxY, offMinZ, offMaxZ,
                1f, 0f, 0f, 80f / 255f);

        RenderUtils.LAYER_QUADS.draw(vb.buildOrThrow());

        ms.popPose();
    }

    public boolean shouldActive(Player target) {
        return switch (modeProperty.getValue()) {
            case Constant -> true;
            case Hit -> target.hurtTime != 0;
            case Zero -> target.hurtTime == 0;
        };
    }
}