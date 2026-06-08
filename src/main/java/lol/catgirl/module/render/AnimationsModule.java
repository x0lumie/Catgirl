package lol.catgirl.module.render;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;

public class AnimationsModule extends Module {

    public static final AnimationsModule INSTANCE = new AnimationsModule();

    public AnimationsModule() {
        super("Animations",
                "Shows custom client-side item animations.",
                ModuleCategory.Render
        );
        addSettings(mode);
    }

    public enum AnimationsMode {
        Vanilla,
        Exhibition,
        Spin,
        Stab,
        Lumie,
        Lumie2
    }

    public final EnumProperty<AnimationsMode> mode =
            new EnumProperty<>("Mode", AnimationsMode.Vanilla);

    @Override
    public String getFinalSuffix() {
        return mode.getValue().toString();
    }
}
