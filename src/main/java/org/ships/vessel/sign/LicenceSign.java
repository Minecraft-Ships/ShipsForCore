package org.ships.vessel.sign;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.config.ConfigurationStream;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.utils.ComponentUtils;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.commands.argument.ship.info.ShipsShipInfoArgumentCommand;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.finder.ShipsSignVesselFinder;
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
import java.util.stream.Collectors;

public class LicenceSign implements ShipsSign {

    private static class VesselStructureUpdate implements OvertimeBlockFinderUpdate {

        private final @Nullable ServerBossBar finalBar;
        private final int totalBlockCount;

        private VesselStructureUpdate(int totalBlockCount, @Nullable ServerBossBar bossBar) {
            this.finalBar = bossBar;
            this.totalBlockCount = totalBlockCount;
        }

        @Override
        public OvertimeBlockFinderUpdate.BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure,
                                                                      @NotNull BlockPosition block) {
            if (this.finalBar != null) {
                int blockCount = currentStructure.getOriginalRelativePositionsToCenter().size() + 1;
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
            Vessel vessel = ShipsSignVesselFinder.find(entity);
            return Optional.of(vessel);
        } catch (LoadVesselException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isSign(List<? extends Component> lines) {
        return lines.size() >= 1 && ComponentUtils.toPlain(lines.get(0)).equalsIgnoreCase("[Ships]");
    }

    @Override
    public void changeInto(@NotNull SignSide sign) throws IOException {
        List<Component> lines = sign.getLines();
        Optional<ShipType<?>> opType = ShipsPlugin
                .getPlugin()
                .getAllShipTypes()
                .stream()
                .filter(t -> ComponentUtils.toPlain(lines.get(1)).equalsIgnoreCase(t.getDisplayName()))
                .findFirst();
        if (opType.isEmpty()) {
            throw new IOException("Unknown Ship Type: Ship Types: " + ShipsPlugin
                    .getPlugin()
                    .getAllShipTypes()
                    .stream()
                    .map(ShipType::getDisplayName)
                    .collect(Collectors.joining(", ")));
        }

        String name = ComponentUtils.toPlain(lines.get(2));
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

        Component line1 = Component.text("[Ships]").color(NamedTextColor.YELLOW);
        Component line2 = Component.text(opType.get().getDisplayName()).color(NamedTextColor.BLUE);
        Component line3 = Component.text(name).color(NamedTextColor.GREEN);
        Component line4 = ShipsPlugin
                .getPlugin()
                .getConfig()
                .getTextOnLicenceForthLine()
                .map(t -> Component.text(t).color(NamedTextColor.GREEN))
                .orElse(Component.empty());
        sign.setLines(line1, line2, line3, line4);
    }

    @Override
    public boolean onPrimaryClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        try {
            this.displayInfo(player, position);
            return true;
        } catch (UnableToFindLicenceSign e1) {
            Collection<? extends SyncBlockPosition> foundStructure = e1
                    .getFoundStructure()
                    .getSyncedPositionsRelativeToWorld();
            foundStructure.forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
            TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDelay(5)
                    .setDelayUnit(TimeUnit.SECONDS)
                    .setRunner((sched) -> foundStructure.forEach(bp -> bp.resetBlock(player)))
                    .buildDelayed(ShipsPlugin.getPlugin())
                    .run();
            return true;
        } catch (IOException e) {
            Optional<LiveTileEntity> opTile = position.getTileEntity();
            if (opTile.isPresent()) {
                if (opTile.get() instanceof LiveSignTileEntity) {
                    SignTileEntity lste = (SignTileEntity) opTile.get();
                    String type = lste.getTextAt(1).map(AText::toPlain).orElse("");
                    String name = lste.getTextAt(2).map(AText::toPlain).orElse("");
                    Optional<ShipType<?>> opType = ShipsPlugin
                            .getPlugin()
                            .getAllShipTypes()
                            .stream()
                            .filter(t -> t.getDisplayName().equalsIgnoreCase(type))
                            .findAny();
                    if (opType.isEmpty()) {
                        player.sendMessage(AText
                                                   .ofPlain("Could not find ShipType with display name of " + type)
                                                   .withColour(NamedTextColours.RED));
                        return false;
                    }
                    File file = new File(TranslateCore.getPlatform().getPlatformConfigFolder(),
                                         "VesselData/" + opType.get().getId().replaceAll(":", ".") + "/" + name + "."
                                                 + TranslateCore.getPlatform().getConfigFormat().getFileType()[0]);
                    if (!file.exists()) {
                        player.sendMessage(AText
                                                   .ofPlain("Could not find the file associated with the ship")
                                                   .withColour(NamedTextColours.RED));
                        return false;
                    }
                    ConfigurationStream.ConfigurationFile config = TranslateCore.createConfigurationFile(file,
                                                                                                         TranslateCore
                                                                                                                 .getPlatform()
                                                                                                                 .getConfigFormat());
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
            return true;
        }
    }

    @Override
    public boolean onSecondClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        if (player.isSneaking()) {
            return this.onPrimaryClick(player, position);
        }
        return false;
    }

    private void displayInfo(@NotNull LivePlayer player, @NotNull SyncPosition<Integer> position) throws IOException {
        Vessel s = ShipsSignVesselFinder.find((SignTileEntity) position
                .getTileEntity()
                .orElseThrow(() -> new RuntimeException("Unknown [ships] sign")));
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
            final ServerBossBar finalBar = bar;
            s.updateStructure(new VesselStructureUpdate(totalCount, bar)).thenAccept(structure -> {
                int originalSize = structure.getOriginalRelativePositionsToCenter().size();
                s.save();
                player.sendMessage(AText.ofPlain(
                        "Vessel structure has updated by " + (structure.getOriginalRelativePositionsToCenter().size()
                                - originalSize)));
                if (finalBar != null) {
                    finalBar.deregisterPlayers();
                }
            });
        }
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
