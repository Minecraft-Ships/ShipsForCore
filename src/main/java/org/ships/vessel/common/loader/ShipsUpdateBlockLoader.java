package org.ships.vessel.common.loader;

import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;
import java.util.function.Consumer;

public class ShipsUpdateBlockLoader {

    protected SyncBlockPosition original;

    public ShipsUpdateBlockLoader(SyncBlockPosition position) {
        this.original = position;
    }

    public void loadOvertime(Consumer<Vessel> consumer, Consumer<LoadVesselException> ex) {
        ShipsPlugin
                .getPlugin()
                .getConfig()
                .getDefaultFinder()
                .init()
                .getConnectedBlocksOvertime(this.original, new OvertimeBlockFinderUpdate() {
                    @Override
                    public void onShipsStructureUpdated(PositionableShipsStructure structure) {
                        try {
                            Vessel vessel = load(structure);
                            consumer.accept(vessel);
                        } catch (LoadVesselException e) {
                            ex.accept(e);
                        }
                    }

                    @Override
                    public BlockFindControl onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                        return BlockFindControl.USE;
                    }
                });
    }

    private Vessel load(PositionableShipsStructure blocks) throws LoadVesselException {
        LicenceSign ls = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
        Optional<SyncBlockPosition> opBlock = blocks.getAll(SignTileEntity.class).stream().filter(b -> {
            LiveSignTileEntity lste = (LiveSignTileEntity) b.getTileEntity().get();
            if (!ls.isSign(lste)) {
                return false;
            }
            return true;
        }).findAny();
        if (!opBlock.isPresent()) {
            throw new UnableToFindLicenceSign(blocks, "Failed to find licence sign");
        }
        SyncBlockPosition block = opBlock.get();
        Vessel vessel = new ShipsLicenceSignFinder((LiveSignTileEntity) opBlock.get().getTileEntity().get()).load();
        AbstractPosititionableShipsStructure apss = new AbstractPosititionableShipsStructure(block);
        blocks.getPositions().forEach(b -> apss.addPosition(b));
        vessel.setStructure(apss);
        return vessel;
    }
}
