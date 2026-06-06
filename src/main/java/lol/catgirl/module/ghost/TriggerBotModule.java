package lol.catgirl.module.ghost;

import com.mojang.blaze3d.platform.InputConstants;
import lol.catgirl.event.EventHook;
import lol.catgirl.mixin.KeyMappingAccessor;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import lol.catgirl.utils.player.PlayerUtils;
import lol.catgirl.utils.render.RenderUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import lol.catgirl.module.Module;
import lol.catgirl.event.impl.*;


public class TriggerBotModule extends Module {
    public static final TriggerBotModule INSTANCE = new TriggerBotModule();

    private enum AttackMode {
        Default,
        Fast,
        SuperFast
    }

    public final EnumProperty<AttackMode> attackMode = new EnumProperty<>("Attack Mode", AttackMode.Fast);


    public final BoolProperty targetESP = new BoolProperty("Target ESP", false);
    public final BoolProperty critical = new BoolProperty("Critical", false);
    public final BoolProperty wtap = new BoolProperty("W-Tap", false);
    public final BoolProperty onlyWhileHeld = new BoolProperty("Only While Held", false);

    public TriggerBotModule() {
        super("Triggerbot",
                "Automatically attacks the crosshair target.",
                ModuleCategory.Ghost);
        addSettings(targetESP, critical, wtap, onlyWhileHeld);
    }

    private EntityHitResult entityHit;
    private final InputConstants.Key AttackKey = InputConstants.getKey("key.mouse.left");
    public boolean resetting = false;
    public int t = 0;
    private AimAssistModule aim;

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        aim = AimAssistModule.INSTANCE;

        if (!mc.options.keyAttack.isDown() && onlyWhileHeld.getValue()) return;

        HitResult hit = mc.hitResult;
        if (hit.getType() == HitResult.Type.ENTITY) {
            entityHit = (EntityHitResult) hit;

            if (canAttack() && !(aim != null && aim.isEnabled())) {
                float cooldown = mc.player.getAttackStrengthScale(0.5f);

                var mode = attackMode.getValue();
                float delay = mode.equals("Fast") ? 0.9f :
                        (mode.equals("Super Fast") ? 0.8f : 1.0f);

                boolean attack = !critical.getValue() ||
                        (mc.player.onGround() && !canCrit()) ||
                        (canCrit());

                if (attack && cooldown >= delay) {
                    pressKey(mc.options.keyAttack);
                }
            }
        } else {
            entityHit = null;
        }

        if (entityHit == null) {
            if (!mc.options.keyUp.isDown() && resetting) {
                mc.options.keyUp.setDown(true);
            }

            resetting = false;
            t = 0;
        }

        if (wtap.getValue()) {

            if (resetting) {
                t++;

                int delay = 4 + mc.player.getRandom().nextInt(3); // 2–4 ticks

                if (t >= delay) {
                    mc.options.keyUp.setDown(true);

                    if (!mc.player.isSprinting()) {
                        mc.options.keySprint.setDown(true);
                    }

                    resetting = false;
                    t = 0;
                }
            }
        }
    }

    @EventHook
    public void onAttack(PlayerAttackPreEvent event) {
        if (!wtap.getValue()) return;

        float mF = mc.player.input.getMoveVector().y;

        if (mc.options.keyUp.isDown() && mF > 0) {
            resetting = true;
            t = 0;
            mc.options.keyUp.setDown(false);
        }
    }


    private boolean canCrit() {
        return mc.player.fallDistance > 0
                && !mc.player.onGround()
                && !mc.player.isInWater()
                && !mc.player.isPassenger()
                && !mc.player.onClimbable()
                && !mc.player.hasEffect(MobEffects.BLINDNESS);
    }

    @EventHook
    public void onRender(Render3DEvent e) {
        if (targetESP.getValue()) {
            if (canAttack()) {
                RenderUtils.renderBox(entityHit.getEntity(), e, e.partialTicks);
            }
        }
    }

    private boolean canAttack() {
        return entityHit != null &&
                entityHit.getEntity().isAlive()
                && PlayerUtils.isHoldingWeapon() && entityHit.getEntity()
                instanceof LivingEntity;
    }

    public static void pressKey(KeyMapping key) {
        InputConstants.Key bind = ((KeyMappingAccessor) key).getKey();
        KeyMapping.click(bind);
    }

    @Override
    protected String getFinalSuffix() {
        return attackMode.getValue().toString();
    }
}