package lol.catgirl.utils;

import net.minecraft.sounds.SoundEvents;

public class Utils implements IMinecraft {
    public static void warningSound() {
        mc.player.playSound(SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), 1.0f, 1.0f);
        mc.player.playSound(SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), 1.0f, 1.0f);
    }
}
