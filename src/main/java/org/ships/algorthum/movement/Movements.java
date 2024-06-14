package org.ships.algorthum.movement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedTransferQueue;

public class Movements {

    private static final Collection<BasicMovement> registered = new LinkedTransferQueue<>();

    public static final Ships5Movement SHIPS_5 = register(new Ships5Movement());
    public static final Ships6Movement SHIPS_6 = register(new Ships6Movement());

    public static <T extends BasicMovement> T register(@NotNull T movement) {
        registered.add(movement);
        return movement;
    }

    @UnmodifiableView
    public static Collection<BasicMovement> getMovements() {
        return Collections.unmodifiableCollection(registered);
    }

}
