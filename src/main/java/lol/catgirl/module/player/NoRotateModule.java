package lol.catgirl.module.player;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.EnumProperty;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public final class NoRotateModule extends Module {
    public static final NoRotateModule INSTANCE = new NoRotateModule();

    private enum Mode {
        Cancel
    }

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.Cancel);

    public NoRotateModule() {
        super("NoRotate", "Prevents the server from rotating you.",
                ModuleCategory.Player);
        addSetting(mode);
    }

    @EventHook
    public void onPacket(PacketReceivedEvent event) {
        if (mode.getValue() == Mode.Cancel) {
            if (event.packet instanceof ServerboundMovePlayerPacket.Rot packet) {
                packet.yRot = mc.player.getYRot();
                packet.xRot = mc.player.getXRot();
            }
        }
    }
}
