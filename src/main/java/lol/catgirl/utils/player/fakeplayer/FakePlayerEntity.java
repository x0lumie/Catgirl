package lol.catgirl.utils.player.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class FakePlayerEntity extends AbstractClientPlayer {
    public FakePlayerEntity(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    public void copyFrom(Player player, float health) {
        this.setPos(player.getX(), player.getY(), player.getZ());
        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot());

        this.setHealth(health);
        this.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel());


        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            this.getInventory().setItem(i, inv.getItem(i).copy());
        }
    }

    public static GameProfile createProfile(String name) {
        return new GameProfile(UUID.randomUUID(), name);
    }
}
