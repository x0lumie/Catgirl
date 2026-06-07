package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.property.impl.SliderProperty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public class ElytraFlyModule extends Module {
    public static final ElytraFlyModule INSTANCE = new ElytraFlyModule();

    public final SliderProperty horizontal = new SliderProperty("Horizontal", 2.0f, 0.1f, 10.0f, 0.1f);
    public final SliderProperty vertical = new SliderProperty("Vertical", 1.0f, 0.1f, 10.0f, 0.1f);

    public ElytraFlyModule() {
        super("ElytraFly", "Makes flying with an elytra easier.", ModuleCategory.Movement);
        addSettings(horizontal, vertical);
    }

    public boolean isElytraFlying() {
        if (mc.player == null) return false;

        return mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA
                && mc.player.isFallFlying();
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (!this.isEnabled()) return;
        if (mc.player == null) return;

        if (!isElytraFlying()) return;

        double motionX = 0.0;
        double motionY = 0.0;
        double motionZ = 0.0;

        float forward = mc.player.zza;
        float strafe = mc.player.xxa;
        float yaw = mc.player.getYRot();

        double speed = horizontal.getValue() * 0.1;

        if (forward != 0 || strafe != 0) {
            double rad = Math.toRadians(yaw);

            motionX = (-Math.sin(rad) * forward + Math.cos(rad) * strafe) * speed;
            motionZ = ( Math.cos(rad) * forward + Math.sin(rad) * strafe) * speed;
        }

        if (mc.options.keyJump.isDown()) {
            motionY = vertical.getValue() * 0.1;
        }

        if (mc.options.keyShift.isDown()) {
            motionY = -vertical.getValue() * 0.1;
        }

        mc.player.setDeltaMovement(new Vec3(motionX, motionY, motionZ));
    }
}