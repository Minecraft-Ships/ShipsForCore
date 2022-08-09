package org.ships.movement.instruction.actions;


import org.core.exceptions.DirectionNotSupported;
import org.core.world.direction.Direction;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.jetbrains.annotations.NotNull;
import org.ships.movement.MovingBlock;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public interface MidMovements {

    static final MidMovement ROTATE_BLOCKS_RIGHT = new MidMovement() {
        @Override
        public void move(@NotNull MovingBlock moving) {
            BlockDetails blockDetails = moving.getStoredBlockData();
            Optional<DirectionalData> opDirectional = blockDetails.getDirectionalData();
            if (opDirectional.isEmpty()) {
                Collection<Direction> opData = blockDetails.getAll(KeyedData.MULTI_DIRECTIONAL);
                blockDetails.set(KeyedData.MULTI_DIRECTIONAL,
                        opData.stream().map(Direction::getRightAngleRight).collect(Collectors.toSet()));
                return;
            }
            DirectionalData directionalData = opDirectional.get();
            Direction direction = directionalData.getDirection().getRightAngleRight();
            try {
                directionalData.setDirection(direction);
            } catch (DirectionNotSupported directionNotSupported) {
                directionNotSupported.printStackTrace();
            }
        }
    };

    static final MidMovement ROTATE_BLOCKS_LEFT = new MidMovement() {
        @Override
        public void move(@NotNull MovingBlock moving) {
            BlockDetails blockDetails = moving.getStoredBlockData();
            Optional<DirectionalData> opDirectional = blockDetails.getDirectionalData();
            if (opDirectional.isEmpty()) {
                Collection<Direction> opData =
                        blockDetails.getAll(KeyedData.MULTI_DIRECTIONAL);
                if (!opData.isEmpty()) {
                    Collection<Direction> collection =
                            opData.stream().map(Direction::getRightAngleLeft).collect(Collectors.toSet());
                    ;
                    blockDetails.set(KeyedData.MULTI_DIRECTIONAL, collection);
                }
                return;
            }
            DirectionalData directionalData = opDirectional.get();
            Direction direction = directionalData.getDirection().getRightAngleLeft();
            try {
                directionalData.setDirection(direction);
            } catch (Exception directionNotSupported) {
                directionNotSupported.printStackTrace();
            }
        }
    };
}
