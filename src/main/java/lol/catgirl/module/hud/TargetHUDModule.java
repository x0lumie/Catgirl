package lol.catgirl.module.hud;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.Render2DEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.client.TargetsModule;
import lol.catgirl.module.combat.velocity.VelocityMode;
import lol.catgirl.module.combat.velocity.impl.*;
import lol.catgirl.module.hud.targethud.TargetHUDMode;
import lol.catgirl.module.hud.targethud.impl.*;
import lol.catgirl.property.impl.EnumProperty;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

public final class TargetHUDModule extends Module {
    public static final TargetHUDModule INSTANCE = new TargetHUDModule();

    public enum Mode {
        Exhibition,
        Astolfo,
        Novoline
    }

    public float x = 10f, y = 50f;

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Exhibition);

    public TargetHUDModule() {
        super("TargetHUD", "Shows a hud with target data on it.", ModuleCategory.Hud);
        addSettings(mode);
    }

    private float dragX, dragY;
    private boolean dragging;

    public void dragging() {
        if (mc.screen instanceof ChatScreen) {

            double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().
                    getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().
                    getGuiScaledHeight() / (double) mc.getWindow().getHeight();

            boolean isMouseDown = GLFW.glfwGetMouseButton(mc.getWindow().handle(),
                    org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) ==
                    org.lwjgl.glfw.GLFW.GLFW_PRESS;

            if (isMouseDown) {
                if (!dragging) {
                    float hudWidth = 155f;
                    float hudHeight = 60f;

                    if (mouseX >= x && mouseX <= x + hudWidth && mouseY >= y && mouseY <= y + hudHeight) {
                        dragging = true;
                        dragX = (float) (mouseX - x);
                        dragY = (float) (mouseY - y);
                    }
                } else {
                    x = (float) (mouseX - dragX);
                    y = (float) (mouseY - dragY);
                }
            } else {
                dragging = false;
            }
        } else {
            dragging = false;
        }
    }

    private LivingEntity target;


    private final Map<Mode, TargetHUDMode> targetHUDModes;

    {
        targetHUDModes = new EnumMap<>(Mode.class);

        targetHUDModes.put(Mode.Exhibition, new ExhibitionTargetHUDMode(this));
        targetHUDModes.put(Mode.Astolfo, new AstolfoTargetHUDMode(this));
        targetHUDModes.put(Mode.Novoline, new NovolineTargetHUDMode(this));

    }

    @EventHook
    public void onRender(Render2DEvent event) {
        if(mc.player == null) return;

        dragging();

        if (mc.screen instanceof ChatScreen) {
            target = mc.player;
        } else {
            target = TargetsModule.getTarget();
        }

        if (!(target instanceof Player)) {
            return;
        }

        if (target == null) return;

        TargetHUDMode currentMode = targetHUDModes.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onRender(event, target);
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
