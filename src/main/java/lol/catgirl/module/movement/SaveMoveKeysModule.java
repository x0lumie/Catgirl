package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.SliderProperty;
import net.minecraft.client.KeyMapping;
import lol.catgirl.module.Module;

public class SaveMoveKeysModule extends Module {
    public static final SaveMoveKeysModule INSTANCE = new SaveMoveKeysModule();

    private final SliderProperty minDelay = new SliderProperty("Min Delay", 0f, 0f, 500f, 1f);
    private final SliderProperty maxDelay = new SliderProperty("Max Delay", 0f, 0f, 500f, 1f);

    private boolean lastInScreen;
    private boolean toPress;
    private long startTime;
    private long delay;

    public SaveMoveKeysModule() {
        super("SaveMoveKeys",
                "Re-press move keys after close screen",
                ModuleCategory.Movement
        );
        addSettings(minDelay, maxDelay);
    }

    @Override
    public void onEnable() {
        lastInScreen = mc.screen != null;
        toPress = false;
        startTime = 0L;
        delay = 0L;
    }

    @Override
    public void onDisable() {
        toPress = false;
    }

    @EventHook
    public void onTick(ClientTickEvent event) {

        clampSettings();

        boolean curInScreen = mc.screen != null;

        if (lastInScreen && !curInScreen && !toPress) {
            toPress = true;

            float min = minDelay.getValue();
            float max = maxDelay.getValue();

            if (max > 0) {
                delay = (long) randomBetween(min, max);
                startTime = System.currentTimeMillis();
            } else {
                delay = 0;
                startTime = System.currentTimeMillis();
            }

            lastInScreen = false;
            return;
        }

        if (toPress) {
            if (System.currentTimeMillis() - startTime >= delay) {

                KeyMapping forward = mc.options.keyUp;
                KeyMapping left = mc.options.keyLeft;
                KeyMapping back = mc.options.keyDown;
                KeyMapping right = mc.options.keyRight;
                KeyMapping jump = mc.options.keyJump;
                KeyMapping sneak = mc.options.keyShift;

                forward.setDown(forward.isDown());
                left.setDown(left.isDown());
                back.setDown(back.isDown());
                right.setDown(right.isDown());
                jump.setDown(jump.isDown());
                sneak.setDown(sneak.isDown());

                toPress = false;
            }
        }

        lastInScreen = curInScreen;
    }

    private void clampSettings() {
        if (minDelay.getValue() > maxDelay.getValue()) {
            minDelay.setValue(maxDelay.getValue());
        }
    }

    private double randomBetween(float min, float max) {
        return min + (Math.random() * (max - min));
    }
}