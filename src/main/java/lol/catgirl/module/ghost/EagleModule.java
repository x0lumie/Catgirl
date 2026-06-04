package lol.catgirl.module.ghost;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.setting.impl.*;
import net.minecraft.world.item.BlockItem;

public class EagleModule extends Module {
    public static final EagleModule INSTANCE = new EagleModule();

    public EagleModule() {
        super("Eagle", "Works as a legit scaffold. shifts on block edges.",
                ModuleCategory.Ghost
                );
        addSettings(delay, blockCheck, directionalCheck, pitch);
    }

    public final SliderProperty delay = new SliderProperty("Delay", 50.0f, 0.0f, 200.0f, 10.0f);
    public final BoolProperty blockCheck = new BoolProperty("Blocks Only", true);
    public final BoolProperty directionalCheck = new BoolProperty("Directional Check", true);
    public final SliderProperty pitch = new SliderProperty("Pitch", 60.0f, 0.0f, 90.0f, 1.0f);

    private boolean wasOverBlock = false;
    private long lastSneakTime = 0;

    @EventHook
    public void onTick(ClientTickEvent event) {

        var player = mc.player;
        var level = mc.level;
        var options = mc.options;

        if (player == null) return;

        boolean holdingBlock = player.getMainHandItem() != null
                && player.getMainHandItem().getItem() instanceof BlockItem;

        boolean passBlockCheck = !blockCheck.getValue() || holdingBlock;
        boolean passDirectional = !directionalCheck.getValue() || player.zza < 0;
        boolean passPitch = player.getXRot() >= pitch.getValue().floatValue();

        if (passBlockCheck && passDirectional && passPitch) {

            boolean isAirBelow = level.getBlockState(
                    player.blockPosition().below()
            ).isAir();

            if (isAirBelow && player.onGround()) {
                options.keyShift.setDown(true);
                wasOverBlock = true;

            } else if (player.onGround()) {

                if (wasOverBlock) {
                    lastSneakTime = System.currentTimeMillis();
                }

                long currentTime = System.currentTimeMillis();
                long delayTime = delay.getValue().longValue();
                long randomizedDelay = (long) (delayTime * (Math.random() * 0.1 + 0.95));

                if (currentTime - lastSneakTime >= randomizedDelay) {
                    options.keyShift.setDown(org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().handle(), options.keyShift.getDefaultKey().getValue()) == 1);
                }

                wasOverBlock = false;
            }

        } else {
            options.keyShift.setDown(
                    org.lwjgl.glfw.GLFW.glfwGetKey(
                            mc.getWindow().handle(),
                            options.keyShift.getDefaultKey().getValue()
                    ) == 1
            );
        }
    }
}
