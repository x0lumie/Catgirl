package lol.catgirl.simulation;

import com.mojang.authlib.GameProfile;
import lol.catgirl.utils.IMinecraft;
import lol.catgirl.utils.player.RotationUtils;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class PlayerSimulation implements IMinecraft {
    private RemotePlayer simulatedEntity;
    private final Player player;

    public PlayerSimulation(Player player) {
        if (mc.level == null) {
            this.player = null;
            return;
        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), "Simulated Player");

        this.simulatedEntity = new RemotePlayer(mc.level, profile) {
            @Override
            public void push(Entity entity) {
                // no col
            }
        };
        this.player = player;
        cloneStates();
    }

    private void cloneStates() {
        simulatedEntity.noPhysics = player.noPhysics;

        simulatedEntity.xOld = player.xOld;
        simulatedEntity.yOld = player.yOld;
        simulatedEntity.zOld = player.zOld;

        simulatedEntity.yRotO = player.yRotO;
        simulatedEntity.xRotO = player.xRotO;

        simulatedEntity.setPos(player.position());
        simulatedEntity.setBoundingBox(player.getBoundingBox());
        simulatedEntity.setDeltaMovement(player.getDeltaMovement());

        final float yaw = RotationUtils.yawChanged
                ? RotationUtils.getRotationYaw()
                : player.getYRot(); // idk if this is correct

        simulatedEntity.setYRot(yaw);
        simulatedEntity.setXRot(
                RotationUtils.pitchChanged
                        ? RotationUtils.getRotationPitch()
                        : player.getXRot()
        );

        simulatedEntity.setShiftKeyDown(player.isShiftKeyDown());
        simulatedEntity.setOnGround(player.onGround());
        simulatedEntity.setSprinting(player.isSprinting());

        for (MobEffectInstance effect : player.getActiveEffects()) {
            simulatedEntity.addEffect(new MobEffectInstance(effect));
        }

        simulatedEntity.setSpeed(player.getSpeed());

        simulatedEntity.fallDistance = player.fallDistance;
    }
}