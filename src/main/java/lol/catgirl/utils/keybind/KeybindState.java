package lol.catgirl.utils.keybind;

import java.util.HashMap;
import java.util.Map;

public class KeybindState {

    private static final Map<String, Boolean> keyStates = new HashMap<>();

    public static boolean wasPressed(String id) {
        return keyStates.getOrDefault(id, false);
    } 

    public static void setPressed(String keybindId, boolean pressed) {
        keyStates.put(keybindId, pressed);
    } 

    public static void clearAll() {
        keyStates.clear();
    }
}
