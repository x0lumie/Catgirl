package lol.catgirl.manager;

import java.util.ArrayList;
import java.util.List;

import lol.catgirl.Catgirl;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.combat.AutoTotemModule;
import lol.catgirl.module.combat.RegenModule;
import lol.catgirl.module.combat.VelocityModule;
import lol.catgirl.module.movement.MovementFixModule;
import lol.catgirl.module.movement.SprintModule;
import lombok.Getter;

@Getter
public final class ModuleManager {
    @Getter
    private static final ModuleManager instance = new ModuleManager();

    public static ArrayList<Module> modules = new ArrayList<>();

    public void init() {
        modules.add(SprintModule.INSTANCE);
        modules.add(MovementFixModule.INSTANCE);
        modules.add(AutoTotemModule.INSTANCE);
        modules.add(RegenModule.INSTANCE);
        modules.add(VelocityModule.INSTANCE);

        Catgirl.LOGGER.info("Initializing " + modules.size() +  " Modules...");
    }

    public List<Module> getModulesByCategory(ModuleCategory moduleCategory) {
        List<Module> result = new ArrayList<>();
        for(Module module : modules) {
            if(module.getCategory() == moduleCategory) {
                result.add(module);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <M extends Module> M getModule(String module) {
        return (M) modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(module))
                .findFirst()
                .orElse(null);
    }

    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().trim().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }
}
