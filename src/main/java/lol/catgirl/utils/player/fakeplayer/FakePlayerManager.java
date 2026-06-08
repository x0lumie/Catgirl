package lol.catgirl.utils.player.fakeplayer;

import lol.catgirl.utils.IMinecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class FakePlayerManager implements IMinecraft {
    private static final List<FakePlayerEntity> fakePlayers = new java.util.ArrayList<>();

    public static FakePlayerEntity add(String name, boolean copyInv, float health) {
        if(mc.level == null || mc.player == null) return null;

        ClientLevel level = mc.level;

        FakePlayerEntity fake = new FakePlayerEntity(level, mc.player.getGameProfile());
        fake.copyFrom(mc.player, health);

        if (!copyInv) {
            fake.getInventory().clearContent();
        }

        level.addEntity(fake);
        fakePlayers.add(fake);
        return fake;
    }

    public static void removeAll() {
        if (mc.level == null) return;

        for (FakePlayerEntity fake : fakePlayers) {
            fake.remove(Entity.RemovalReason.DISCARDED);
        }

        fakePlayers.clear();
    }

    public static List<FakePlayerEntity> getPlayers() {
        return fakePlayers;
    }

    public static void remove(String name) {
        if(mc.level == null) return;

        for (FakePlayerEntity fake : fakePlayers) {
            if (fake.getGameProfile().name().equals(name)) {
                fake.remove(Entity.RemovalReason.DISCARDED);
                break;
            }
        }
    }
}
