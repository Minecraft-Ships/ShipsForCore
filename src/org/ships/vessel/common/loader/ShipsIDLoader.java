package org.ships.vessel.common.loader;

import org.ships.vessel.common.types.Vessel;

public class ShipsIDLoader implements ShipsLoader {

    protected String id;

    public ShipsIDLoader(String id){
        this.id = id;
    }

    @Override
    public Vessel load() {
        return null;
    }
}
