package org.ships.vessel.common.assits.shiptype;

import org.array.utils.ArrayUtils;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.util.regex.Pattern;

public interface CloneableShipType<T extends Vessel> extends ShipType<T> {

    CloneableShipType<T> cloneWithName(File file, String name);

    CloneableShipType<T> getOriginType();

    default CloneableShipType<T> cloneWithName(File file){
        if(!file.getName().contains(".")){
            return cloneWithName(file, file.getName().replaceAll(" ", "_"));
        }
        String[] nameArray = file.getName().split(Pattern.quote("."));
        String[] array = ArrayUtils.filter(0, nameArray.length - 1, nameArray);
        return cloneWithName(file, ArrayUtils.toString("_", t -> t, array).replaceAll(" ", "_"));
    }
}
