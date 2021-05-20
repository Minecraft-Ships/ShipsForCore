package org.ships.vessel.common.loader;

import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;

import java.io.IOException;
import java.util.Optional;

public class ShipsLicenceSignFinder implements ShipsLoader {

    protected SignTileEntity ste;

    public ShipsLicenceSignFinder(SyncBlockPosition position) throws IOException {
        Optional<LiveTileEntity> opEntity = position.getTileEntity();
        if (!opEntity.isPresent()) {
            throw new IOException("Block is not a sign");
        }
        if (!(opEntity.get() instanceof LiveSignTileEntity)) {
            throw new IOException("Block is not a sign");
        }
        ste = (LiveSignTileEntity) opEntity.get();
    }

    public ShipsLicenceSignFinder(SignTileEntity ste) {
        this.ste = ste;
    }

    @Override
    public Vessel load() throws LoadVesselException {
        LicenceSign ls = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
        if (!ls.isSign(ste)) {
            throw new LoadVesselException("Unable to read sign");
        }
        String typeS = ste.getLine(1).get().toPlain();
        Optional<ShipType> opType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(st -> st.getDisplayName().equalsIgnoreCase(typeS)).findAny();
        if (!opType.isPresent()) {
            throw new LoadVesselException("Unable to find shiptype of " + typeS);
        }
        String name = ste.getLine(2).get().toPlain().toLowerCase();
        String id = "ships:" + opType.get().getName().toLowerCase() + "." + name;
        return new ShipsIDFinder(id).load();
    }
}
