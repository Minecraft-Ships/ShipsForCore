package org.ships.vessel.common.loader;

import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncPosition;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.finder.IdVesselFinder;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;

import java.io.IOException;
import java.util.Optional;

@Deprecated(forRemoval = true)
public class ShipsLicenceSignFinder implements ShipsLoader {

    protected SignTileEntity ste;

    public ShipsLicenceSignFinder(SyncPosition<Integer> position) throws IOException {
        Optional<LiveTileEntity> opEntity = position.getTileEntity();
        if (opEntity.isEmpty()) {
            throw new IOException("Block is not a sign");
        }
        if (!(opEntity.get() instanceof LiveSignTileEntity)) {
            throw new IOException("Block is not a sign");
        }
        this.ste = (SignTileEntity) opEntity.get();
    }

    public ShipsLicenceSignFinder(SignTileEntity ste) {
        this.ste = ste;
    }

    @Override
    public Vessel load() throws LoadVesselException {
        LicenceSign ls = ShipsPlugin
                .getPlugin()
                .get(LicenceSign.class)
                .orElseThrow(() -> new RuntimeException("Could not find licence sign. is it registered?"));
        if (!ls.isSign(this.ste)) {
            throw new LoadVesselException("Unable to read sign");
        }
        String typeS = this.ste.getTextAt(1).orElseThrow(() -> new RuntimeException("You broke logic")).toPlain();
        Optional<ShipType<?>> opType = ShipsPlugin
                .getPlugin()
                .getAllShipTypes()
                .stream()
                .filter(st -> st.getDisplayName().equalsIgnoreCase(typeS))
                .findAny();
        if (opType.isEmpty()) {
            throw new LoadVesselException("Unable to find shiptype of " + typeS);
        }
        String name = this.ste.getTextAt(2).get().toPlain().toLowerCase();
        String id = "ships:" + opType.get().getName().toLowerCase() + "." + name;
        return IdVesselFinder.load(id);
    }
}
