package lol.catgirl.module.combat;

import lol.catgirl.accessor.IServerboundInteractPacket;
import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.PacketSendEvent;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;

public final class MaceKillModule extends Module {
    public static final MaceKillModule INSTANCE = new MaceKillModule();

    private final BoolProperty swing = new BoolProperty("Swing", true);

    private final BoolProperty disableWhenBlocked = new BoolProperty("Disable When Blocked", true);
    private final SliderProperty fallHeight = new SliderProperty("Fall Height", 22, 1, 169, 1);
    private final SliderProperty spamPackets = new SliderProperty("Spam Packets", 4, 1, 17, 1);
    private final BoolProperty bypassTotems = new BoolProperty("Bypass Totems", false);
    private final SliderProperty attacks = new SliderProperty("Attacks", 3, 1, 10, 1);
    private final SliderProperty heightIncrease = new SliderProperty("Height Increase", 9, 1, 100, 1);
    private final BoolProperty useOffset = new BoolProperty("Use Offset", true);
    private final SliderProperty horizontalOffset = new SliderProperty("Horizontal Offset", 0.05f, 0f, 1f, 0.01f);
    private final SliderProperty yOffset = new SliderProperty("Y Offset", 0.01f, 0f, 1f, 0.01f);

    private Vec3 previousPos;
    private boolean sendingAttacks;

    private final BlockPos.MutableBlockPos mutablePos =
            new BlockPos.MutableBlockPos();

    private final Map<Vec3, Boolean> positionCache =
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Vec3, Boolean> eldest) {
                    return size() > 256;
                }
            };

    public MaceKillModule() {
        super("MaceKill",
                "Makes the mace into a overpowered weapon",
                ModuleCategory.Combat
        );

        addSettings(
                swing,
                disableWhenBlocked,
                fallHeight,
                spamPackets,
                bypassTotems,
                attacks,
                heightIncrease,
                useOffset,
                horizontalOffset,
                yOffset
        );
    }

    @EventHook
    public void onPacketSend(PacketSendEvent event) {

        if (sendingAttacks) {
            return;
        }

        if (mc.player == null || mc.level == null) {
            return;
        }

        if (!(event.getPacket() instanceof ServerboundInteractPacket packet)) {
            return;
        }

        IServerboundInteractPacket accessor =
                (IServerboundInteractPacket) packet;

        if (accessor.catgirl$getType()
                != ServerboundInteractPacket.ActionType.ATTACK) {
            return;
        }

        if (mc.player.getMainHandItem().getItem() != Items.MACE) {
            return;
        }

        Entity entity = accessor.catgirl$getEntity();

        if (!(entity instanceof LivingEntity target)) {
            return;
        }

        if (!target.isAlive()) {
            return;
        }

        if (disableWhenBlocked.getValue()) {

            if (target.isBlocking()
                    || target.isInvulnerable()) {

                return;
            }
        }

        int baseHeight = getMaxHeightAbovePlayer();

        if (baseHeight <= 0) {
            return;
        }

        event.setCancelled(true);

        previousPos = mc.player.position();

        int attackCount = bypassTotems.getValue()
                ? attacks.getValue().intValue()
                : 1;

        int currentHeight = baseHeight;

        for (int i = 0; i < spamPackets.getValue(); i++) {

            mc.player.connection.send(
                    new ServerboundMovePlayerPacket.Rot(
                            mc.player.getYRot(),
                            mc.player.getXRot(),
                            false, false

                    )
            );
        }

        try {

            boolean valid = true;

            for (int i = 0; i < attackCount; i++) {

                int blocks = (i == 0)
                        ? baseHeight
                        : currentHeight;

                if (mc.player.getY() + blocks >
                        mc.level.getMaxY() - 1) {

                    valid = false;
                    continue;
                }

                Vec3 targetPos = new Vec3(
                        mc.player.getX(),
                        mc.player.getY() + blocks,
                        mc.player.getZ()
                );

                sendMove(targetPos);
                sendMove(previousPos);

                mc.player.setPos(
                        previousPos.x,
                        previousPos.y,
                        previousPos.z
                );

                sendingAttacks = true;

                if (swing.getValue()) {

                    mc.player.swing(
                            InteractionHand.MAIN_HAND
                    );

                    mc.player.connection.send(
                            new ServerboundSwingPacket(
                                    InteractionHand.MAIN_HAND
                            )
                    );
                }

                mc.player.connection.send(
                        ServerboundInteractPacket.createAttackPacket(
                                target,
                                mc.player.isShiftKeyDown()
                        )
                );

                currentHeight +=
                        heightIncrease.getValue().intValue();
            }

            positionCache.clear();

            if (valid && useOffset.getValue()) {

                Vec3 offset = getOffset(previousPos);

                sendMove(offset);

                mc.player.setPos(
                        offset.x,
                        offset.y,
                        offset.z
                );
            }

        } finally {
            sendingAttacks = false;
        }
    }

    private void sendMove(Vec3 pos) {

        if (mc.player == null) {
            return;
        }

        mc.player.connection.send(
                new ServerboundMovePlayerPacket.PosRot(
                        pos.x,
                        pos.y,
                        pos.z,
                        mc.player.getYRot(),
                        mc.player.getXRot(),
                        false, false
                )
        );
    }

    private Vec3 getOffset(Vec3 base) {

        double dx = horizontalOffset.getValue();
        double dy = yOffset.getValue();

        Vec3[] offsets = new Vec3[] {
                base.add(dx, dy, 0),
                base.add(-dx, dy, 0),

                base.add(0, dy, dx),
                base.add(0, dy, -dx),

                base.add(dx, dy, dx),
                base.add(-dx, dy, -dx),

                base.add(-dx, dy, dx),
                base.add(dx, dy, -dx)
        };

        Collections.shuffle(Arrays.asList(offsets));

        for (Vec3 pos : offsets) {

            if (!invalid(pos)) {
                return pos;
            }
        }

        Vec3 vertical = base.add(0, dy, 0);

        if (!invalid(vertical)) {
            return vertical;
        }

        return base;
    }

    private boolean invalid(Vec3 pos) {

        if (mc.player == null || mc.level == null) {
            return true;
        }

        double clampedY = Mth.clamp(
                pos.y,
                mc.level.getMinY(),
                mc.level.getMaxY() - 1
        );

        if (clampedY != pos.y) {
            return true;
        }

        BlockPos floored = BlockPos.containing(pos);

        int chunkX = floored.getX() >> 4;
        int chunkZ = floored.getZ() >> 4;

        if (!mc.level.hasChunk(chunkX, chunkZ)) {
            return true;
        }

        if (positionCache.containsKey(pos)) {
            return positionCache.get(pos);
        }

        Entity entity = mc.player;

        Vec3 delta = pos.subtract(entity.position());

        AABB box = entity.getBoundingBox().move(delta);

        mutablePos.set(floored);

        for (int x = -1; x <= 1; x++) {

            mutablePos.setX(floored.getX() + x);

            for (int y = -1; y <= 1; y++) {

                mutablePos.setY(floored.getY() + y);

                for (int z = -1; z <= 1; z++) {

                    mutablePos.setZ(floored.getZ() + z);

                    BlockState state =
                            mc.level.getBlockState(mutablePos);

                    if (
                            state.is(Blocks.LAVA)
                                    || state.is(Blocks.FIRE)
                                    || state.is(Blocks.SOUL_FIRE)
                                    || state.is(Blocks.MAGMA_BLOCK)
                                    || state.is(Blocks.CAMPFIRE)
                                    || state.is(Blocks.SWEET_BERRY_BUSH)
                                    || state.is(Blocks.POWDER_SNOW)
                    ) {
                        positionCache.put(pos, true);
                        return true;
                    }
                }
            }
        }

        for (Entity e : mc.level.getEntities(entity, box)) {

            if (e.canBeCollidedWith(entity)) {

                positionCache.put(pos, true);
                return true;
            }
        }

        boolean collides =
                mc.level.getBlockCollisions(entity, box)
                        .iterator()
                        .hasNext();

        positionCache.put(pos, collides);

        return collides;
    }

    private int getMaxHeightAbovePlayer() {

        if (mc.player == null || mc.level == null) {
            return 0;
        }

        int worldTop = mc.level.getMaxY() - 1;

        int maxBlocks =
                (int) (worldTop - mc.player.getY());

        return Math.min(
                fallHeight.getValue().intValue(),
                maxBlocks
        );
    }
}