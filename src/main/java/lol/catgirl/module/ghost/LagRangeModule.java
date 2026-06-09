package lol.catgirl.module.ghost;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PostMotionEvent;
import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.manager.BlinkManager;
import lol.catgirl.manager.LagManager;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.TargetsModule;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.MathUtils;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.render.RenderUtils;
import lol.catgirl.utils.render.nanovg.DrawUtil;
import lol.catgirl.utils.render.nanovg.ResourceManager;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.awt.*;

public class LagRangeModule extends Module {

    public enum Mode {
        Lag,
        Blink
    }

    // so if your confused basically it gets the max range of targets in targets module and
    // enables blink within that range and disables after you reach <= rangeProperty.

    public EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Lag);
    public SliderProperty rangeProperty = new SliderProperty("Disable Range", 3.5f, 0.1f, 6, 0.05f);
    public SliderProperty minDelayProperty = new SliderProperty("Min Delay", 50.0f, 0.0f, 5000.0f, 10.0f).hide(() -> mode.getValue() == Mode.Blink);
    public SliderProperty maxDelayProperty = new SliderProperty("Max Delay", 200.0f, 0.0f, 5000.0f, 10.0f).hide(() -> mode.getValue() == Mode.Blink);
    private final BoolProperty teleports = new BoolProperty("Delay Teleports", true).hide(() -> mode.getValue() == Mode.Blink);
    private final BoolProperty velocity = new BoolProperty("Delay Velocity", true).hide(() -> mode.getValue() == Mode.Blink);
    private final BoolProperty entities = new BoolProperty("Delay Entity Movements", true).hide(() -> mode.getValue() == Mode.Blink);
    public final BoolProperty displayProperty = new BoolProperty("Display", true);
    public final BoolProperty onlyWithKillaura = new BoolProperty("Only With Killaura", true);
    public final BoolProperty renderLagPos = new BoolProperty("Render Lag Pos", true).hide(() -> mode.getValue() == Mode.Blink);

    private boolean lagging = false;
    private int lagAmount = maxDelayProperty.getValue().intValue();

    public static final LagRangeModule INSTANCE = new LagRangeModule();

    public LagRangeModule() {
        super("LagRange", "Uses lag in a range to the target to delay your movement on their end.", ModuleCategory.Ghost);
        addSettings(mode, rangeProperty, minDelayProperty, maxDelayProperty, teleports, velocity, entities, displayProperty, onlyWithKillaura, renderLagPos);
    }

    @EventHook
    public void onPostMotion(PostMotionEvent event) {
        if (onlyWithKillaura.getValue() && !ModuleManager.getModule("Aura").isEnabled()) {
            if (lagging) disableLag();
            return;
        }

        if (TargetsModule.getTarget() == null) {
            if (lagging) disableLag();
            return;
        }

        if (!MoveUtils.isMoving()) {
            if (lagging) disableLag();
            return;
        }

        double distance = mc.player.distanceTo(TargetsModule.getTarget());

        if (distance <= TargetsModule.seekRange.getValue() && distance > rangeProperty.getValue()) {
            if (mode.getValue() ==  Mode.Lag) {
                lagAmount = (int) MathUtils.randomInt(minDelayProperty.getValue().intValue(), maxDelayProperty.getValue().intValue());
                LagManager.spoof(lagAmount, true, velocity.getValue(),
                        teleports.getValue(), entities.getValue());
            } else {
                BlinkManager.enable();
            }
            lagging = true;
        } else {
            if (lagging) {
                disableLag();
            }
            return;
        }

        setSuffix(String.format("%.1f", rangeProperty.getValue()));
    }

    @EventHook
    public void onRender2DEvent(Render2DEvent event) {
        if (!displayProperty.getValue() || !lagging) return;

        String text = mode.getValue() == Mode.Lag ? String.format("Lagging %dms", lagAmount) : "Blinking";
        int textWidth = mc.font.width(text);
        int x = (event.scaledWidth - textWidth) / 2;
        int y = 10;

        event.context.drawString(mc.font, text, x, y, Color.YELLOW.getRGB());
    }

    @EventHook
    public void onRender3DEvent(Render3DEvent event) {
        if (!renderLagPos.getValue() || !lagging || mode.getValue() == Mode.Blink) return;
        if (!LagManager.hasServerPosition) return;
        if (mc.options.getCameraType().isFirstPerson()) return;

        double camX = mc.gameRenderer.getMainCamera().position().x;
        double camY = mc.gameRenderer.getMainCamera().position().y;
        double camZ = mc.gameRenderer.getMainCamera().position().z;

        AABB box = mc.player.getBoundingBox();
        float hw = (float)((box.maxX - box.minX) / 2.0 + 0.3);
        float h  = (float)( box.maxY - box.minY        + 0.3);

        PoseStack ms = event.matrixStack;
        ms.pushPose();
        ms.translate(LagManager.serverX - camX, LagManager.serverY - camY, LagManager.serverZ - camZ);

        Matrix4f mat = ms.last().pose();
        BufferBuilder vb = RenderUtils.beginQuads();

        RenderUtils.fillBox(vb, mat,
                -hw,  hw,
                0f,  h,
                -hw,  hw,
                1f, 0f, 0f, 80f / 255f);

        RenderUtils.LAYER_QUADS.draw(vb.buildOrThrow());
        ms.popPose();
    }

    private void disableLag() {
        if (mode.getValue() == Mode.Lag) {
            LagManager.dispatch();
            LagManager.disable();
        } else {
            BlinkManager.disable();
        }
        lagging = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (lagging) {
            disableLag();
        }
    }

}
