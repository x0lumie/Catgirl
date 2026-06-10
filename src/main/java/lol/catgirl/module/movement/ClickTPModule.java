package lol.catgirl.module.movement;

import lol.catgirl.event.EventHook;
import lol.catgirl.event.impl.ClientTickEvent;
import lol.catgirl.event.impl.Render3DEvent;
import lol.catgirl.module.Module;
import lol.catgirl.module.ModuleCategory;
import lol.catgirl.property.impl.BoolProperty;
import lol.catgirl.property.impl.SliderProperty;
import lol.catgirl.utils.client.ColorUtils;
import lol.catgirl.utils.player.PacketUtils;
import lol.catgirl.utils.render.RenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public final class ClickTPModule extends Module {
    public static final ClickTPModule INSTANCE = new ClickTPModule();

    public final SliderProperty blockOffset = new SliderProperty("Block Offset", 1.0f, -1f, 1f, 0.1f);
    public final SliderProperty spoofs = new SliderProperty("Spoofs", 0, 0, 40, 1);
    public final BoolProperty ground = new BoolProperty("On Ground", false);

    public ClickTPModule() {
        super("ClickTP", "Teleports you at the current block you are looking at.", ModuleCategory.Movement);
        addSettings(blockOffset, spoofs, ground);
    }

    private int delay;

    @EventHook
    public void onTick(ClientTickEvent event) {
        if(delay > 0) {
            delay--;
            return;
        }

        if (mc.options.keyPickItem.isDown() && delay < 0) {
            HitResult raycast = mc.player.pick(256.0d,
                    mc.getDeltaTracker().getGameTimeDeltaPartialTick(false),
                     false);

            if (raycast instanceof BlockHitResult blockHitResult
                && !mc.level.isEmptyBlock(blockHitResult.getBlockPos())
            ) {
                Vec3 position = Vec3.atCenterOf(blockHitResult.getBlockPos());

                for (int i = 0; i < spoofs.getValue(); i++) {
                    PacketUtils.sendPacket(
                            new ServerboundMovePlayerPacket.Pos(
                                    position.x, position.y + blockOffset.getValue(),
                                    position.z, ground.getValue(), false
                            ));
                }

                mc.player.setPos(
                        position.x, position.y + blockOffset.getValue(), position.z
                );
                delay = 5;
            }
        }
    }

    @EventHook
    public void onRender3D(Render3DEvent event) {

        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = ((BlockHitResult) mc.hitResult).getBlockPos();


        RenderUtils.renderBlock(pos, event, Color.RED);
    }
}

