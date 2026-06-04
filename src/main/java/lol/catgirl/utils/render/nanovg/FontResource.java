package lol.catgirl.utils.render.nanovg;

import lol.catgirl.Catgirl;
import org.lwjgl.nanovg.NanoVG;

import java.io.IOException;
import java.nio.ByteBuffer;

public class FontResource {

    private final ByteBuffer resource;
    public final String identifier;

    public FontResource(String fontName) throws IOException {
        this.identifier = fontName;

        this.resource = MiscUtil.getResourceAsByteBuffer("fonts/" + fontName + ".ttf");

        int handle = NanoVG.nvgCreateFontMem(
                DrawUtil.context,
                identifier,
                resource,
                true
        );

        if (handle == -1) {
            Catgirl.LOGGER.error("CRITICAL ERROR: Failed to load font: " + identifier);
        } else {
            Catgirl.LOGGER.info("Successfully loaded font: " + identifier);
        }
    }
}