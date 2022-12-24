package org.ships.vessel.common.flag;

import org.core.config.parser.StringParser;
import org.jetbrains.annotations.NotNull;
import org.ships.movement.autopilot.path.FlightCheckpoint;
import org.ships.movement.autopilot.path.FlightPath;
import org.ships.movement.instruction.details.MovementDetailsBuilder;

import java.util.Optional;

public class FlightPathFlag implements VesselFlag<FlightPath> {

    private FlightPath path;
    private FlightCheckpoint currentCheckpoint;
    private MovementDetailsBuilder movementDetail = new MovementDetailsBuilder();

    public void setCurrentCheckpoint(FlightCheckpoint checkpoint) {
        if (this.path == null) {
            throw new IllegalStateException("FlightPath has not been set");
        }
        if (!this.path.getCheckpoints().contains(checkpoint)) {
            throw new IllegalArgumentException("Checkpoint is not contained in the path");
        }
        this.currentCheckpoint = checkpoint;
    }

    public void setMovementDetail(@NotNull MovementDetailsBuilder builder) {
        this.movementDetail = builder;
    }

    public @NotNull MovementDetailsBuilder getMovementDetail() {
        return this.movementDetail;
    }

    public Optional<FlightCheckpoint> getCurrentCheckpoint() {
        return Optional.ofNullable(this.currentCheckpoint);
    }

    @Override
    public Optional<FlightPath> getValue() {
        return Optional.ofNullable(this.path);
    }

    @Override
    public void setValue(FlightPath value) {
        this.path = value;
        this.currentCheckpoint = null;
    }

    @Override
    public StringParser<FlightPath> getParser() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public Builder<FlightPath, ? extends VesselFlag<FlightPath>> toBuilder() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public String getId() {
        return "flight_path";
    }

    @Override
    public String getName() {
        return "Flight Path";
    }
}
