package lol.catgirl.event.impl;

import lol.catgirl.event.Event;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerUseMultiplierEvent extends Event {
    public float forward, sideways;
}
