package lol.catgirl.module.render;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

import java.time.LocalTime;

public class TimeChangerModule extends Module {
    public static final TimeChangerModule INSTANCE = new TimeChangerModule();

    public final BoolProperty realTime = new BoolProperty("Real World Time", false);
    public final SliderProperty time = new SliderProperty("Time", 6000.0f, 0.0f, 24000.0f, 100.0f).hide(()->realTime.getValue());

    public TimeChangerModule() {
        super("TimeChanger",
                "Changes the client time.",
                ModuleCategory.Render
        );

        addSettings(realTime, time);
    }

    @EventHook
    public void onTick(ClientTickEvent event) {
        if (mc.level == null) return;

        if(!realTime.getValue()) {
            mc.level.getLevelData().setDayTime(time.getValue().longValue());
        } else {
            long mTime = time.getValue().longValue();

            final LocalTime localTime = LocalTime.now();
            final int hour = localTime.getHour();
            final int minute = localTime.getMinute();

            final long totalMinutes = hour * 60L + minute;
            long minecraftTime = (totalMinutes * 1000L / 1440L) * 24L;
            mTime = (minecraftTime + 18000L) % 24000L;

            mc.level.getLevelData().setDayTime(time.getValue().longValue());
        }
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