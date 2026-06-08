package lol.catgirl.module.ghost;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PlayerInteractionRangeEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;

public class ReachModule extends Module {
    public static final ReachModule INSTANCE = new ReachModule();

    public final SliderProperty reachSlider = new SliderProperty("Distance", 1.0f, 3.0f, 6.0f, 0.1f);
    public final BoolProperty blockReach = new BoolProperty("Block Reach", false);

    public ReachModule() {
        super("Reach", "Allows you to reach further.", ModuleCategory.Ghost);
    }

    public float getAmount() {
        return (float) Math.round(reachSlider.getValue() * 10) / 10f;
    }

    @EventHook
    public void onReachEntity(PlayerInteractionRangeEvent.Entity event) {
        if (!this.isEnabled()) return;

        event.setReach(getAmount());
        event.setCancelled(true);
    }

    @EventHook
    public void onReachBlock(PlayerInteractionRangeEvent.Block event) {
        if (!this.isEnabled()) return;

        if (!blockReach.getValue()) {
            return;
        }

        event.setReach(getAmount());
        event.setCancelled(true);
    }
}
