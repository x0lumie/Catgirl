package lol.catgirl.utils.player;

import lol.catgirl.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerUtils implements IMinecraft {

    public static int jumpAge;
    public static int hurtAge;
    public static long lastModTime;

    public static double getBiblicallyAccurateDistanceToEntity(Entity target) {
        return mc.player.getEyePosition().distanceTo(getClosestPoint(target));
    }

    @SuppressWarnings("deprecation")
    public static boolean isOverLiquid() {
        BlockPos pos = mc.player.blockPosition().below();
        return mc.level.getBlockState(pos).liquid();
    }

    public static double getArmorProtection(ItemStack stack) {
        double protection = 0;
        final ItemAttributeModifiers attributeModifiersComponent = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry entry : attributeModifiersComponent.modifiers()) {
            if (entry.attribute() != Attributes.ARMOR) {
                continue;
            }
            AttributeModifier
                    modifier = entry.modifier();
            protection += modifier.amount();
        }
        return protection;
    }

    public static double getStackAttackDamage(ItemStack stack) {
        double attackDamage = 0;
        final ItemAttributeModifiers attributeModifiersComponent = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry entry : attributeModifiersComponent.modifiers()) {
            if (entry.attribute() != Attributes.ATTACK_DAMAGE || entry.slot() != EquipmentSlotGroup.MAINHAND) {
                continue;
            }
            AttributeModifier modifier = entry.modifier();
            attackDamage += modifier.amount();
        }
        return attackDamage;
    }

    public static Vec3 getClosestPoint(Entity target) {
        AABB hb = target.getBoundingBox();
        Vec3 eyePos = mc.player.getEyePosition();


        double cx = Mth.clamp(eyePos.x, hb.minX, hb.maxX);
        double cy = Mth.clamp(eyePos.y, hb.minY, hb.maxY);
        double cz = Mth.clamp(eyePos.z, hb.minZ, hb.maxZ);

        return new Vec3(cx, cy, cz);
    }

    public static double getBiblicallyAccurateDistanceToCentreOfEntity(Entity target) {
        return mc.player.getEyePosition().distanceTo(getCentrePoint(target));
    }

    public static Vec3 getCentrePoint(Entity target) {
        AABB hb = target.getBoundingBox();
        return hb.getCenter();
    }

    public static HitResult raycast(float yaw, float pitch, double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3 startPos = mc.player.getEyePosition(tickDelta);
        Vec3 direction = mc.player.calculateViewVector(pitch, yaw);
        Vec3 endPos = startPos.add(direction.scale(maxDistance));

        HitResult blockHit = mc.level.clip(new ClipContext(
                startPos, endPos,
                ClipContext.Block.COLLIDER,
                includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                mc.player
        ));

        EntityHitResult entityHit = raycastEntities(mc.level, mc.player, startPos, endPos, maxDistance);

        if (entityHit != null && (blockHit == null || entityHit.getLocation().distanceToSqr(startPos) < blockHit.getLocation().distanceToSqr(startPos))) {
            return entityHit;
        }

        return null;
    }
    public static BlockHitResult raycastBlocks(float yaw, float pitch, double maxDistance, boolean includeFluids) {
        Vec3 startPos = mc.player.getEyePosition();

        float f = pitch * ((float)Math.PI / 180F);
        float g = -yaw * ((float)Math.PI / 180F);
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        Vec3 direction = new Vec3((double)(i * j), (double)(-k), (double)(h * j));

        Vec3 endPos = startPos.add(direction.scale(maxDistance));

        return mc.level.clip(new ClipContext(
                startPos,
                endPos,
                ClipContext.Block.OUTLINE,
                includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                mc.player
        ));
    }

    public static BlockHitResult raycastBlocks(float yaw, float pitch, double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3 startPos = mc.player.getEyePosition(tickDelta);
        Vec3 direction = mc.player.calculateViewVector(pitch, yaw);
        Vec3 endPos = startPos.add(direction.scale(maxDistance));

        BlockHitResult blockHit = mc.level.clip(new ClipContext(
                startPos,
                endPos,
                ClipContext.Block.OUTLINE,
                includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                mc.player
        ));

        return blockHit;
    }

    // Entity raycasting method (manual)
    private static EntityHitResult raycastEntities(Level world, Player player, Vec3 startPos, Vec3 endPos, double maxDistance) {
        EntityHitResult closestEntityHit = null;
        double closestDistanceSq = maxDistance * maxDistance;

        AABB searchBox = new AABB(startPos, endPos).inflate(1.0);

        for (Entity entity : world.getEntities(player, searchBox)) {
            if (!entity.isAlive() || entity.isSpectator()) continue;

            AABB entityBox = entity.getBoundingBox();
            Optional<Vec3> intersection = entityBox.clip(startPos, endPos);
            if (intersection.isPresent()) {
                double distanceSq = startPos.distanceToSqr(intersection.get());
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closestEntityHit = new EntityHitResult(entity, intersection.get());
                }
            }
        }

        return closestEntityHit;
    }

    public static boolean isHoldingWeapon() {
        if (mc.player.getMainHandItem() != null) {
            ItemStack t = mc.player.getMainHandItem();
            Item item = mc.player.getMainHandItem().getItem();
            return t.is(ItemTags.SWORDS) || item instanceof AxeItem;
        }
        return false;
    }

    public static boolean isHoldingMace() {
        if (mc.player.getMainHandItem() != null) {
            Item item = mc.player.getMainHandItem().getItem();
            return item instanceof MaceItem;
        }
        return false;
    }

    public static int fallDistance() {
        var player = mc.player;
        var world = mc.level;

        int startY = player.getBlockY();
        if (player.getY() % 1.0 == 0) startY--;

        int fallDistance = 0;

        for (int y = startY; y >= 0; y--) {
            BlockState block = world.getBlockState(new BlockPos(player.getBlockX(), y, player.getBlockZ()));
            if (!block.isAir() && !block.getBlock().toString().toLowerCase().contains("sign")) {
                fallDistance = startY - y;
                break;
            }
        }

        return fallDistance;
    }

    public static double getFallDistanceDouble() {
        var player = mc.player;
        var world = mc.level;

        double startY = player.getY();
        double motionY = player.getDeltaMovement().y;

        if (startY % 1.0 == 0) startY -= 1e-5;

        double fallDistance = 0;

        double minY = world.getMinY();

        for (double y = startY; y >= minY; y -= 0.01) {
            BlockState block = world.getBlockState(new BlockPos(player.getBlockX(), (int)Math.floor(y), player.getBlockZ()));
            if (!block.isAir() && !block.getBlock().toString().toLowerCase().contains("sign")) {
                fallDistance = startY - y;
                break;
            }
        }

        if (motionY < 0) fallDistance = -fallDistance;


        return fallDistance;
    }

    private static final ScheduledExecutorService jumpExecutor = Executors.newSingleThreadScheduledExecutor();

    public static void jump() {
        mc.options.keyJump.setDown(true);
        jumpExecutor.schedule(() -> mc.options.keyJump.setDown(false),
                20, TimeUnit.MILLISECONDS);

//        mc.player.input.makeJump();
    }

    public static boolean isInWeb() {
        BlockPos pos = mc.player.blockPosition();
        Block block = mc.level.getBlockState(pos).getBlock();

        if (block != null && block == Blocks.COBWEB) {
            return true;
        }
        return false;
    }

    public static boolean isOverVoid() {
        if (mc.player == null || mc.level == null) return false;

        BlockPos pos = mc.player.blockPosition();

        for (int y = pos.getY(); y > mc.level.getMinY(); y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());

            if (!mc.level.getBlockState(checkPos).isAir()) {
                return false;
            }
        }

        return true;
    }
}