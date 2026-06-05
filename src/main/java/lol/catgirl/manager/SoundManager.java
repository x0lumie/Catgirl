package lol.catgirl.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class SoundManager {

    public enum Sounds {
        SIGMA_ON("sigmaenable"),
        SIGMA_OFF("sigmadisable"),
        AUGUSTUS_ON("augustus-enable"),
        AUGUSTUS_OFF("augustus-disable"),
        NOTE_ON("note-enable"),
        NOTE_OFF("note-disable"),
        SIMP_ON("simp-enable"),
        SIMP_OFF("simp-disable"),
        SMOOTH_ON("smoothenable"),
        SMOOTH_OFF("smoothdisable")

        ;

        private final Identifier id;
        private SoundEvent event;

        Sounds(String name) {
            this.id = Identifier.fromNamespaceAndPath(
                    "catgirl", name);
        }

        public SoundEvent get() {
            return event;
        }

        private void register() {
            this.event = Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    id,
                    SoundEvent.createVariableRangeEvent(id)
            );
        }

        public void play() {
            play(1.0f, 1.0f);
        }

        public void play(float volume, float pitch) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && event != null) {
                player.playSound(event, volume, pitch);
            }
        }
    }

    public void init() {
        for (Sounds sound : Sounds.values()) {
            sound.register();
        }
    }
}