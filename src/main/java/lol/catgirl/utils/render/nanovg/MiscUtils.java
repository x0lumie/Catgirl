package lol.catgirl.utils.render.nanovg;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class MiscUtils {

    public static ByteBuffer getResourceAsByteBuffer(String resource) throws IOException {
        return getResourceAsByteBuffer(resource, 1024);
    }

    public static ByteBuffer getResourceAsByteBuffer(String resource, int bufferSize) throws IOException {
        try (InputStream source = MiscUtils.class.getResourceAsStream("/assets/catgirl/" + resource)) {
            if (source == null) throw new IllegalArgumentException("Resource not found: " + resource);

            try (ReadableByteChannel rbc = Channels.newChannel(source)) {
                ByteBuffer buffer = BufferUtils.createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) break;

                    if (!buffer.hasRemaining()) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2);
                    }
                }

                buffer.flip();
                return buffer;
            }
        }
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
}