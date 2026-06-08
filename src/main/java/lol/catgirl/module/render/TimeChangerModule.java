package lol.catgirl.module.render;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.SliderProperty;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class TimeChangerModule extends Module {
    public static final TimeChangerModule INSTANCE = new TimeChangerModule();

    private final SliderProperty time = new SliderProperty("Time", 6000.0f, 0.0f, 24000.0f, 100.0f);
    public TimeChangerModule() {
        super("TimeChanger",
                "Changes the client time.",
                ModuleCategory.Render
        );

        addSettings(time);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.level == null) return;

        mc.level.getLevelData().setDayTime(time.getValue().longValue());

    }

    @EventHook
    public void onPacketReceive(PacketReceivedEvent event) {
        if(!this.isEnabled()) return;
        if (mc.level == null) return;

        if (event.packet instanceof ClientboundSetTimePacket) {
            event.setCancelled(true);
        }
    }
}