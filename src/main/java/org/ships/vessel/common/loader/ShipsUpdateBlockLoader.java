package org.ships.vessel.common.loader;

import org.core.world.position.block.entity.sign.LiveSignTileEntity;
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
        return opVesselFuture;
    }

    @Deprecated(forRemoval = true)
    public void loadOvertime(Consumer<? super Vessel> consumer, Consumer<? super LoadVesselException> ex) {
        this.loadOvertime(ex).thenAccept(opVessel -> opVessel.ifPresent(consumer::accept));
    }

    private Vessel load(PositionableShipsStructure blocks) throws LoadVesselException {
        LicenceSign ls = ShipsSigns.LICENCE;
        Optional<LiveSignTileEntity> opBlock = blocks.getRelativeToWorld(ls).findAny();
        if (opBlock.isEmpty()) {
            throw new UnableToFindLicenceSign(blocks, "Failed to find licence sign");
        }
        LiveSignTileEntity block = opBlock.get();
        Vessel vessel = ShipsSignVesselFinder.find(opBlock.get());
        PositionableShipsStructure apss = new AbstractPositionableShipsStructure(block.getPosition());
        blocks.getPositionsRelativeToWorld().forEach(apss::addPositionRelativeToWorld);
        vessel.setStructure(apss);
        return vessel;
    }
}
