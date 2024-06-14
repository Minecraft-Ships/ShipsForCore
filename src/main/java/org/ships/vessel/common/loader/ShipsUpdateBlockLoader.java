package org.ships.vessel.common.loader;

import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Deprecated(forRemoval = true)
public class ShipsUpdateBlockLoader {

    protected final SyncBlockPosition original;

    public ShipsUpdateBlockLoader(SyncBlockPosition position) {
        this.original = position;
    }

    public CompletableFuture<Optional<Vessel>> loadOvertime(Consumer<? super LoadVesselException> ex) {
        CompletableFuture<Optional<Vessel>> opVesselFuture = ShipsPlugin
                .getPlugin()
                .getConfig()
                .getDefaultFinder()
                .init()
                .getConnectedBlocksOvertime(this.original,
                                            (currentStructure, block) -> OvertimeBlockFinderUpdate.BlockFindControl.USE)
                .thenApply(structure -> {
                    try {
                        return Optional.of(ShipsUpdateBlockLoader.this.load(structure));
                    } catch (LoadVesselException e) {
                        ex.accept(e);
                        return Optional.empty();
                    }
                });
        return opVesselFuture.thenCompose(opVessel -> {
            if (opVessel.isPresent() && opVessel.get() instanceof WaterType) {
                return opVessel.get().getStructure().fillAir().thenApply(structure -> opVessel);
            }
            return CompletableFuture.completedFuture(opVessel);
        });
    }

    @Deprecated(forRemoval = true)
    public void loadOvertime(Consumer<? super Vessel> consumer, Consumer<? super LoadVesselException> ex) {
        loadOvertime(ex).thenAccept(opVessel -> opVessel.ifPresent(consumer::accept));
    }

    private Vessel load(PositionableShipsStructure blocks) throws LoadVesselException {
        LicenceSign ls = ShipsSigns.LICENCE;
        Optional<SyncBlockPosition> opBlock = blocks.getAll(SignTileEntity.class).stream().filter(b -> {
            SignTileEntity lste = (SignTileEntity) b
                    .getTileEntity()
                    .orElseThrow(() -> new IllegalStateException("Could not get tile entity"));
            return ls.isSign(lste);
        }).findAny();
        if (opBlock.isEmpty()) {
            throw new UnableToFindLicenceSign(blocks, "Failed to find licence sign");
        }
        SyncBlockPosition block = opBlock.get();
        Vessel vessel = ShipsSignVesselFinder.find((SignTileEntity) opBlock
                .get()
                .getTileEntity()
                .orElseThrow(() -> new IllegalStateException("Could not get tile entity")));
        PositionableShipsStructure apss = new AbstractPositionableShipsStructure(block);
        blocks.getSyncedPositionsRelativeToWorld().forEach(apss::addPositionRelativeToWorld);
        vessel.setStructure(apss);
        return vessel;
    }
}
