package org.ships.vessel.common.loader;

import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.AbstractPositionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;
import java.util.function.Consumer;

public class ShipsUpdateBlockLoader {

    protected final SyncBlockPosition original;

    public ShipsUpdateBlockLoader(SyncBlockPosition position) {
        this.original = position;
    }

    public void loadOvertime(Consumer<? super Vessel> consumer, Consumer<? super LoadVesselException> ex) {
        ShipsPlugin
                .getPlugin()
                .getConfig()
                .getDefaultFinder()
                .init()
                .getConnectedBlocksOvertime(this.original, new OvertimeBlockFinderUpdate() {
                    @Override
                    public void onShipsStructureUpdated(@NotNull PositionableShipsStructure structure) {
                        try {
                            Vessel vessel = ShipsUpdateBlockLoader.this.load(structure);
                            consumer.accept(vessel);
                        } catch (LoadVesselException e) {
                            ex.accept(e);
                        }
                    }

                    @Override
                    public BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure, @NotNull BlockPosition block) {
                        return BlockFindControl.USE;
                    }
                });
    }

    private Vessel load(PositionableShipsStructure blocks) throws LoadVesselException {
        LicenceSign ls = ShipsPlugin.getPlugin().get(LicenceSign.class).orElseThrow(() -> new IllegalStateException(
                "Could not fince licence sign"));
        Optional<SyncBlockPosition> opBlock = blocks.getAll(SignTileEntity.class).stream().filter(b -> {
            SignTileEntity lste = (SignTileEntity) b.getTileEntity().orElseThrow(() -> new IllegalStateException(
                    "Could not get tile entity"));
            return ls.isSign(lste);
        }).findAny();
        if (!opBlock.isPresent()) {
            throw new UnableToFindLicenceSign(blocks, "Failed to find licence sign");
        }
        SyncBlockPosition block = opBlock.get();
        Vessel vessel =
                new ShipsLicenceSignFinder((SignTileEntity) opBlock.get().getTileEntity().orElseThrow(() -> new IllegalStateException("Could not get tile entity"))).load();
        PositionableShipsStructure apss = new AbstractPositionableShipsStructure(block);
        blocks.getPositions().forEach(apss::addPosition);
        vessel.setStructure(apss);
        return vessel;
    }
}
