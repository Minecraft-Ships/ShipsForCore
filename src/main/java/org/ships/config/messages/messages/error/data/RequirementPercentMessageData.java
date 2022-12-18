package org.ships.config.messages.messages.error.data;

import org.jetbrains.annotations.NotNull;
import org.ships.vessel.common.types.Vessel;

public class RequirementPercentMessageData {

    private final @NotNull Vessel vessel;
    private final int blocksMeetingRequirements;
    private final double percentageMet;

    public RequirementPercentMessageData(@NotNull Vessel vessel, double percentageMet, int blocksMeetingRequirements) {
        this.blocksMeetingRequirements = blocksMeetingRequirements;
        this.percentageMet = percentageMet;
        this.vessel = vessel;
    }

    public @NotNull Vessel getVessel() {
        return this.vessel;
    }

    public int getBlocksMeetingRequirements() {
        return this.blocksMeetingRequirements;
    }

    public double getPercentageMet() {
        return this.percentageMet;
    }
}
