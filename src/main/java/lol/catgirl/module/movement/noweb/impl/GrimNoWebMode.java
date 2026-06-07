package lol.catgirl.module.movement.noweb.impl;

import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.movement.noweb.NoWebMode;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;

public final class GrimNoWebMode implements NoWebMode {
    @Override
    public void onTick(PreUpdateEvent event) {
        if(mc.player == null) return;
        if(!PlayerUtils.isInWeb()) return;
        if(!MoveUtils.isMoving()) return;

        MoveUtils.setSpeedWithFixedDirection(0.644d);
    }
}
