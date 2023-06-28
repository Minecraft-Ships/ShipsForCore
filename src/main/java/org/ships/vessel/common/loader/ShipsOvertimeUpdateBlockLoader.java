package org.ships.vessel.common.loader;

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
                                                                              BlockPosition block);

    protected abstract void onExceptionThrown(LoadVesselException e);

    public CompletableFuture<Optional<Vessel>> loadOvertime() {
        if (!this.update) {
            Collection<Vessel> vessels = ShipsPlugin.getPlugin().getVessels();
            Optional<Vessel> opVessel = vessels
                    .parallelStream()
                    .filter(vessel -> vessel.getPosition().getWorld().equals(this.original.getWorld()))
                    .filter(v -> {
                        Collection<ASyncBlockPosition> positions = v
                                .getStructure()
                                .getPositionsRelativeTo(Position.toASync(this.original));
                        return positions
                                .parallelStream()
                                .anyMatch(position -> position.getPosition().equals(this.original.getPosition()));
                    })
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
        CompletableFuture<Optional<Vessel>> opVesselFuture = finder
                .getConnectedBlocksOvertime(this.original, ShipsOvertimeUpdateBlockLoader.this::onBlockFind)
                .thenApply(structure -> {
                    LicenceSign ls = ShipsPlugin
                            .getPlugin()
                            .get(LicenceSign.class)
                            .orElseThrow(() -> new IllegalStateException("Could" + " not get licence"));
                    Optional<SyncBlockPosition> opBlock = structure.getAll(SignTileEntity.class).stream().filter(b -> {
                        SignTileEntity lste = (SignTileEntity) b
                                .getTileEntity()
                                .orElseThrow(() -> new IllegalStateException("Could not get tile entity"));
                        return ls.isSign(lste);
                    }).findAny();
                    if (opBlock.isEmpty()) {
                        ShipsOvertimeUpdateBlockLoader.this.onExceptionThrown(
                                new UnableToFindLicenceSign(structure, "Failed to find licence sign"));
                        return Optional.empty();
                    }
                    PositionableShipsStructure structure2 = new AbstractPositionableShipsStructure(opBlock.get());
                    structure.getSyncedPositionsRelativeToWorld().forEach(structure2::addPositionRelativeToWorld);
                    try {
                        LiveTileEntity tileEntity = structure2
                                .getPosition()
                                .getTileEntity()
                                .orElseThrow(() -> new IllegalStateException("Could not get tile entity"));
                        Vessel vessel = ShipsSignVesselFinder.find((SignTileEntity) tileEntity);
                        vessel.setStructure(structure2);
                        ShipsOvertimeUpdateBlockLoader.this.onStructureUpdate(vessel);
                        return Optional.of(vessel);
                    } catch (LoadVesselException e) {
                        ShipsOvertimeUpdateBlockLoader.this.onExceptionThrown(e);
                        return Optional.empty();
                    }
                });
        return opVesselFuture.thenCompose(opVessel -> {
            if (opVessel.isPresent()) {
                if (opVessel.get() instanceof WaterType) {
                    return opVessel.get().getStructure().fillAir().thenApply(pss -> opVessel);
                }
            }
            return CompletableFuture.completedFuture(opVessel);
        });
    }
}
