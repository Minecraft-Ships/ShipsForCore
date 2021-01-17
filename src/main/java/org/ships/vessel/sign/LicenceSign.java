package org.ships.vessel.sign;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.config.ConfigurationStream;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.utils.Identifable;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsLicenceSignFinder;
import org.ships.vessel.common.loader.shipsvessel.ShipsFileLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class LicenceSign implements ShipsSign {

    public Optional<Vessel> getShip(SignTileEntity entity){
        try {
            return Optional.of(new ShipsLicenceSignFinder(entity).load());
        } catch (LoadVesselException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isSign(SignTileEntity entity) {
        Optional<Text> opValue = entity.getLine(0);
        return opValue.isPresent() && opValue.get().equalsPlain(getFirstLine().toPlain(), false);
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException{
        SignTileEntitySnapshot snapshot = sign.getSnapshot();
        Text[] lines = snapshot.getLines();
        Optional<ShipType> opType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> lines[1].equalsPlain(t.getDisplayName(), true)).findFirst();
        if(!opType.isPresent()){
            throw new IOException("Unknown Ship Type: Ship Types: " + ArrayUtils.toString(", ", ShipType::getDisplayName, ShipsPlugin.getPlugin().getAll(ShipType.class)));
        }

        String name = lines[2].toPlain();
        if(name.replaceAll(" ", "").length() == 0){
            throw new IOException("Invalid name: Change 3rd line");
        }
        if(name.contains(":")){
            name = name.replaceAll(":", "");
        }
        if(name.contains(" ")){
            name = name.replaceAll(" ", "_");
        }
        name = (Character.toUpperCase(name.charAt(0))) + name.substring(1);
        snapshot.setLine(0, CorePlugin.buildText(TextColours.YELLOW + "[Ships]"));
        snapshot.setLine(1, CorePlugin.buildText(TextColours.BLUE + opType.get().getDisplayName()));
        snapshot.setLine(2, CorePlugin.buildText(TextColours.GREEN + name));
        snapshot.setLine(3, CorePlugin.buildText(TextColours.GREEN + lines[3].toPlain()));
        return snapshot;
    }

    @Override
    public Text getFirstLine() {
        return CorePlugin.buildText(TextColours.YELLOW + "[Ships]");
    }

    @Override
    public boolean onPrimaryClick(LivePlayer player, SyncBlockPosition position){
        try {
            Vessel s = new ShipsLicenceSignFinder(position).load();
            if (!player.isSneaking()) {
                if (s instanceof Identifable) {
                    player.sudo("ships", "ship", ((Identifable) s).getId().substring(6), "info");
                }
            } else {
                int size = s.getStructure().getPositions().size();
                ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
                ServerBossBar bar = null;
                int totalCount = config.getDefaultTrackSize();
                if (config.isBossBarVisible()) {
                    bar = CorePlugin.createBossBar().register(player).setMessage(CorePlugin.buildText("0 / " + totalCount));
                }
                final ServerBossBar finalBar = bar;
                ShipsPlugin.getPlugin().getConfig().getDefaultFinder().setConnectedVessel(s).getConnectedBlocksOvertime(s.getPosition(), new OvertimeBlockFinderUpdate() {
                    @Override
                    public void onShipsStructureUpdated(PositionableShipsStructure structure) {
                        s.setStructure(structure);
                        s.save();
                        player.sendMessagePlain("Vessels structure has updated by " + (structure.getPositions().size() - size));
                        if (finalBar != null) {
                            finalBar.deregisterPlayers();
                        }
                    }

                    @Override
                    public boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                        if (finalBar != null) {
                            int blockCount = currentStructure.getPositions().size() + 1;
                            finalBar.setMessage(CorePlugin.buildText(blockCount + " / " + totalCount));
                            try {
                                finalBar.setValue(blockCount, totalCount);
                            } catch (IllegalArgumentException ignore) {

                            }
                        }
                        return true;
                    }
                });
            }
        }catch (UnableToFindLicenceSign e1){
            e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
            CorePlugin.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
        } catch (IOException e) {
            Optional<LiveTileEntity> opTile = position.getTileEntity();
            if(opTile.isPresent()){
                if(opTile.get() instanceof LiveSignTileEntity){
                    LiveSignTileEntity lste = (LiveSignTileEntity)opTile.get();
                    String type = lste.getLine(1).get().toPlain();
                    String name = lste.getLine(2).get().toPlain();
                    Optional<ShipType> opType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> t.getDisplayName().equalsIgnoreCase(type)).findAny();
                    if(!opType.isPresent()){
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + "Could not find ShipType with display name of " + type));
                        return false;
                    }
                    File file = new File("plugins/Ships/VesselData/" + opType.get().getId().replaceAll(":", ".") + "/" + name + "." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]);
                    if(!file.exists()){
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + "Could not find the file associated with the ship"));
                        return false;
                    }
                    ConfigurationStream.ConfigurationFile config = CorePlugin.createConfigurationFile(file, CorePlugin.getPlatform().getConfigFormat());
                    config.set(ShipsFileLoader.META_LOCATION_X, position.getX());
                    config.set(ShipsFileLoader.META_LOCATION_Y, position.getY());
                    config.set(ShipsFileLoader.META_LOCATION_Z, position.getZ());
                    config.set(ShipsFileLoader.META_LOCATION_WORLD, position.getWorld());
                    config.save();
                    try {
                        ShipsVessel vessel = new ShipsFileLoader(file).load();
                        ShipsPlugin.getPlugin().registerVessel(vessel);
                        player.sendMessagePlain("Ship has resynced");
                    } catch (LoadVesselException loadVesselException) {
                        player.sendMessagePlain(loadVesselException.getReason());
                        return false;
                    }
                    return true;
                }
            }
            player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
        }
        return false;
    }

    @Override
    public boolean onSecondClick(LivePlayer player, SyncBlockPosition position) {
        if(player.isSneaking()){
            return onPrimaryClick(player, position);
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
