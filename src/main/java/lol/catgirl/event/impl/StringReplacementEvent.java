package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StringReplacementEvent extends Event {
    public String text;
}
