package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChatEvent extends Event {
    public String context;
}
