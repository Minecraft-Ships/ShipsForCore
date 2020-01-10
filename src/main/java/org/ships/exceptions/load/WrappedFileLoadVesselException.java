package org.ships.exceptions.load;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;

public class WrappedFileLoadVesselException extends FileLoadVesselException {

    private Throwable throwable;

    public WrappedFileLoadVesselException(File file, Throwable throwable) {
        super(file, throwable.getMessage());
        this.throwable = throwable;
    }

    @Override
    public void printStackTrace(){
        System.err.println("An error occurred in file: " + getFile().getPath());
        this.throwable.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream stream){
        System.err.println("An error occurred in file: " + getFile().getPath());
        this.throwable.printStackTrace(stream);
    }

    @Override
    public void printStackTrace(PrintWriter writer){
        System.err.println("An error occurred in file: " + getFile().getPath());
        this.throwable.printStackTrace(writer);
    }
}
