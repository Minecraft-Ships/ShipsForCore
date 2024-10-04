package org.ships.vessel.common.flag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.concurrent.LinkedTransferQueue;

public final class VesselFlags {

    private static final Map<String, VesselFlag.Builder<?, ?>> flagsBuilders = new HashMap<>();
    private static final Collection<VesselFlag<?>> flags = new LinkedTransferQueue<>();

    public static final AltitudeLockFlag ALTITUDE_LOCK = registerDefault(new AltitudeLockFlag());
    public static final CooldownFlag COOLDOWN = registerDefault(new CooldownFlag());
    public static final EotFlag EOT = registerDefault(new EotFlag.Builder().buildEmpty());
    public static final FlightPathFlag FLIGHT_PATH = registerDefault(new FlightPathFlag());
    public static final MovingFlag MOVING = registerDefault(new MovingFlag());
    public static final SuccessfulMoveFlag SUCCESSFUL_MOVE = registerDefault(new SuccessfulMoveFlag());

    private VesselFlags() {
        throw new RuntimeException("Do not create");
    }

    public static <F extends VesselFlag<?>> Optional<F> getDefault(Class<F> clazz) {
        return flags.stream().filter(clazz::isInstance).findFirst().map(flag -> (F) flag);
    }

    public static <F extends VesselFlag<?>> @NotNull F registerDefault(F flag) {
        if (getDefault(flag.getClass()).isPresent()) {
            throw new IllegalArgumentException("Flag already has a default");
        }
        flags.add(flag);
        return flag;
    }

    public static <B extends VesselFlag.Builder<?, ?>> @NotNull B registerBuilder(String id, @NotNull B flag) {
        if (flagsBuilders.containsKey(id)) {
            throw new IllegalArgumentException("Id of '" + id + "' is already registered");
        }
        flagsBuilders.put(id, flag);
        return flag;
    }

    @UnmodifiableView
    public static Map<String, VesselFlag.Builder<?, ?>> builders() {
        return Collections.unmodifiableMap(flagsBuilders);
    }

}
