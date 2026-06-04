package lol.catgirl.file.impl;

import com.google.gson.JsonObject;
import lol.catgirl.file.AbstractBaseFile;
import lol.catgirl.manager.ModuleManager;
import lol.catgirl.module.Module;

public class ModulesFile extends AbstractBaseFile<JsonObject> {

    public static final ModulesFile DEFAULT = new ModulesFile("default.json");

    public ModulesFile(String name) {
        super("configs/" + name, JsonObject.class, JsonObject::new);
    }

    @Override
    protected void load(JsonObject in) {
        for (Module module : ModuleManager.modules) {
            if (in.has(module.getName())) {
                module.fromJson(in.get(module.getName()));
            }
        }
    }

    @Override
    protected JsonObject save() {
        JsonObject object = new JsonObject();

        for (Module module : ModuleManager.modules) {
            object.add(module.getName(), module.toJson());
        }

        return object;
    }
}