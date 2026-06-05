package lol.catgirl.module.player;

import com.mojang.blaze3d.platform.InputConstants;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.EnumProperty;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import lol.catgirl.module.Module;

import java.util.Arrays;
public final class InventoryMoveModule extends Module {
    public static final InventoryMoveModule INSTANCE = new InventoryMoveModule();

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Grim);
    public final BoolProperty sneak = new BoolProperty("Allow Sneaking", false);
    public final BoolProperty sprint = new BoolProperty("Sprint", false);

    public final BoolProperty chestCheck = new BoolProperty("Chest Check", true);

    // TODO: make intave better

    public enum Mode {
        Normal,
        Grim,
        Intave
    }

    public InventoryMoveModule() {
        super("InventoryMove",
                "Allows you move inside of your inventory.",
                ModuleCategory.Movement
        );

        addSettings(
                mode,
                sneak,
                sprint,
                chestCheck
        );
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (!isEnabled() || skip()) {
            return;
        }

        if (mode.getValue() == Mode.Intave && isContainerScreenOpen()) {
            KeyMapping[] keys = {
                    mc.options.keyUp,
                    mc.options.keyDown,
                    mc.options.keyLeft,
                    mc.options.keyRight
            };

            for (KeyMapping key : keys) {
                key.setDown(isPressed(key));
            }

            mc.options.keyShift.setDown(true);

            if (mc.player.tickCount % 20 == 0) {
                mc.options.keyJump.setDown(false);
            }
        } else {
            KeyMapping[] keys = {
                    mc.options.keyUp,
                    mc.options.keyDown,
                    mc.options.keyLeft,
                    mc.options.keyRight,
                    mc.options.keyJump,
            };

            if (sneak.getValue()) {
                keys = Arrays.copyOf(keys, keys.length + 1);
                keys[keys.length - 1] = mc.options.keyShift;
            }

            for (KeyMapping key : keys) {
                key.setDown(isPressed(key));
            }
        }
    }

    @EventHook
    public void onPacketReceive(PacketReceivedEvent event) {
        if (!this.isEnabled() || mc.player == null) return;

        Packet<?> packet = event.packet;

        if (mode.getValue() == Mode.Grim && packet instanceof ClientboundContainerClosePacket closePacket) {
            if (closePacket.getContainerId() == mc.player.inventoryMenu.containerId) {
                event.setCancelled(true);
            }
        }
    }

    @EventHook
    public void onPacketSend(PacketSendEvent event) {
        if (!this.isEnabled() || !(mode.getValue() == Mode.Intave) || mc.player == null) {
            return;
        }

        Packet<?> packet = event.getPacket();

        if (packet instanceof ServerboundContainerClosePacket) {
            if (isContainerScreenOpen()) {
                event.setCancelled(true);
            }
        }

        if (packet instanceof ServerboundPlayerCommandPacket commandPacket) {
            if (isContainerScreenOpen()) {
                ServerboundPlayerCommandPacket.Action action = commandPacket.getAction();

                if (action == ServerboundPlayerCommandPacket.Action.START_SPRINTING ||
                        action == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean skip() {
        return mc.screen instanceof CreativeModeInventoryScreen
                || mc.screen instanceof ChatScreen
                || mc.screen instanceof SignEditScreen
                || mc.screen instanceof AnvilScreen
                || mc.screen instanceof AbstractCommandBlockEditScreen
                || mc.screen instanceof StructureBlockEditScreen
                || (chestCheck.getValue() && mc.screen instanceof ContainerScreen);
    }

    private boolean isContainerScreenOpen() {
        return mc.screen instanceof AbstractContainerScreen;
    }

    private boolean isPressed(KeyMapping key) {
        return InputConstants.isKeyDown(
                mc.getWindow(),
                key.getDefaultKey().getValue()
        );
    }

    @Override
    public void onDisable() {
        for (KeyMapping key : new KeyMapping[]{
                mc.options.keyUp,
                mc.options.keyDown,
                mc.options.keyLeft,
                mc.options.keyRight,
                mc.options.keyJump,
                mc.options.keyShift
        }) {
            key.setDown(false);
        }
    }

    @Override
    protected String getFinalSuffix() {
        return mode.getValue().toString();
    }
}