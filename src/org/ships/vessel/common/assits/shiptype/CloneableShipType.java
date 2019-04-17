package org.ships.vessel.common.assits.shiptype;

import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public interface CloneableShipType<V extends Vessel> extends ShipType {

    <T extends Vessel> CloneableShipType<T> clone(File root, String display, Class<T> vesselClass);
    Class<V> getVesselClass();

    default CloneableShipType<V> clone(File root, String display){
        return clone(root, display, getVesselClass());
    }

    @Override
    default V createNewVessel(SignTileEntity ste, BlockPosition position){
        try {
            return getVesselClass()
                    .getConstructor(SignTileEntity.class, BlockPosition.class, ShipType.class)
                    .newInstance(ste, position, this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}
