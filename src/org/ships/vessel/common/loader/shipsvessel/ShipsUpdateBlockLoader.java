package org.ships.vessel.common.loader.shipsvessel;

import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsLoader;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;

public class ShipsUpdateBlockLoader implements ShipsLoader {

    protected BlockPosition original;

    public ShipsUpdateBlockLoader(BlockPosition position){
        this.original = position;
    }

    @Override
    public Vessel load() throws LoadVesselException {
        PositionableShipsStructure blocks = ShipsPlugin.getPlugin().getConfig().getDefaultFinder().getConnectedBlocks(this.original);
        LicenceSign ls = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
        Optional<BlockPosition> opBlock = blocks.getAll(SignTileEntity.class).stream().filter(b -> {
            LiveSignTileEntity lste = (LiveSignTileEntity) b.getTileEntity().get();
            if (!ls.isSign(lste)){
                return false;
            }
            return true;
        }).findAny();
        if(!opBlock.isPresent()){
            throw new UnableToFindLicenceSign(blocks, "Failed to find licence sign");
        }
        BlockPosition block = opBlock.get();
        ShipsVessel vessel = new ShipsLicenceSignLoader((LiveSignTileEntity) opBlock.get().getTileEntity().get()).load();
        AbstractPosititionableShipsStructure apss = new AbstractPosititionableShipsStructure(block);
        blocks.getPositions().stream().forEach(b -> apss.addPosition(b));
        vessel.setStructure(apss);
        return vessel;
    }
}
