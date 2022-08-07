package org.ships.exceptions.load;

import java.io.IOException;

public class LoadVesselException extends IOException {

    private final String reason;

    public LoadVesselException(String reason) {
        super(reason);
        this.reason = reason;
    }

    public String getReason() {
        return this.reason;
    }
}
