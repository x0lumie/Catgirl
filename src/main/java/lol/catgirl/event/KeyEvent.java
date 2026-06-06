package lol.catgirl.event;

import lol.catgirl.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KeyEvent extends Event {
    private final int key, scancode, action, modifiers;
}