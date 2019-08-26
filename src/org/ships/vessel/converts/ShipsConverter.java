package org.ships.vessel.converts;

import org.core.utils.Identifable;

import java.io.File;
import java.io.IOException;

public interface ShipsConverter<T extends Identifable> extends Identifable {

    File getFolder();
    T convert(File file) throws IOException;
}
