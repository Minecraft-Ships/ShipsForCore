package org.ships.algorthum.blockfinder.exact;

import org.core.world.position.impl.BlockPosition;

import java.util.Optional;
import java.util.function.Predicate;

public interface ExactBlockFinder {

    Optional<BlockPosition> findFirst(BlockPosition starting, Predicate<BlockPosition> predicate);
}
