package org.ships.movement.autopilot.path;

import org.array.utils.ArrayUtils;
import org.core.platform.plugin.Plugin;
import org.core.utils.Identifiable;
import org.core.vector.type.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class FlightPath implements Identifiable {

    private final boolean asExact;
    private final List<FlightCheckpoint> checkpoints = new LinkedList<>();

    private final @NotNull Plugin plugin;
    private final @NotNull String name;

    public FlightPath(@NotNull FlightPathBuilder builder) {
        this.asExact = builder.isAsExact();
        this.plugin = builder.getPlugin();
        this.name = builder.getName();
        this.checkpoints.addAll(builder.getCheckpoints());
    }

    public List<FlightCheckpoint> getCheckpoints() {
        return Collections.unmodifiableList(this.checkpoints);
    }

    public Optional<FlightCheckpoint> getNext(@NotNull FlightCheckpoint previous) {
        List<FlightCheckpoint> checkpoints = this.getCheckpoints();
        int previousIndex = checkpoints.indexOf(previous);
        int nextIndex = previousIndex + 1;
        if (checkpoints.size() == nextIndex) {
            return Optional.empty();
        }
        return Optional.of(checkpoints.get(previousIndex + 1));
    }

    public @NotNull FlightCheckpoint getNearest(@NotNull Vector3<Integer> position) {
        return ArrayUtils
                .getBest(checkpoint -> checkpoint.getPosition().getPosition().distanceSquared(position),
                         (original, compare) -> original > compare, this.getCheckpoints())
                .orElseThrow(() -> new RuntimeException("No checkpoints found"));
    }


    @Override
    public String getId() {
        return this.plugin.getPluginId() + ":" + this.name.toLowerCase().replaceAll(" ", "_");
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }
}
