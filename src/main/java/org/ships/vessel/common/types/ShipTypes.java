package org.ships.vessel.common.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.ships.vessel.common.assits.shiptype.CloneableShipType;
import org.ships.vessel.common.types.typical.airship.AirshipType;
import org.ships.vessel.common.types.typical.marsship.MarsshipType;
import org.ships.vessel.common.types.typical.opship.OPShipType;
import org.ships.vessel.common.types.typical.plane.PlaneType;
import org.ships.vessel.common.types.typical.submarine.SubmarineType;
import org.ships.vessel.common.types.typical.watership.WaterShipType;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Stream;

public final class ShipTypes {

    private static final @NotNull Collection<ShipType<?>> registered = new LinkedTransferQueue<>();

    public static final PlaneType PLANE = register(new PlaneType());
    public static final SubmarineType SUBMARINE = register(new SubmarineType());
    public static final AirshipType AIRSHIP = register(new AirshipType());
    public static final WaterShipType WATERSHIP = register(new WaterShipType());
    public static final MarsshipType MARSSHIP = register(new MarsshipType());
    public static final OPShipType OVERPOWERED_SHIP = register(new OPShipType());

    private ShipTypes() {
        throw new RuntimeException("Do not generate");
    }


    private static <T extends ShipType<?>> T register(@NotNull T shipType) {
        registered.add(shipType);
        return shipType;
    }

    public static void unregisterType(@NotNull ShipType<?> type) {
        registered.remove(type);
    }

    public static void registerType(@NotNull ShipType<?> shipType) {
        if (registered.stream().anyMatch(type -> type.getId().equals(shipType.getId()))) {
            throw new IllegalArgumentException(
                    "ShipType with the id of '" + shipType.getId() + "' is already registered");
        }
        registered.add(shipType);
    }

    @UnmodifiableView
    public static Collection<ShipType<?>> shipTypes() {
        return Collections.unmodifiableCollection(registered);
    }

    public static Stream<CloneableShipType<?>> cloneableShipTypes() {
        return shipTypes()
                .stream()
                .filter(shipType -> shipType instanceof CloneableShipType<?>)
                .map(shipType -> (CloneableShipType<?>) shipType);
    }
}
