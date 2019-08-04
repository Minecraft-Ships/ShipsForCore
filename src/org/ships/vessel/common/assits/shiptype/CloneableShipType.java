package org.ships.vessel.common.assits.shiptype;

import org.core.CorePlugin;
import org.ships.vessel.common.types.ShipType;

import java.io.File;
import java.util.regex.Pattern;

public interface CloneableShipType extends ShipType {

    CloneableShipType cloneWithName(File file, String name);

    CloneableShipType getOriginType();

    default CloneableShipType cloneWithName(File file){
        if(!file.getName().contains(".")){
            return cloneWithName(file, file.getName().replaceAll(" ", "_"));
        }
        String[] nameArray = file.getName().split(Pattern.quote("."));
        String[] array = CorePlugin.strip(String.class, 0, nameArray.length - 1, nameArray);
        return cloneWithName(file, CorePlugin.toString("_", array).replaceAll(" ", "_"));
    }
}
