package org.ships.listener.core;

import org.core.event.EventListener;
import org.core.event.HEvent;
import org.core.event.events.block.BlockChangeEvent;
import org.core.event.events.block.tileentity.SignChangeEvent;
import org.core.event.events.entity.EntityInteractEvent;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.details.TiledBlockDetails;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.sign.ShipsSign;

import java.io.IOException;
import java.util.Optional;

public class CoreEventListener implements EventListener {

    @HEvent
    public void onPlayerInteractWithBlock(EntityInteractEvent.WithBlock.AsPlayer event){
        BlockPosition position = event.getInteractPosition();
        if(!(position.getBlockDetails() instanceof TiledBlockDetails)){
            return;
        }
        TileEntitySnapshot lte = ((TiledBlockDetails)position.getBlockDetails()).getTileEntity();
        if(!(lte instanceof SignTileEntitySnapshot)){
            return;
        }
        SignTileEntitySnapshot stes = (SignTileEntitySnapshot)lte;
        ShipsPlugin.getPlugin().getAll(ShipsSign.class).stream().filter(s -> s.isSign(stes)).forEach(s -> {
            boolean cancel = s.onSecondClick(event.getEntity(), position);
            if(cancel){
                event.setCancelled(true);
            }
        });
    }

    @HEvent
    public void onSignChangeEvent(SignChangeEvent.ByPlayer event){
        ShipsSign sign;
        boolean register = false;
        if(event.getFrom().getLine(0).equalsIgnoreCase("[Ships]")) {
            sign = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
            register = true;
        }else if(ShipsPlugin.getPlugin().getAll(ShipsSign.class).stream().anyMatch(s -> event.getFrom().getLine(0).equalsIgnoreCase(TextColours.stripColours(s.getFirstLine())))){
            sign = ShipsPlugin.getPlugin().getAll(ShipsSign.class).stream().filter(s -> event.getFrom().getLine(0).equalsIgnoreCase(TextColours.stripColours(s.getFirstLine()))).findAny().get();
        }else{
            System.out.println("No match: " + event.getFrom().getLine(0) + " | " + "[Ships]");
            return;
        }
        SignTileEntitySnapshot stes;
        try {
            stes = sign.changeInto(event.getTo());
        } catch (IOException e) {
            event.getEntity().sendMessage("Error: " + e.getMessage());
            return;
        }
        if(register){
            String typeS = TextColours.stripColours(stes.getLine(1));
            System.out.println("Position: " + event.getPosition());
            ShipType type = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> t.getDisplayName().equals(typeS)).findAny().get();
            Vessel vessel = type.createNewVessel(stes, event.getPosition());
            System.out.println("Vessel: " + vessel);
            vessel.save();
            //register ship
        }
        event.setTo(stes);
    }

    @HEvent
    public void onBlockBreak(BlockChangeEvent.Break.ByPlayer event){
        if(!(event.getBeforeState() instanceof TiledBlockDetails)){
            return;
        }
        TileEntitySnapshot tes = ((TiledBlockDetails)event.getBeforeState()).getTileEntity();
        if(!(tes instanceof SignTileEntitySnapshot)){
            return;
        }
        SignTileEntitySnapshot sign = (SignTileEntitySnapshot) tes;
        LicenceSign licenceSign = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
        if(!licenceSign.isSign(sign)){
            return;
        }
        Optional<Vessel> opVessel = licenceSign.getShip(sign);
        if(!opVessel.isPresent()){
            return;
        }
        Vessel vessel = opVessel.get();
        if(!(vessel instanceof ShipsVessel)){
            return;
        }
        ShipsVessel sVessel = (ShipsVessel) vessel;
        if(!sVessel.getPermission(event.getEntity()).canRemove()){
            event.setCancelled(true);
            //INCORRECT PERMISSIONS
            return;
        }
    }
}
