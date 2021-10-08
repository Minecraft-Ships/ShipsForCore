package org.ships.algorthum.movement;

import org.core.TranslateCore;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.BlockSnapshot;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.ships.event.vessel.move.VesselMoveEvent;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.Result;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.types.Vessel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ships5AsyncedMovement implements BasicMovement {
    @Override
    public String getId() {
        return "ships:ships_five_asynced";
    }

    @Override
    public String getName() {
        return "Ships 5 Asynced";
    }

    @Override
    public Result move(Vessel vessel, MovementContext context) throws MoveException {
        List<MovingBlock> blocks = context.getMovingStructure().order(MovingBlockSet.ORDER_ON_PRIORITY);
        int waterLevel = -1;
        Optional<Integer> opWaterLevel = vessel.getWaterLevel();
        if (opWaterLevel.isPresent()) {
            waterLevel = opWaterLevel.get();
        }
        final int finalWaterLevel = waterLevel;
        /*List<MovingBlock> asyncedBlocks = new ArrayList<>();
        List<MovingBlock> syncedBlocks = new ArrayList<>();
        blocks.forEach(mb -> {
            SyncBlockPosition position = mb.getBeforePosition();
            if (position.getTileEntity().isPresent()) {
                syncedBlocks.add(mb);
                return;
            }
            asyncedBlocks.add(mb);
        });*/


        List<BlockSnapshot.AsyncBlockSnapshot> removeBlocksAsynced = blocks
                .stream()
                .map(mb -> {
                    BlockPosition after = mb.getBeforePosition();
                    BlockType type = BlockTypes.AIR;
                    if (finalWaterLevel >= after.getY()) {
                        type = BlockTypes.WATER;
                    }
                    return type.getDefaultBlockDetails().createSnapshot(Position.toASync(after));
                })
                .collect(Collectors.toList());

        /*List<BlockSnapshot.SyncBlockSnapshot> removeBlocksSynced = syncedBlocks
                .stream()
                .map(mb -> {
                    BlockPosition after = mb.getBeforePosition();
                    BlockType type = BlockTypes.AIR;
                    if (finalWaterLevel >= after.getY()) {
                        type = BlockTypes.WATER;
                    }
                    return type.getDefaultBlockDetails().createSnapshot(Position.toSync(after));
                })
                .collect(Collectors.toList());*/


        List<BlockSnapshot.AsyncBlockSnapshot> applyBlocks = blocks
                .stream()
                .map(mb -> {
                    BlockDetails details = mb.getStoredBlockData();
                    return details.createSnapshot(Position.toASync(mb.getAfterPosition()));
                }).collect(Collectors.toList());

        //CorePlugin.getServer().applyBlockSnapshots(removeBlocksSynced);

        TranslateCore
                .getServer()
                .applyBlockSnapshots(
                        removeBlocksAsynced,
                        ShipsPlugin.getPlugin(),
                        () -> TranslateCore
                                .getServer()
                                .applyBlockSnapshots(
                                        applyBlocks,
                                        ShipsPlugin.getPlugin(),
                                        () -> {
                                            Stream.of(context.getPostMovementProcess()).forEach(movement -> movement.postMove(vessel));
                                            vessel.set(MovingFlag.class, null);
                                            VesselMoveEvent.Post eventPost = new VesselMoveEvent.Post(vessel, context, Result.DEFAULT_RESULT);
                                            TranslateCore.getPlatform().callEvent(eventPost);
                                            context.getPostMovement().accept(eventPost);
                                            Result.DEFAULT_RESULT.run(vessel, context);
                                        }));
        return new Result();
    }
}
