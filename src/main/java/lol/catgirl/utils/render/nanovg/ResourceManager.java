package lol.catgirl.utils.render.nanovg;

import java.io.IOException;

public class ResourceManager {

    public static void init() throws IOException {
        FontResources.init();
    }

    public static class FontResources {
        public static FontResource regular;
        public static FontResource productSansBold, minecraft
                ;

        public static void init() throws IOException {
            regular = new FontResource("regular");
            productSansBold = new FontResource("ProductSans-Bold");
            minecraft = new FontResource("Mojangles");
        }
    }
}