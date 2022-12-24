package org.ships.movement.autopilot.path;

import org.core.platform.plugin.Plugin;
import org.core.vector.type.Vector3;
import org.core.world.WorldExtent;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.plugin.ShipsPlugin;

import java.util.LinkedList;
import java.util.List;

public class FlightPathBuilder {

    private boolean asExact;
    private final List<FlightCheckpoint> checkpoints = new LinkedList<>();

    private Plugin plugin;
    private String name;

    public FlightPath build() {
        return new FlightPath(this);
    }

    public boolean isAsExact() {
        return this.asExact;
    }

    public FlightPathBuilder setAsExact(boolean asExact) {
        this.asExact = asExact;
        return this;
    }

    public List<FlightCheckpoint> getCheckpoints() {
        return this.checkpoints;
    }

    public FlightPathBuilder addCheckpoint(FlightCheckpoint checkpoint) {
        this.checkpoints.add(checkpoint);
        return this;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public FlightPathBuilder setPlugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public FlightPathBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public FlightPathBuilder ofAutopilot(@NotNull BlockPosition vesselPosition,
                                         @NotNull Vector3<Integer> to,
                                         @Nullable Integer flightHeight) {
        WorldExtent world = vesselPosition.getWorld();
        int travelHeight = (flightHeight == null
                || vesselPosition.getY() >= flightHeight) ? vesselPosition.getY() : flightHeight;
        if (flightHeight != null && travelHeight != vesselPosition.getY()) {
            this.addCheckpoint(new FlightCheckpoint(
                    world.getPosition(vesselPosition.getX(), flightHeight, vesselPosition.getZ())));
        }
        if (vesselPosition.getX().intValue() != to.getX().intValue()) {
            this.addCheckpoint(new FlightCheckpoint(world.getPosition(to.getX(), travelHeight, vesselPosition.getZ())));
        }
        if (vesselPosition.getZ().intValue() != to.getZ().intValue()) {
            this.addCheckpoint(new FlightCheckpoint(world.getPosition(to.getX(), travelHeight, to.getZ())));
        }
        if (travelHeight != to.getY()) {
            this.addCheckpoint(new FlightCheckpoint(Position.toBlock(world.getPosition(to))));
        }
        this.asExact = false;
        this.name = "Autopilot";
        this.plugin = ShipsPlugin.getPlugin();
        return this;
    }
}
