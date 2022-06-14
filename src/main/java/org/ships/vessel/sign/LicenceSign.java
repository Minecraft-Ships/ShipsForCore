package org.ships.vessel.sign;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.config.ConfigurationStream;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.source.viewer.CommandViewer;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.algorthum.blockfinder.FindAirOvertimeBlockFinderUpdate;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.commands.argument.ship.info.ShipsShipInfoArgumentCommand;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.loader.ShipsLicenceSignFinder;
import org.ships.vessel.common.loader.shipsvessel.ShipsFileLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LicenceSign implements ShipsSign {

    private static class VesselStructureUpdate implements OvertimeBlockFinderUpdate {

        private final @Nullable ServerBossBar finalBar;
        private final int totalBlockCount;
        private final @NotNull CommandViewer messager;
        private final @NotNull Vessel vessel;

        private VesselStructureUpdate(@NotNull Vessel vessel, int totalBlockCount, @NotNull CommandViewer messager,
                                      @Nullable ServerBossBar bossBar) {
            this.messager = messager;
            this.vessel = vessel;
            this.finalBar = bossBar;
            this.totalBlockCount = totalBlockCount;
        }

        @Override
        public void onShipsStructureUpdated(@NotNull PositionableShipsStructure structure) {
            int originalSize = structure.getOriginalRelativePositions().size();
            this.vessel.setStructure(structure);
            this.vessel.save();
            this.messager.sendMessage(AText.ofPlain("Vessel structure has updated by " + (structure.getOriginalRelativePositions().size() - originalSize)));
            if (this.finalBar!=null) {
                this.finalBar.deregisterPlayers();
            }
        }

        @Override
        public OvertimeBlockFinderUpdate.BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure, @NotNull BlockPosition block) {
            if (this.finalBar!=null) {
                int blockCount = currentStructure.getOriginalRelativePositions().size() + 1;
                this.finalBar.setTitle(AText.ofPlain(blockCount + "/" + this.totalBlockCount));
                try {
                    this.finalBar.setValue(blockCount, this.totalBlockCount);
                } catch (IllegalArgumentException ignore) {

                }
            }
            return OvertimeBlockFinderUpdate.BlockFindControl.USE;
        }
    }


    public Optional<Vessel> getShip(SignTileEntity entity) {
        if (!this.isSign(entity)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new ShipsLicenceSignFinder(entity).load());
        } catch (LoadVesselException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isSign(List<? extends AText> lines) {
        return lines.size() >= 1 && lines.get(0).toPlain().equalsIgnoreCase("[Ships]");
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException {
        SignTileEntitySnapshot snapshot = sign.getSnapshot();
        List<AText> lines = snapshot.getText();
        Optional<ShipType<?>> opType = ShipsPlugin.getPlugin().getAllShipTypes()
                .stream()
                .filter(t -> lines.get(1).toPlain().equalsIgnoreCase(t.getDisplayName()))
                .findFirst();
        if (opType.isEmpty()) {
            throw new IOException("Unknown Ship Type: Ship Types: " +
                    ShipsPlugin
                            .getPlugin()
                            .getAllShipTypes()
                            .stream()
                            .map(ShipType::getDisplayName)
                            .collect(Collectors.joining(", ")));
        }

        String name = lines.get(2).toPlain();
        if (name.replaceAll(" ", "").isEmpty()) {
            throw new IOException("Invalid name: Change 3rd line");
        }
        if (name.contains(":")) {
            name = name.replaceAll(":", "");
        }
        if (name.contains(" ")) {
            name = name.replaceAll(" ", "_");
        }
        name = (Character.toUpperCase(name.charAt(0))) + name.substring(1);
        snapshot.setTextAt(0, AText.ofPlain("[Ships]").withColour(NamedTextColours.YELLOW));
        snapshot.setTextAt(1, AText.ofPlain(opType.get().getDisplayName()).withColour(NamedTextColours.BLUE));
        snapshot.setTextAt(2, AText.ofPlain(name).withColour(NamedTextColours.GREEN));
        AText forth = ShipsPlugin
                .getPlugin()
                .getConfig()
                .getTextOnLicenceForthLine()
                .map(AText::ofLegacy)
                .orElseGet(() -> lines.get(3).withColour(NamedTextColours.GREEN));
        snapshot.setTextAt(3, forth);
        return snapshot;
    }

    private void displayInfo(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) throws IOException {
        Vessel s = new ShipsLicenceSignFinder(position).load();
        if (!player.isSneaking()) {
            if (s instanceof IdentifiableShip) {
                ShipsShipInfoArgumentCommand.displayInfo(player, s);
            }
        } else {
            ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
            ServerBossBar bar = null;
            int totalCount = config.getDefaultTrackSize();
            if (config.isBossBarVisible()) {
                bar = TranslateCore.createBossBar().register(player).setTitle(AText.ofPlain("0 / " + totalCount));
            }
            config
                    .getDefaultFinder()
                    .setConnectedVessel(s)
                    .getConnectedBlocksOvertime(
                            position,
                            new FindAirOvertimeBlockFinderUpdate(
                                    s, new VesselStructureUpdate(s, totalCount, player, bar)));
        }
    }

    @Override
    public boolean onPrimaryClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        try {
            this.displayInfo(player, position);
        } catch (UnableToFindLicenceSign e1) {
            Collection<? extends SyncBlockPosition> foundStructure = e1
                    .getFoundStructure()
                    .getPositions((Function<? super SyncBlockPosition, ? extends SyncBlockPosition>) s -> s);
            foundStructure.forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
            TranslateCore
                    .createSchedulerBuilder()
                    .setDelay(5)
                    .setDelayUnit(TimeUnit.SECONDS)
                    .setRunner((sched) -> foundStructure
                            .forEach(bp -> bp.resetBlock(player)))
                    .build(ShipsPlugin.getPlugin())
                    .run();
        } catch (IOException e) {
            Optional<LiveTileEntity> opTile = position.getTileEntity();
            if (opTile.isPresent()) {
                if (opTile.get() instanceof LiveSignTileEntity lste) {
                    String type = lste.getTextAt(1).map(AText::toPlain).orElse("");
                    String name = lste.getTextAt(2).map(AText::toPlain).orElse("");
                    Optional<ShipType<?>> opType = ShipsPlugin.getPlugin().getAllShipTypes()
                            .stream()
                            .filter(t -> t.getDisplayName().equalsIgnoreCase(type))
                            .findAny();
                    if (opType.isEmpty()) {
                        player.sendMessage(AText
                                .ofPlain("Could not find ShipType with display name of " + type)
                                .withColour(NamedTextColours.RED));
                        return false;
                    }
                    File file =
                            new File(TranslateCore.getPlatform().getPlatformConfigFolder(), "Ships" +
                                    "/VesselData/" + opType.get().getId().replaceAll(":",
                                    ".") + "/" + name + "." + TranslateCore.getPlatform().getConfigFormat().getFileType()[0]);
                    if (!file.exists()) {
                        player.sendMessage(AText.ofPlain("Could not find the file associated with the ship").withColour(NamedTextColours.RED));
                        return false;
                    }
                    ConfigurationStream.ConfigurationFile config = TranslateCore.createConfigurationFile(file, TranslateCore.getPlatform().getConfigFormat());
                    config.set(ShipsFileLoader.META_LOCATION_X, position.getX());
                    config.set(ShipsFileLoader.META_LOCATION_Y, position.getY());
                    config.set(ShipsFileLoader.META_LOCATION_Z, position.getZ());
                    config.set(ShipsFileLoader.META_LOCATION_WORLD, position.getWorld());
                    config.save();
                    try {
                        ShipsVessel vessel = new ShipsFileLoader(file).load();
                        ShipsPlugin.getPlugin().registerVessel(vessel);
                        player.sendMessage(AText.ofPlain("Ship has resynced"));
                    } catch (LoadVesselException loadVesselException) {
                        player.sendMessage(AText.ofPlain(loadVesselException.getReason()));
                        return false;
                    }
                    return true;
                }
            }
            player.sendMessage(AText.ofPlain(e.getMessage()).withColour(NamedTextColours.RED));
        }
        return false;
    }

    @Override
    public boolean onSecondClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        if (player.isSneaking()) {
            return this.onPrimaryClick(player, position);
        }
        return false;
    }

    @Override
    public String getId() {
        return "ships:licence_sign";
    }

    @Override
    public String getName() {
        return "Licence sign";
    }
}
