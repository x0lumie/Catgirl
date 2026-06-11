package lol.catgirl.module.client;

// sshhh

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.event.impl.PreUpdateEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.module.combat.AuraModule;
import lol.catgirl.module.combat.VelocityModule;
import lol.catgirl.module.movement.SpeedModule;
import lol.catgirl.module.player.AutoArmorModule;
import lol.catgirl.module.player.ChestStealerModule;
import lol.catgirl.module.player.InventoryManagerModule;
import lol.catgirl.module.player.ScaffoldModule;
import lol.catgirl.property.impl.BoolProperty;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public final class AutoDisableModule extends Module {
    public static final AutoDisableModule INSTANCE = new AutoDisableModule();

    public final BoolProperty disableAuraWhenScaffold = new BoolProperty("Disable Aura When Scaffold", true);
    public final BoolProperty disableSpeedOnScaffold = new BoolProperty("Disable speed On Scaffold", true);
    public final BoolProperty disableAuraOnFlag = new BoolProperty("Disable Aura On Flag", false);
    public final BoolProperty disableScaffoldOnFlag = new BoolProperty("Disable Scaffold On Flag", false);
    public final BoolProperty disableInventoryManagerOnFlag = new BoolProperty("Disable Manager On Flag", false);
    public final BoolProperty disableAutoArmorOnFlag = new BoolProperty("Disable AutoArmor On Flag", false);
    public final BoolProperty disableChestStealerOnFlag = new BoolProperty("Disable ChestStealer On Flag", false);
    public final BoolProperty disableVelocityOnFlag = new BoolProperty("Disable Velocity On Flag", false);


    public AutoDisableModule() {
        super("AutoDisable", "Automatically disables modules when flagged.", ModuleCategory.Client);
        addSettings(
                disableAuraWhenScaffold,
                disableSpeedOnScaffold,
                disableAuraOnFlag,
                disableScaffoldOnFlag,
                disableInventoryManagerOnFlag,
                disableAutoArmorOnFlag,
                disableChestStealerOnFlag,
                disableVelocityOnFlag
        );
    }

    @EventHook
    public void onPreUpdate(PreUpdateEvent event) {
        if (disableAuraWhenScaffold.getValue()
                && AuraModule.INSTANCE.isEnabled() && ScaffoldModule.INSTANCE.isEnabled()) {
            AuraModule.INSTANCE.toggle();
        }

        if (disableSpeedOnScaffold.getValue() && SpeedModule.INSTANCE.isEnabled()
                && ScaffoldModule.INSTANCE.isEnabled()) {
            SpeedModule.INSTANCE.toggle();
        }
    }

    @EventHook
    public void onPacket(PacketReceivedEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (event.packet instanceof ClientboundPlayerPositionPacket) {
            if (event.packet instanceof ClientboundPlayerPositionPacket packet) {

                if (disableAuraOnFlag.getValue()
                        && AuraModule.INSTANCE.isEnabled()) {
                    AuraModule.INSTANCE.toggle();
                }

                if (disableScaffoldOnFlag.getValue()
                        && ScaffoldModule.INSTANCE.isEnabled()) {
                    ScaffoldModule.INSTANCE.toggle();
                }

                if (disableSpeedOnScaffold.getValue()
                        && SpeedModule.INSTANCE.isEnabled()) {
                    SpeedModule.INSTANCE.toggle();
                }

                if (disableInventoryManagerOnFlag.getValue()
                        && InventoryManagerModule.INSTANCE.isEnabled()) {
                    InventoryManagerModule.INSTANCE.toggle();
                }

                if (disableAutoArmorOnFlag.getValue()
                        && AutoArmorModule.INSTANCE.isEnabled()) {
                    AutoArmorModule.INSTANCE.toggle();
                }

                if (disableChestStealerOnFlag.getValue()
                        && ChestStealerModule.INSTANCE.isEnabled()) {
                    ChestStealerModule.INSTANCE.toggle();
                }

                if (disableVelocityOnFlag.getValue()
                        && VelocityModule.INSTANCE.isEnabled()) {
                    VelocityModule.INSTANCE.toggle();
                }
            }
        }
    }
}
