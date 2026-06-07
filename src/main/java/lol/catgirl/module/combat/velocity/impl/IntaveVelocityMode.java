package lol.catgirl.module.combat.velocity.impl;

import lol.catgirl.event.impl.PacketReceivedEvent;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.module.combat.velocity.VelocityMode;
import lol.catgirl.utils.player.MoveUtils;
import lol.catgirl.utils.player.PlayerUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

public class IntaveVelocityMode implements VelocityMode {
    
    private int velocityTicks = 0;
    private int noMovementTicks = 0;
    private double lastReduction = 0;
    
    @Override
    public void onPacketRecieved(PacketReceivedEvent event) {
        if (event.packet instanceof ClientboundSetEntityMotionPacket) {
            ClientboundSetEntityMotionPacket packet = (ClientboundSetEntityMotionPacket) event.packet;
            
            if (packet.getId() == mc.player.getId()) {
                Vec3 original = packet.getMovement();
                
                if (Math.abs(original.x) < 0.05 && Math.abs(original.z) < 0.05) {
                    return;
                }
                
                boolean moving = MoveUtils.isMoving();
                boolean inWeb = PlayerUtils.isInWeb();
                boolean inWater = mc.player.isInWater();
                boolean onGround = mc.player.onGround();
                boolean sneaking = mc.player.isShiftKeyDown();
                boolean sprinting = mc.player.isSprinting();
                
                double reduction = 1.0;
                
                if (inWeb) {
                    reduction = 0.65;
                }
                else if (!moving) {
                    reduction = 0.70;
                    noMovementTicks++;
                }
                else if (sneaking) {
                    reduction = 0.75;
                }
                else if (onGround && Math.abs(original.x) < 0.3 && Math.abs(original.z) < 0.3) {
                    reduction = 0.80;
                }
                else if (moving && !sprinting) {
                    reduction = 0.88;
                }
                else if (sprinting) {
                    reduction = 0.95;
                }
                
                reduction += (Math.random() - 0.5) * 0.03;
                reduction = Math.max(0.65, Math.min(0.98, reduction));
                
                double newX = original.x * reduction;
                double newZ = original.z * reduction;
                double newY = original.y * 0.75;
                
                newX += (Math.random() - 0.5) * 0.002;
                newZ += (Math.random() - 0.5) * 0.002;
                
                if (Math.signum(newX) != Math.signum(original.x) && Math.abs(original.x) > 0.05) {
                    newX = original.x * 0.1;
                }
                if (Math.signum(newZ) != Math.signum(original.z) && Math.abs(original.z) > 0.05) {
                    newZ = original.z * 0.1;
                }
                
                try {
                    java.lang.reflect.Field field = ClientboundSetEntityMotionPacket.class.getDeclaredField("movement");
                    field.setAccessible(true);
                    field.set(packet, new Vec3(newX, newY, newZ));
                } catch (Exception e) {
                    packet.movement = new Vec3(newX, newY, newZ);
                }
                
                lastReduction = reduction;
                velocityTicks = 0;
            }
        }
    }
    
    @Override
    public void onTick(ClientTickEvent event) {
        if (mc.player == null) return;
        
        velocityTicks++;
        
        if (MoveUtils.isMoving()) {
            noMovementTicks = 0;
        } else if (noMovementTicks > 0) {
            noMovementTicks--;
        }
        
        if (velocityTicks == 2 && lastReduction < 0.85) {
            Vec3 vel = mc.player.getDeltaMovement();
            if (Math.abs(vel.x) < 0.08 && Math.abs(vel.z) < 0.08 && MoveUtils.isMoving()) {
                float yaw = mc.player.getYRot();
                float radYaw = (float) Math.toRadians(yaw);
                double addX = -Math.sin(radYaw) * 0.02;
                double addZ = Math.cos(radYaw) * 0.02;
                mc.player.setDeltaMovement(vel.x + addX, vel.y, vel.z + addZ);
            }
        }
        
        if (velocityTicks > 20) {
            velocityTicks = 0;
        }
    }
}