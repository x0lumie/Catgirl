package lol.catgirl.utils.keybind;

public interface Keybindable {
    int getKey();
    void onBindPress();
    String getKeybindId();
}
