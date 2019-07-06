package org.ships.vessel.common.loader.legacy;

import org.core.text.Text;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.shiptype.ClassicShipType;
import org.ships.vessel.common.loader.ShipsLoader;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public class LegacyShipIDLoader implements ShipsLoader {

    protected BlockPosition position;

    public LegacyShipIDLoader(BlockPosition position){
        this.position = position;
    }

    @Override
    public Vessel load() throws LoadVesselException {
        Optional<LiveTileEntity> opTile = this.position.getTileEntity();
        if(!opTile.isPresent()){
            throw new LoadVesselException("Position is not a sign");
        }
        LiveTileEntity tile = opTile.get();
        if(!(tile instanceof LiveSignTileEntity)){
            throw new LoadVesselException("Position is not a sign");
        }
        LiveSignTileEntity lste = (LiveSignTileEntity)tile;
        Optional<Text> opName = lste.getLine(2);
        Optional<Text> opTypeS = lste.getLine(1);
        if(!opName.isPresent()){
            throw new LoadVesselException("Position is not a Licence Sign");
        }
        if(!opTypeS.isPresent()){
            throw new LoadVesselException("Position is not a Licence Sign");
        }
        Optional<ClassicShipType> opType = ShipsPlugin.getPlugin().getAll(ClassicShipType.class).stream().filter(st -> st.getName().equalsIgnoreCase(opTypeS.get().toPlain())).findAny();
        if(!opType.isPresent()){
            throw new LoadVesselException(opTypeS.get().toPlain() + " is not a classic ship");
        }
        return opType.get().createClassicVessel(lste);
    }
}
