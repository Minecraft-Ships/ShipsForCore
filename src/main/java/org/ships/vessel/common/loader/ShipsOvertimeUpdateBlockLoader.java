package org.ships.vessel.common.loader;

import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public abstract class ShipsOvertimeUpdateBlockLoader extends ShipsUpdateBlockLoader {

    private class OvertimeRunnable implements OvertimeBlockFinderUpdate {

        @Override
        public void onShipsStructureUpdated(@NotNull PositionableShipsStructure structure) {
            LicenceSign ls =
                    ShipsPlugin.getPlugin().get(LicenceSign.class).orElseThrow(() -> new IllegalStateException("Could" +
                            " not get licence"));
            Optional<SyncBlockPosition> opBlock = structure.getAll(SignTileEntity.class).stream().filter(b -> {
                SignTileEntity lste = (SignTileEntity) b.getTileEntity().orElseThrow(() -> new IllegalStateException(
                        "Could not get tile entity"));
                return ls.isSign(lste);
            }).findAny();
            if (opBlock.isEmpty()) {
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

    private final boolean update;

    public ShipsOvertimeUpdateBlockLoader(@NotNull SyncBlockPosition position, boolean update) {
        super(position);
        this.update = update;
    }

    public void loadOvertime() {
        if (!this.update) {
            Set<Vessel> vessels = ShipsPlugin.getPlugin().getVessels();
            Optional<Vessel> opVessel = vessels
                    .parallelStream()
                    .filter(vessel -> vessel
                            .getPosition()
                            .getWorld()
                            .equals(this.original.getWorld()))
                    .filter(v -> {
                        Collection<ASyncBlockPosition> positions = v.getStructure().getPositionsRelativeTo(Position.toASync(this.original));
                        return positions.parallelStream().anyMatch(position -> position.getPosition().equals(this.original.getPosition()));
                    }).findAny();
            if (opVessel.isEmpty()) {
                this.onExceptionThrown(new UnableToFindLicenceSign(new AbstractPositionableShipsStructure(this.original), "no ship found at position"));
                return;
            }
            this.onStructureUpdate(opVessel.get());
            return;
        }


        BasicBlockFinder finder = ShipsPlugin.getPlugin().getConfig().getDefaultFinder();
        finder.getConnectedBlocksOvertime(this.original, new OvertimeRunnable());
    }
}
