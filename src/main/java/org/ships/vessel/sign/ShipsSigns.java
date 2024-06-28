package org.ships.vessel.sign;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedTransferQueue;

public final class ShipsSigns {

    private static final @NotNull Collection<ShipsSign> registered = new LinkedTransferQueue<>();

    public static final AltitudeSign ALTITUDE = register(new AltitudeSign());
    public static final EOTSign EOT = register(new EOTSign());
    public static final LicenceSign LICENCE = register(new LicenceSign());
    public static final WheelSign WHEEL = register(new WheelSign());
    public static final MoveSign MOVE = register(new MoveSign());

    private ShipsSigns() {
        throw new RuntimeException("Do not create");
    }


    private static <T extends ShipsSign> T register(@NotNull T shipType) {
        registered.add(shipType);
        return shipType;
    }

    @UnmodifiableView
    public static Collection<ShipsSign> signs() {
        return Collections.unmodifiableCollection(registered);
    }

}
