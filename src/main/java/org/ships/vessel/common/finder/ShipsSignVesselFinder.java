package org.ships.vessel.common.finder;

import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;

import java.util.Optional;

public class ShipsSignVesselFinder {

    public static Vessel find(SignTileEntity signTileEntity) throws LoadVesselException {
        LicenceSign ls = ShipsPlugin
                .getPlugin()
                .get(LicenceSign.class)
                .orElseThrow(() -> new RuntimeException("Could not find licence sign. is it registered?"));

        if (!ls.isSign(signTileEntity)) {
            throw new LoadVesselException("Unable to read sign");
        }

        String typeS = signTileEntity.getTextAt(1).orElseThrow(() -> new RuntimeException("You broke logic")).toPlain();
        Optional<ShipType<?>> opType = ShipsPlugin
                .getPlugin()
                .getAllShipTypes()
                .stream()
                .filter(st -> st.getDisplayName().equalsIgnoreCase(typeS))
                .findAny();
        if (opType.isEmpty()) {
            throw new LoadVesselException("Unable to find shiptype of " + typeS);
        }

        String name = signTileEntity.getTextAt(2).get().toPlain().toLowerCase();
        String id = "ships:" + opType.get().getName().toLowerCase() + "." + name;
        return IdVesselFinder.load(id);
    }

}
