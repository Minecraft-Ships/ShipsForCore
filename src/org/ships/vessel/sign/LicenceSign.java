package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsBlockLoader;
import org.ships.vessel.common.loader.ShipsIDLoader;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.IOException;
import java.util.Optional;

public class LicenceSign implements ShipsSign {

    public Optional<Vessel> getShip(SignTileEntity entity){
        Optional<Text> opLine1 = entity.getLine(1);
        if(!opLine1.isPresent()){
            return Optional.empty();
        }
        Optional<Text> opLine2 = entity.getLine(2);
        if(!opLine2.isPresent()){
            return Optional.empty();
        }
        String typeS = opLine1.get().toPlain();
        String nameS = opLine2.get().toPlain();
        if(typeS == null){
            return Optional.empty();
        }
        if(nameS == null){
            return Optional.empty();
        }
        Optional<ShipType> opType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(st -> st.getDisplayName().equals(typeS)).findAny();
        if(!opType.isPresent()){
            return Optional.empty();
        }
        Vessel vessel;
        try {
            vessel = new ShipsIDLoader(opType.get().getId() + ":" + nameS).load();
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.ofNullable(vessel);
    }

    @Override
    public boolean isSign(SignTileEntity entity) {
        Optional<Text> opValue = entity.getLine(0);
        return opValue.isPresent() && opValue.get().equals(getFirstLine());
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException{
        SignTileEntitySnapshot snapshot = sign.getSnapshot();
        Text[] lines = snapshot.getLines();
        Optional<ShipType> opType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> lines[1].equalsPlain(t.getDisplayName(), true)).findFirst();
        if(!opType.isPresent()){
            throw new IOException("Unknown Ship Type: Ship Types: " + CorePlugin.toString(", ", ShipType::getDisplayName, ShipsPlugin.getPlugin().getAll(ShipType.class)));
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
    public boolean onPrimaryClick(LivePlayer player, BlockPosition position){
        return onSecondClick(player, position);
    }

    @Override
    public boolean onSecondClick(LivePlayer player, BlockPosition position) {
        try {
            Vessel s = new ShipsBlockLoader(position).load();
            if (!player.isSneaking()) {
                player.sendMessage(CorePlugin.buildText(TextColours.AQUA + "----[Ships Info]----"));
                player.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Name: " + TextColours.AQUA + s.getName()));
                player.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Max Altitude: " + TextColours.AQUA + s.getAltitudeSpeed()));
                player.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Max Speed: " + TextColours.AQUA + s.getMaxSpeed()));
                player.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Size: " + TextColours.AQUA + s.getStructure().getRelativePositions().size()));
                player.sendMessage(CorePlugin.buildText(TextColours.GREEN + "Entities: " + TextColours.AQUA + s.getEntities().size()));
                if (!(s instanceof ShipsVessel)) {
                    return false;
                }
                ShipsVessel vessel = (ShipsVessel) s;
                vessel.getExtraInformation().forEach((key, value) -> player.sendMessage(CorePlugin.buildText(TextColours.GREEN + key + ": " + TextColours.AQUA + value)));
                //player.sendMessage(TextColours.AQUA + "Default Crew" + vessel.getDefaultPermission().getId());
                player.sendMessage(CorePlugin.buildText(TextColours.GREEN + "id: " + TextColours.AQUA + vessel.getId()));
            } else {
                if (!(s instanceof ShipsVessel)) {
                    return false;
                }
                ShipsVessel vessel = (ShipsVessel) s;
                int size = vessel.getStructure().getPositions().size();
                ShipsPlugin.getPlugin().getConfig().getDefaultFinder().setConnectedVessel(vessel).getConnectedBlocksOvertime(vessel.getPosition(), new OvertimeBlockFinderUpdate() {
                    @Override
                    public void onShipsStructureUpdated(PositionableShipsStructure structure) {
                        vessel.setStructure(structure);
                        vessel.save();
                        player.sendMessagePlain("Vessels structure has updated by " + (structure.getPositions().size() - size));
                    }

                    @Override
                    public boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                        return true;
                    }
                });
            }
        } catch (IOException e) {
            Optional<LiveTileEntity> opTile = position.getTileEntity();
            if(opTile.isPresent()){
                if(opTile.get() instanceof LiveSignTileEntity){
                    LiveSignTileEntity lste = (LiveSignTileEntity)opTile.get();
                    String type = lste.getLine(1).get().toPlain();
                    String name = lste.getLine(2).get().toPlain();
                    try{
                        AbstractShipsVessel vessel = new ShipsIDLoader(type + ":" + name).load();
                        vessel.getStructure().setPosition(position);
                        vessel.save();
                        player.sendMessage(CorePlugin.buildText(TextColours.AQUA + "Resynced " + name + " with file. Please try again"));
                    }catch(IOException e1){
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getMessage()));
                    }
                    return false;
                }
            }
            player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
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
