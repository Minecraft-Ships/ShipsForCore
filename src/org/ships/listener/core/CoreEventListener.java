package org.ships.listener.core;

import org.core.CorePlugin;
import org.core.event.EventListener;
import org.core.event.HEvent;
import org.core.event.events.block.BlockChangeEvent;
import org.core.event.events.block.tileentity.SignChangeEvent;
import org.core.event.events.connection.ClientConnectionEvent;
import org.core.event.events.entity.EntityInteractEvent;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.details.data.keyed.AttachableKeyedData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.permissions.Permissions;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.loader.shipsvessel.ShipsBlockLoader;
import org.ships.vessel.common.loader.shipsvessel.ShipsFileLoader;
import org.ships.vessel.common.loader.shipsvessel.ShipsIDLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.sign.ShipsSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CoreEventListener implements EventListener {

    @HEvent
    public void onPlayerLeaveEvent(ClientConnectionEvent.Leave event){
        boolean check = ShipsFileLoader.loadAll(e -> {})
                .stream()
                .anyMatch(v -> v.getEntities().stream().anyMatch(e -> e.equals(event.getEntity())));
        if(!check){
            return;
        }
        event.getEntity().setGravity(true);
    }

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
            event.setCancelled(true);
            event.getEntity().sendMessagePlain("Error: " + e.getMessage());
            return;
        }
        if(register){
            Text typeText = stes.getLine(1).get();
            ShipType type = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> typeText.equalsPlain(t.getDisplayName(), true)).findAny().get();
            String permission = Permissions.getMakePermission(type);
            if(!(event.getEntity().hasPermission(permission) || event.getEntity().hasPermission(Permissions.SHIP_REMOVE_OTHER))){
                event.getEntity().sendMessage(CorePlugin.buildText(TextColours.RED + "Missing permission: " + permission));
                event.setCancelled(true);
                return;
            }
            try {
                new ShipsIDLoader(type.getName().toLowerCase() + ":" + stes.getLine(2).get().toPlain().toLowerCase()).load();
                event.getEntity().sendMessage(CorePlugin.buildText(TextColours.RED + "Name has already been taken"));
                event.setCancelled(true);
                return;
            } catch (LoadVesselException e) {
            }
            try {
                for(Direction direction : FourFacingDirection.getFourFacingDirections()) {
                    new ShipsBlockLoader(event.getPosition().getRelative(direction)).load();
                    event.getEntity().sendMessage(CorePlugin.buildText(TextColours.RED + "Can not create a new ship ontop of another ship"));
                }
            } catch (LoadVesselException e) {
            }
            PositionableShipsStructure pss = ShipsPlugin.getPlugin().getConfig().getDefaultFinder().getConnectedBlocks(event.getPosition());
            Vessel vessel = type.createNewVessel(stes, event.getPosition());
            vessel.setStructure(pss);
            if(vessel instanceof CrewStoredVessel){
                ((CrewStoredVessel)vessel).getCrew().put(event.getEntity().getUniqueId(), CrewPermission.CAPTAIN);
            }
            vessel.save();
        }
        event.setTo(stes);
    }

    @HEvent
    public void onBlockExplode(BlockChangeEvent.Break.ByExplosion event){
        BlockPosition position = event.getPosition();
        List<Direction> directions = new ArrayList<>(Arrays.asList(FourFacingDirection.getFourFacingDirections()));
        directions.add(FourFacingDirection.NONE);
        for(Direction direction : directions) {
            if (!(position.getRelative(direction).getTileEntity().isPresent())) {
                continue;
            }
            LiveTileEntity lte = position.getRelative(direction).getTileEntity().get();
            if (!(lte instanceof LiveSignTileEntity)) {
                continue;
            }
            LiveSignTileEntity sign = (LiveSignTileEntity) lte;
            LicenceSign licenceSign = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
            if (!licenceSign.isSign(sign)) {
                continue;
            }
            if(!direction.equals(FourFacingDirection.NONE)){
                Optional<Direction> opAttachable = position.getRelative(direction).getBlockDetails().get(AttachableKeyedData.class);
                if(!opAttachable.isPresent()){
                    continue;
                }
                if(!opAttachable.get().getOpposite().equals(direction)){
                    continue;
                }
            }
            event.setCancelled(true);
            return;
        }
    }

    @HEvent
    public void onBlockBreak(BlockChangeEvent.Break.ByPlayer event){
        List<Direction> list = new ArrayList<>(Arrays.asList(FourFacingDirection.getFourFacingDirections()));
        list.add(FourFacingDirection.NONE);
        BlockPosition position = event.getPosition();
        for(Direction direction : list) {
            BlockPosition pos = position.getRelative(direction);
            if (!(pos.getTileEntity().isPresent())) {
                continue;
            }
            LiveTileEntity lte = pos.getTileEntity().get();
            if(!(lte instanceof LiveSignTileEntity)){
                continue;
            }
            LiveSignTileEntity lste = (LiveSignTileEntity)lte;
            LicenceSign licenceSign = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
            if (!licenceSign.isSign(lste)) {
                continue;
            }
            if(!direction.equals(FourFacingDirection.NONE)){
                Optional<Direction> opAttachable = position.getRelative(direction).getBlockDetails().get(AttachableKeyedData.class);
                if(!opAttachable.isPresent()){
                    continue;
                }
                if(!opAttachable.get().getOpposite().equals(direction)){
                    continue;
                }
            }
            Optional<ShipsVessel> opVessel = licenceSign.getShip(lste);
            if (!opVessel.isPresent()) {
                continue;
            }
            ShipsVessel vessel = opVessel.get();
            if (!(vessel.getPermission(event.getEntity().getUniqueId()).canRemove() || (event.getEntity().hasPermission(Permissions.ABSTRACT_SHIP_MOVE_OTHER)))) {
                event.setCancelled(true);
                return;
            }
            File file = vessel.getFile();
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                event.setCancelled(true);
                e.printStackTrace();
            }
            event.getEntity().sendMessage(CorePlugin.buildText(TextColours.AQUA + vessel.getName() + " removed successfully"));
            return;
        }
    }
}
