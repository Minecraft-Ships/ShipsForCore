package org.ships.vessel.common.assits;

import org.ships.vessel.common.types.Vessel;


public interface Fallable extends Vessel {

    boolean shouldFall();
}
