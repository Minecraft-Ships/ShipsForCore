package org.ships.listener.core;

import org.core.event.EventListener;
import org.core.event.HEvent;
import org.core.event.events.block.BlockChangeEvent;
import org.core.event.events.block.tileentity.SignChangeEvent;
import org.core.event.events.entity.EntityInteractEvent;
import org.core.text.Text;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.details.TiledBlockDetails;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.sign.ShipsSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.IOException;
import java.util.Optional;

public class CoreEventListener implements EventListener {

    @HEvent
    public void onPlayerInteractWithBlock(EntityInteractEvent.WithBlock.AsPlayer event){
        BlockPosition position = event.getInteractPosition();
        Optional<LiveTileEntity> opTE = position.getTileEntity();
        if(!opTE.isPresent()){
            return;
        }
        if(!(opTE.get() instanceof LiveSignTileEntity)){
            return;
        }
        LiveSignTileEntity lste = (LiveSignTileEntity) opTE.get();
        ShipsPlugin.getPlugin().getAll(ShipsSign.class).stream().filter(s -> s.isSign(lste)).forEach(s -> {
            boolean cancel = event.getClickAction() == EntityInteractEvent.PRIMARY_CLICK_ACTION ? s.onPrimaryClick(event.getEntity(), position) : s.onSecondClick(event.getEntity(), position);
            if(cancel){
                event.setCancelled(true);
            }
        });
    }

    @HEvent
    public void onSignChangeEvent(SignChangeEvent.ByPlayer event){
        ShipsSign sign = null;
        boolean register = false;
        Optional<Text> opFirstLine = event.getFrom().getLine(0);
        if(!opFirstLine.isPresent()){
            return;
        }
        Text line = opFirstLine.get();
        if(line.equalsPlain("[Ships]", true)) {
            sign = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
            register = true;
        }else if(ShipsPlugin.getPlugin().getAll(ShipsSign.class).stream().anyMatch(s -> s.getFirstLine().equalsPlain(line.toPlain(), true))){
            sign = ShipsPlugin.getPlugin().getAll(ShipsSign.class).stream().filter(s -> s.getFirstLine().equalsPlain(line.toPlain(), true)).findAny().get();
        }
        if(sign == null){
            return;
        }
        SignTileEntitySnapshot stes;
        try {
            stes = sign.changeInto(event.getTo());
        } catch (IOException e) {
            event.getEntity().sendMessagePlain("Error: " + e.getMessage());
            return;
        }
        if(register){
            Text typeText = stes.getLine(1).get();
            ShipType type = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> typeText.equalsPlain(t.getDisplayName(), true)).findAny().get();
            Vessel vessel = type.createNewVessel(stes, event.getPosition());
            PositionableShipsStructure pss = ShipsPlugin.getPlugin().getConfig().getDefaultFinder().getConnectedBlocks(event.getPosition());
            vessel.setStructure(pss);
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
