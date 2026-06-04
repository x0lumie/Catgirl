package lol.catgirl.file;

import com.google.gson.JsonElement;
import lol.catgirl.Catgirl;
import lol.catgirl.utils.IMinecraft;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public abstract class AbstractBaseFile<T extends JsonElement> implements IMinecraft {
    public static final Path BASE_DIRECTORY = Path.of(mc.gameDirectory + "/Catgirl");

    public final String name;
    public final Path path;
    private final Class<T> tClass;
    private final Supplier<T> factory;

    public AbstractBaseFile(String name, Class<T> tClass, Supplier<T> factory) {
        this.name = name.endsWith(".json") ? name : name + ".json";
        this.path = BASE_DIRECTORY.resolve(this.name);
        this.tClass = tClass;
        this.factory = factory;
    }

    protected abstract void load(T in);
    protected abstract T save();

/*    @SneakyThrows
    public T loadFromFile() {
        try {
            var loaded = FileUtil.loadJson(path, tClass);

            if (loaded == null) {
                loaded = factory.get();
            }

            load(loaded);
            return loaded;
        } catch (IOException e) {
            Catgirl.LOGGER.info("Failed to load {} the file.", name);
            return factory.get();
        }
    }*/

    public void saveToFile() {
        try {
            Files.createDirectories(path.getParent());
            // FileUtil.saveJson(path, save());
        } catch (IOException io) {
            io.printStackTrace();
            Catgirl.LOGGER.info("Failed to save {}", name);
        }
    }
}
