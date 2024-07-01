package org.ships.vessel.common.loader;

import org.core.vector.type.Vector3;
import org.core.world.position.block.entity.LiveTileEntity;
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
import org.ships.vessel.common.assits.WaterType;
import org.ships.vessel.common.finder.ShipsSignVesselFinder;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.sign.ShipsSigns;
import org.ships.vessel.structure.AbstractPositionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Deprecated(forRemoval = true)
public abstract class ShipsOvertimeUpdateBlockLoader extends ShipsUpdateBlockLoader {

    private final boolean update;

    public ShipsOvertimeUpdateBlockLoader(@NotNull SyncBlockPosition position, boolean update) {
        super(position);
        this.update = update;
    }

    protected abstract void onStructureUpdate(Vessel vessel);

    protected abstract OvertimeBlockFinderUpdate.BlockFindControl onBlockFind(PositionableShipsStructure currentStructure,
                                                                              Vector3<Integer> block);

    protected abstract void onExceptionThrown(LoadVesselException e);

    public CompletableFuture<Optional<Vessel>> loadOvertime() {
        if (!this.update) {
            Collection<Vessel> vessels = ShipsPlugin.getPlugin().getVessels();
            Optional<Vessel> opVessel = vessels
                    .parallelStream()
                    .filter(vessel -> vessel.getPosition().getWorld().equals(this.original.getWorld()))
                    .filter(v -> v
                            .getStructure()
                            .getVectorsRelativeTo(this.original.getPosition()).anyMatch(position -> position.equals(this.original.getPosition())))
                    .findAny();
            if (opVessel.isEmpty()) {
                this.onExceptionThrown(
                        new UnableToFindLicenceSign(new AbstractPositionableShipsStructure(this.original),
                                                    "no ship found at position"));
                return CompletableFuture.completedFuture(Optional.empty());
            }
            this.onStructureUpdate(opVessel.get());
            return CompletableFuture.completedFuture(opVessel);
        }


        BasicBlockFinder finder = ShipsPlugin.getPlugin().getConfig().getDefaultFinder();
        return finder
                .getConnectedBlocksOvertime(this.original, this::onBlockFind)
                .thenApply(structure -> {
                    LicenceSign ls = ShipsSigns.LICENCE;
                    Optional<SyncBlockPosition> opBlock = structure
                            .getRelativeToWorld(ls)
                            .findAny()
                            .map(LiveTileEntity::getPosition);
                    if (opBlock.isEmpty()) {
                        this.onExceptionThrown(
                                new UnableToFindLicenceSign(structure, "Failed to find licence sign"));
                        return Optional.empty();
                    }

                    PositionableShipsStructure structure2 = new AbstractPositionableShipsStructure(opBlock.get());
                    structure2.copyFrom(structure);

                    LiveTileEntity tileEntity = structure2
                            .getPosition()
                            .getTileEntity()
                            .orElseThrow(() -> new IllegalStateException("Could not get tile entity"));
                    try {
                        Vessel vessel = ShipsSignVesselFinder.find((SignTileEntity) tileEntity);
                        vessel.setStructure(structure2);

                        this.onStructureUpdate(vessel);
                        return Optional.of(vessel);
                    } catch (LoadVesselException e) {
                        this.onExceptionThrown(e);
                        return Optional.empty();
                    }
                });
    }
}
