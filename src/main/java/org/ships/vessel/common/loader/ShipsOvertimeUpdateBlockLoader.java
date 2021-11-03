package org.ships.vessel.common.loader;

import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.AbstractPositionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;

public abstract class ShipsOvertimeUpdateBlockLoader extends ShipsUpdateBlockLoader {

    private class OvertimeRunnable implements OvertimeBlockFinderUpdate {

        @Override
        public void onShipsStructureUpdated(PositionableShipsStructure structure) {
            LicenceSign ls =
                    ShipsPlugin.getPlugin().get(LicenceSign.class).orElseThrow(() -> new IllegalStateException("Could" +
                            " not get licence"));
            Optional<SyncBlockPosition> opBlock = structure.getAll(SignTileEntity.class).stream().filter(b -> {
                SignTileEntity lste = (SignTileEntity) b.getTileEntity().orElseThrow(() -> new IllegalStateException(
                        "Could not get tile entity"));
                return ls.isSign(lste);
            }).findAny();
            if (!opBlock.isPresent()) {
                ShipsOvertimeUpdateBlockLoader.this.onExceptionThrown(new UnableToFindLicenceSign(structure, "Failed to find licence sign"));
                return;
            }
            PositionableShipsStructure structure2 = new AbstractPositionableShipsStructure(opBlock.get());
            structure.getPositions().forEach(structure2::addPosition);
            try {
                Vessel vessel =
                        new ShipsLicenceSignFinder((SignTileEntity) structure2.getPosition().getTileEntity().orElseThrow(() -> new IllegalStateException("Could not get tile entity"))).load();
                vessel.setStructure(structure2);
                ShipsOvertimeUpdateBlockLoader.this.onStructureUpdate(vessel);
            } catch (LoadVesselException e) {
                ShipsOvertimeUpdateBlockLoader.this.onExceptionThrown(e);
            }
        }

        @Override
        public BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure, @NotNull BlockPosition block) {
            return ShipsOvertimeUpdateBlockLoader.this.onBlockFind(currentStructure, block);
        }
    }

    protected abstract void onStructureUpdate(Vessel vessel);

    protected abstract OvertimeBlockFinderUpdate.BlockFindControl onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block);

    protected abstract void onExceptionThrown(LoadVesselException e);

    public ShipsOvertimeUpdateBlockLoader(SyncBlockPosition position) {
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
