package org.ships.vessel.common.assits;

import org.ships.vessel.common.types.Vessel;

import java.io.IOException;

public interface WritableNameVessel extends Vessel {

    /**
     *
     * @param name the new name
     * @return itself for chaining
     * @throws IOException if there is a issue writing the name, an IOException will be thrown
     */
    Vessel setName(String name) throws IOException;
}
