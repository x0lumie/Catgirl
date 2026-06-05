package lol.catgirl.module.ghost;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.BoolProperty;
import lol.catgirl.setting.impl.SliderProperty;
import lol.catgirl.utils.client.ClickUtil;
import lol.catgirl.utils.client.MathUtil;
import lol.catgirl.utils.client.TickingTimer;
import net.minecraft.world.phys.HitResult;
import lol.catgirl.module.Module;

public final class AutoClickerModule extends Module {

    public static final AutoClickerModule INSTANCE = new AutoClickerModule();

    public final BoolProperty disableWhileBreaking = new BoolProperty("Disable While Break", true);
    public final SliderProperty minLeftCps = new SliderProperty("Min Left CPS", 10, 0, 20, 1);
    public final SliderProperty maxLeftCps = new SliderProperty("Max Left CPS", 15, 0, 20, 1);
    public final SliderProperty minRightCps = new SliderProperty("Min Right CPS", 10, 0, 20, 1);
    public final SliderProperty maxRightCps = new SliderProperty("Max Right CPS", 20, 0, 20, 1);
    public final BoolProperty leftClicker = new BoolProperty("Left Clicker", true);
    public final BoolProperty rightClicker = new BoolProperty("Right Clicker", false);
    public final BoolProperty blockHit = new BoolProperty("Block Hit", false);
    public final SliderProperty blockHitChance = new SliderProperty("Block Hit Chance", 100, 0, 100, 1).hide(()->!blockHit.getValue());
    public final SliderProperty blockHitDelay = new SliderProperty("Block Hit Delay", 80, 0, 300, 1).hide(()->!blockHit.getValue());


    public AutoClickerModule() {
        super("AutoClicker",
                "Automatically clicks the mouse with block-hit features.",
                ModuleCategory.Combat
        );
        addSettings(
                disableWhileBreaking,
                minLeftCps,
                maxLeftCps,
                minRightCps,
                maxRightCps,
                leftClicker,
                rightClicker,
                blockHit,
                blockHitChance,
                blockHitDelay
        );
    }

    private final TickingTimer
            leftTimerUtil = new TickingTimer(),
            rightTimerUtil = new TickingTimer(),
            blockHitTimerUtil = new TickingTimer()
    ;

    private boolean blockingPaused;

    private double currentLeftCPS;
    private double currentRightCPS;

    @Override
    public void onEnable() {
        currentLeftCPS = minLeftCps.getValue();
        currentRightCPS = minRightCps.getValue();
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if(!this.isEnabled()) return;
        if (mc.player == null) return;

        if (mc.mouseHandler.isLeftPressed() && leftClicker.getValue()) {

            if (mc.player.pick(mc.player.blockInteractionRange(), mc.player.attackAnim, false).getType() == HitResult.Type.BLOCK
                    && disableWhileBreaking.getValue()) {
                ClickUtil.action(ClickUtil.Button.LEFT, true);
                return;
            }

            if (leftTimerUtil.hasTimeElapsed(1000 / currentLeftCPS, true)) {

                if (blockHit.getValue()
                        && MathUtil.randomInt(0, 100) <= blockHitChance.getValue()
                        && !blockingPaused) {

                    blockingPaused = true;
                    blockHitTimerUtil.reset();

                    ClickUtil.action(ClickUtil.Button.RIGHT, false);

                    ClickUtil.action(ClickUtil.Button.LEFT, true);
                    currentLeftCPS = MathUtil.randomInt(
                            Math.round(minLeftCps.getValue()),
                            Math.round(maxLeftCps.getValue())
                    );
                    ClickUtil.action(ClickUtil.Button.LEFT, false);

                    return;
                }

                if (blockingPaused && blockHitTimerUtil.hasTimeElapsed(blockHitDelay.getValue(), false)) {
                    blockingPaused = false;
                }

                ClickUtil.action(ClickUtil.Button.LEFT, true);
                currentLeftCPS = MathUtil.randomInt(
                        Math.round(minLeftCps.getValue()),
                        Math.round(maxLeftCps.getValue())
                );
                ClickUtil.action(ClickUtil.Button.LEFT, false);
            }
        }

        if (mc.mouseHandler.isRightPressed() && rightClicker.getValue()){
            if (rightTimerUtil.hasTimeElapsed(1000 / currentRightCPS, true)){
                ClickUtil.action(ClickUtil.Button.RIGHT, true);
                currentRightCPS = MathUtil.randomInt(Math.round(minRightCps.getValue()), Math.round(maxRightCps.getValue()));
                ClickUtil.action(ClickUtil.Button.RIGHT, false);

            }
        }
    }
}
