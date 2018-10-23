package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.details.TiledBlockDetails;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsIDLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.Optional;

public class LicenceSign implements ShipsSign {

    public Optional<Vessel> getShip(SignTileEntity entity){
        String typeS = TextColours.stripColours(entity.getLine(1));
        String nameS = TextColours.stripColours(entity.getLine(2));
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
        return Optional.of(new ShipsIDLoader(opType.get().getId() + ":" + nameS).load());
    }

    @Override
    public boolean isSign(SignTileEntity entity) {
        return entity.getLine(0).equals(TextColours.YELLOW + "[Ships]");
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) throws IOException{
        SignTileEntitySnapshot snapshot = sign.getSnapshot();
        String[] lines = snapshot.getLines();
        Optional<ShipType> opType = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> t.getDisplayName().equalsIgnoreCase(sign.getLine(1))).findFirst();
        if(!opType.isPresent()){
            throw new IOException("Unknown Ship Type: Ship Types: " + CorePlugin.toString(", ", s -> s.getDisplayName(), ShipsPlugin.getPlugin().getAll(ShipType.class)));
        }

        String name = lines[2];
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
        snapshot.setLine(0, TextColours.YELLOW + "[Ships]");
        snapshot.setLine(1, TextColours.BLUE + opType.get().getDisplayName());
        snapshot.setLine(2, TextColours.GREEN + name);
        snapshot.setLine(3, TextColours.GREEN + lines[3]);
        return snapshot;
    }

    @Override
    public String getFirstLine() {
        return TextColours.YELLOW + "[Ships]";
    }

    @Override
    public boolean onSecondClick(LivePlayer player, BlockPosition position) {
        TileEntitySnapshot tes = ((TiledBlockDetails) position.getBlockDetails()).getTileEntity();
        if(!(tes instanceof SignTileEntity)){
            return false;
        }
        SignTileEntity ste = (SignTileEntity) tes;
        getShip(ste).ifPresent(s -> {
            player.sendMessage(TextColours.AQUA + "----[Ships Info]----");
            player.sendMessage(TextColours.AQUA + "Name: " + s.getName());
            player.sendMessage(TextColours.AQUA + "Max Altitude: " + s.getAltitudeSpeed());
            player.sendMessage(TextColours.AQUA + "Max Speed: " + s.getMaxSpeed());
            player.sendMessage(TextColours.AQUA + "Size: " + s.getStructure().getRelativePositions().size());
            if(!(s instanceof ShipsVessel)){
                return;
            }
            ShipsVessel vessel = (ShipsVessel)s;
            //player.sendMessage(TextColours.AQUA + "Default Crew" + vessel.getDefaultPermission().getId());
            player.sendMessage(TextColours.AQUA + "id: " + vessel.getId());
        });
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
