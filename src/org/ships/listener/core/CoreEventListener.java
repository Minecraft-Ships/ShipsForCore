package org.ships.listener.core;

import org.core.CorePlugin;
import org.core.event.EventListener;
import org.core.event.HEvent;
import org.core.event.events.block.BlockChangeEvent;
import org.core.event.events.block.tileentity.SignChangeEvent;
import org.core.event.events.entity.EntityInteractEvent;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.permissions.Permissions;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsBlockLoader;
import org.ships.vessel.common.loader.ShipsIDLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.sign.ShipsSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class CoreEventListener implements EventListener {

    @HEvent
    public void onPlayerInteractWithBlock(EntityInteractEvent.WithBlock.AsPlayer event){
        ShipsPlugin.getPlugin().getDebugFile().addMessage("--[Start of CoreEventListener:onPlayerInteractWithBlock(EntityInteractEvent.WithBlock.AsPlayer)]--");
        BlockPosition position = event.getInteractPosition();
        Optional<LiveTileEntity> opTE = position.getTileEntity();
        if(!opTE.isPresent()){
            ShipsPlugin.getPlugin().getDebugFile().addMessage("Returned due to the block not being a LiveTileEntity", "--[end of CoreEventListener:onPlayerInteractWithBlock(EntityInteractEvent.WithBlock.AsPlayer)]--");
            return;
        }
        if(!(opTE.get() instanceof LiveSignTileEntity)){
            ShipsPlugin.getPlugin().getDebugFile().addMessage("Returned due to the block not being a LiveSignTileEntity", "--[End of CoreEventListener:onPlayerInteractWithBlock(EntityInteractEvent.WithBlock.AsPlayer)]--");
            return;
        }
        LiveSignTileEntity lste = (LiveSignTileEntity) opTE.get();
        ShipsPlugin.getPlugin().getAll(ShipsSign.class).stream().filter(s -> s.isSign(lste)).forEach(s -> {
            ShipsPlugin.getPlugin().getDebugFile().addMessage("Sign found as " + s.getId());
            boolean cancel = event.getClickAction() == EntityInteractEvent.PRIMARY_CLICK_ACTION ? s.onPrimaryClick(event.getEntity(), position) : s.onSecondClick(event.getEntity(), position);
            if(cancel){
                ShipsPlugin.getPlugin().getDebugFile().addMessage("EntityInteractEvent cancelled due to ShipsSign click failing");
                event.setCancelled(true);
            }
        });
        ShipsPlugin.getPlugin().getDebugFile().addMessage("--[End of CoreEventListener:onPlayerInteractWithBlock(EntityInteractEvent.WithBlock.AsPlayer)]--");
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
            event.setCancelled(true);
            event.getEntity().sendMessagePlain("Error: " + e.getMessage());
            return;
        }
        if(register){
            Text typeText = stes.getLine(1).get();
            ShipType type = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> typeText.equalsPlain(t.getDisplayName(), true)).findAny().get();
            String permission = Permissions.getMakePermission(type);
            if(!event.getEntity().hasPermission(permission)){
                event.getEntity().sendMessage(CorePlugin.buildText(TextColours.RED + "Missing permission: " + permission));
                event.setCancelled(true);
                return;
            }
            try {
                System.out.println("ID: " + type.getName().toLowerCase() + ":" + stes.getLine(2).get().toPlain().toLowerCase());
                new ShipsIDLoader(type.getName().toLowerCase() + ":" + stes.getLine(2).get().toPlain().toLowerCase()).load();
                event.getEntity().sendMessage(CorePlugin.buildText(TextColours.RED + "Name has already been taken"));
                event.setCancelled(true);
                return;
            } catch (LoadVesselException e) {
            }
            try {
                new ShipsBlockLoader(event.getPosition()).load();
                event.getEntity().sendMessage(CorePlugin.buildText(TextColours.RED + "Can not create a new ship ontop of another ship"));
            } catch (LoadVesselException e) {
            }
            PositionableShipsStructure pss = ShipsPlugin.getPlugin().getConfig().getDefaultFinder().getConnectedBlocks(event.getPosition());
            Vessel vessel = type.createNewVessel(stes, event.getPosition());
            vessel.setStructure(pss);
            vessel.save();
            if(vessel instanceof ShipsVessel){
                ((ShipsVessel)vessel).getCrew().put(event.getEntity(), CrewPermission.CAPTAIN);
            }
            //register ship
        }
        event.setTo(stes);
    }

    @HEvent
    public void onBlockBreak(BlockChangeEvent.Break.ByPlayer event){
        if(!(event.getBeforeState().get(KeyedData.TILED_ENTITY).isPresent())){
            return;
        }
        TileEntitySnapshot<? extends TileEntity> tes = event.getBeforeState().get(KeyedData.TILED_ENTITY).get();
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
            return;
        }
        File file = sVessel.getFile();
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            event.setCancelled(true);
            e.printStackTrace();
        }
    }
}
