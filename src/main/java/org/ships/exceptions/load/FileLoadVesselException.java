package org.ships.exceptions.load;

import java.io.File;

public class FileLoadVesselException extends LoadVesselException {

    private final File file;

    public FileLoadVesselException(File file, String reason) {
        super(reason + ": " + file.getPath());
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }
}
