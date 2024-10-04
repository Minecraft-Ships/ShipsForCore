package org.ships.algorthum.blockfinder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedTransferQueue;

public final class BlockFinders {

    private static final Collection<BasicBlockFinder> registered = new LinkedTransferQueue<>();

    public static final Ships5BlockFinder SHIPS_FIVE_SYNCED = register(new Ships5BlockFinder());
    public static final Ships5AsyncBlockFinder SHIPS_FIVE_ASYNCED = register(new Ships5AsyncBlockFinder());

    public static final Ships6BlockFinder SHIPS_SIX_RELEASE_TWO_SYNCED = register(new Ships6BlockFinder());

    public static final Ships6MultiAsyncBlockFinder SHIPS_SIX_RELEASE_ONE_ASYNC_MULTI_THREADED = register(new Ships6MultiAsyncBlockFinder());

    public static final Ships6SingleAsyncBlockFinder SHIPS_SIX_RELEASE_ONE_ASYNC_SINGLE_THREADED = register(new Ships6SingleAsyncBlockFinder());

    private BlockFinders() {
        throw new RuntimeException("Do not create");
    }

    public static <T extends BasicBlockFinder> T register(@NotNull T finder) {
        registered.add(finder);
        return finder;
    }

    @UnmodifiableView
    public static Collection<BasicBlockFinder> getBlockFinders() {
        return Collections.unmodifiableCollection(registered);
    }


}
