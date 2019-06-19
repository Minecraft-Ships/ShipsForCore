package org.ships.exceptions.load;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class LoadVesselException extends IOException {

    private final File file;
    private final String reason;

    public LoadVesselException(File file, String reason){
        super(reason);
        this.file = file;
        this.reason = reason;
    }

    public String getReason(){
        return this.reason;
    }

    public Optional<File> getFile(){
        return Optional.ofNullable(this.file);
    }
}
