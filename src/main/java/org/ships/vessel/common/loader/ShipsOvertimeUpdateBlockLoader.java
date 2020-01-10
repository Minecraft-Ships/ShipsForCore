package org.ships.vessel.common.loader;

import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;

public abstract class ShipsOvertimeUpdateBlockLoader extends ShipsUpdateBlockLoader {

    private class OvertimeRunnable implements OvertimeBlockFinderUpdate{

        @Override
        public void onShipsStructureUpdated(PositionableShipsStructure structure) {
            LicenceSign ls = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
            Optional<BlockPosition> opBlock = structure.getAll(SignTileEntity.class).stream().filter(b -> {
                LiveSignTileEntity lste = (LiveSignTileEntity) b.getTileEntity().get();
                if (!ls.isSign(lste)){
                    return false;
                }
                return true;
            }).findAny();
            if(!opBlock.isPresent()){
                ShipsOvertimeUpdateBlockLoader.this.onExceptionThrown(new UnableToFindLicenceSign(structure, "Failed to find licence sign"));
                return;
            }
            PositionableShipsStructure structure2 = new AbstractPosititionableShipsStructure(opBlock.get());
            structure.getPositions().forEach(b -> structure2.addPosition(b));
            try {
                Vessel vessel = new ShipsLicenceSignFinder((LiveSignTileEntity) structure2.getPosition().getTileEntity().get()).load();
                vessel.setStructure(structure2);
                onStructureUpdate(vessel);
            } catch (LoadVesselException e) {
                ShipsOvertimeUpdateBlockLoader.this.onExceptionThrown(e);
                return;
            }
        }

        @Override
        public boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
            return ShipsOvertimeUpdateBlockLoader.this.onBlockFind(currentStructure, block);
        }
    }

    protected abstract void onStructureUpdate(Vessel vessel);
    protected abstract boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block);
    protected abstract void onExceptionThrown(LoadVesselException e);

    public ShipsOvertimeUpdateBlockLoader(BlockPosition position) {
        super(position);
    }

    public void loadOvertime() {
        BasicBlockFinder finder = ShipsPlugin.getPlugin().getConfig().getDefaultFinder();
        /*try {
            Vessel vessel = new ShipsBlockFinder(this.original).load();
            finder.setConnectedVessel(vessel);
        } catch (LoadVesselException e) {

        }*/
        finder.getConnectedBlocksOvertime(this.original, new OvertimeRunnable());
    }
}
