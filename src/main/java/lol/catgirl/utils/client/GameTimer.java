package lol.catgirl.utils.client;

import lombok.Getter;
import lombok.Setter;

// Controls the Minecraft Timer.

@Setter
@Getter
public class GameTimer {
    @Getter
    @Setter
    private static float speed = 1.0f;

    public static void reset() {
        speed = 1.0f;
    }
}
