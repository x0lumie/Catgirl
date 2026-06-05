package lol.catgirl.utils.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jspecify.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class FileUtils {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static void saveString(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    public static String loadString(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public static <T> void saveJson(Path path, T object) throws IOException {
        String json = GSON.toJson(object);
        saveString(path, json);
    }

    public static <T> T loadJson(Path path, Class<T> clazz) throws IOException {
        String json = loadString(path);
        return GSON.fromJson(json, clazz);
    }

    public static @Nullable File openFile(String title, @Nullable String defaultPathAndFile) {
        final var open = TinyFileDialogs.tinyfd_openFileDialog(
                title, defaultPathAndFile,
                null, null,
                false
        );
        if (open == null) return null;
        return Paths.get(open).toFile();
    }

    public static File @Nullable [] openFiles(String title, String defaultPathAndFile, String singleFilterDescription) {
        final var open = TinyFileDialogs.tinyfd_openFileDialog(
                title, defaultPathAndFile,
                null, null,
                true
        );
        if (open == null) return new File[0];
        return Arrays.stream(open.split("\\|")).map(p -> Paths.get(open).toFile()).toArray(File[]::new);
    }
}
