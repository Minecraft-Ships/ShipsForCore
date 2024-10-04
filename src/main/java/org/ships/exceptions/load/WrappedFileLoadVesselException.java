package org.ships.exceptions.load;

import org.ships.plugin.ShipsPlugin;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;

public class WrappedFileLoadVesselException extends FileLoadVesselException {

    private final Throwable throwable;

    public WrappedFileLoadVesselException(File file, Throwable throwable) {
        super(file, throwable.getMessage());
        this.throwable = throwable;
    }

    @Override
    public void printStackTrace() {
        ShipsPlugin.getPlugin().getLogger().error("An error occurred in file: " + this.getFile().getPath());
        this.throwable.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream stream) {
        ShipsPlugin.getPlugin().getLogger().error("An error occurred in file: " + this.getFile().getPath());
        this.throwable.printStackTrace(stream);
    }

    @Override
    public void printStackTrace(PrintWriter writer) {
        ShipsPlugin.getPlugin().getLogger().error("An error occurred in file: " + this.getFile().getPath());
        this.throwable.printStackTrace(writer);
    }
}
