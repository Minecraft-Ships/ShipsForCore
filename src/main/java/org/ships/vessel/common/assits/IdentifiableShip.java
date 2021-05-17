package org.ships.vessel.common.assits;

import org.ships.exceptions.NoLicencePresent;
import org.ships.vessel.common.types.Vessel;

public interface IdentifiableShip extends Vessel {

    String getId() throws NoLicencePresent;
}
