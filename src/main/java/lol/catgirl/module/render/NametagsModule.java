package lol.catgirl.module.render;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.RenderTickEvent;
import lol.catgirl.event.impl.RenderWorldEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.combat.VelocityModule;
import lol.catgirl.module.combat.velocity.VelocityMode;
import lol.catgirl.module.combat.velocity.impl.CancelVelocityMode;
import lol.catgirl.module.combat.velocity.impl.JumpResetVelocityMode;
import lol.catgirl.module.combat.velocity.impl.MatrixSprintVelocityMode;
import lol.catgirl.module.combat.velocity.impl.MatrixVelocityMode;
import lol.catgirl.module.render.nametags.NametagsMode;
import lol.catgirl.module.render.nametags.impl.CatgirlNametagsMode;
import lol.catgirl.module.render.nametags.impl.MinecraftNametagsMode;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;

import java.util.EnumMap;
import java.util.Map;

public final class NametagsModule extends Module {
    public static final NametagsModule INSTANCE = new NametagsModule();

    public enum Mode {
        Catgirl,
        Minecraft
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Catgirl);
    public final BoolProperty scaleByDistance = new BoolProperty("Scale Distance", false).hide(()-> !(mode.getValue() == Mode.Minecraft));


    public NametagsModule() {
        super("NameTags", "Custom player nametags.", ModuleCategory.Render);
        addSettings(mode, scaleByDistance);
    }

    private final Map<Mode, NametagsMode> nametagMode;

    {
        nametagMode = new EnumMap<>(Mode.class);

        nametagMode.put(Mode.Catgirl, new CatgirlNametagsMode());
        nametagMode.put(Mode.Minecraft, new MinecraftNametagsMode(this));

    }

    @EventHook
    public void onRenderTick(RenderTickEvent event) {
        NametagsMode currentMode = nametagMode.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onRenderTick(event);
        }
    }

    @EventHook
    public void onRenderWorld(RenderWorldEvent event) {
        NametagsMode currentMode = nametagMode.get(mode.getValue());
        if (currentMode != null) {
            currentMode.onRenderWorld(event);
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
