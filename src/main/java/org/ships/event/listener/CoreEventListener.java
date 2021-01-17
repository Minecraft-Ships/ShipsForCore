package org.ships.event.listener;

import org.core.CorePlugin;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.scene.droppeditem.DroppedItem;
import org.core.event.EventListener;
import org.core.event.HEvent;
import org.core.event.events.block.BlockChangeEvent;
import org.core.event.events.block.tileentity.SignChangeEvent;
import org.core.event.events.connection.ClientConnectionEvent;
import org.core.event.events.entity.EntityInteractEvent;
import org.core.event.events.entity.EntitySpawnEvent;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.details.data.keyed.AttachableKeyedData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.config.configuration.ShipsConfig;
import org.ships.event.vessel.create.VesselCreateEvent;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.movement.MovementContext;
import org.ships.permissions.Permissions;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.assits.FileBasedVessel;
import org.ships.vessel.common.assits.TeleportToVessel;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.flag.PlayerStatesFlag;
import org.ships.vessel.common.loader.ShipsBlockFinder;
import org.ships.vessel.common.loader.ShipsIDFinder;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.sign.ShipsSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class CoreEventListener implements EventListener {

    @HEvent
    public void onPlaceEvent(BlockChangeEvent.Place event){
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        DefaultBlockList list = ShipsPlugin.getPlugin().getBlockList();
        if(!config.isStructureClickUpdating()){
            return;
        }
        for(Direction direction : Direction.withYDirections(FourFacingDirection.getFourFacingDirections())){
            SyncBlockPosition position = event.getPosition().getRelative(direction);
            if(!list.getBlockInstruction(position.getBlockType()).getCollideType().equals(BlockInstruction.CollideType.MATERIAL)){
                continue;
            }
            try {
                Vessel vessel = new ShipsBlockFinder(position).load();
                vessel.getStructure().addPosition(position);
            } catch (LoadVesselException ignored) {
            }
        }
    }

    @HEvent
    public void onEntitySpawnEvent(EntitySpawnEvent event){
        if(!(event.getEntity() instanceof DroppedItem)){
            return;
        }
        boolean bool = ShipsPlugin.getPlugin().getVessels().stream().filter(e -> {
            Optional<MovementContext> opValue = e.getValue(MovingFlag.class);
            return opValue.filter(movementContext -> !movementContext.getMovingStructure().isEmpty()).isPresent();
        }).anyMatch(v -> {
            Optional<MovementContext> opSet = v.getValue(MovingFlag.class);
            return opSet.map(movementContext -> movementContext.getMovingStructure().stream().anyMatch(mb -> (mb.getBeforePosition().equals(event.getPosition().toBlockPosition())) || (mb.getAfterPosition().equals(event.getPosition().toBlockPosition())))).orElse(false);
        });
        if(bool) {
            event.setCancelled(true);
        }
    }

    @HEvent
    public void onPlayerJoinEvent(ClientConnectionEvent.Incoming.Joined event) {
        for (Vessel vessel : ShipsPlugin.getPlugin().getVessels()) {
            if (!(vessel instanceof ShipsVessel)) {
                continue;
            }
            ShipsVessel shipsVessel = (ShipsVessel) vessel;
            Optional<PlayerStatesFlag> opFlag = shipsVessel.get(PlayerStatesFlag.class);
            if(!opFlag.isPresent()){
                continue;
            }
            Map<UUID, Vector3<Double>> map = opFlag.get().getValue().get();
            Vector3<Double> vector = map.get(event.getEntity().getUniqueId());
            if(vector == null){
                continue;
            }
            SyncBlockPosition sPos = vessel.getPosition();
            SyncExactPosition position = sPos.toExactPosition().getRelative(vector);
            if(!position.equals(event.getEntity().getPosition())) {
                event.getEntity().setPosition(position);
            }
            Optional<MovingFlag> opMovingFlag = shipsVessel.get(MovingFlag.class);
            event.getEntity().setGravity(!opMovingFlag.isPresent() || !opMovingFlag.get().getValue().isPresent());
        }
    }

    @HEvent
    public void onPlayerLeaveEvent(ClientConnectionEvent.Leave event){
        for (Vessel vessel : ShipsPlugin.getPlugin().getVessels()) {
            if(!(vessel instanceof ShipsVessel)){
                continue;
            }
            ShipsVessel shipsVessel = (ShipsVessel)vessel;
            PlayerStatesFlag flag = shipsVessel.get(PlayerStatesFlag.class).orElse(new PlayerStatesFlag());
            vessel.getEntitiesOvertime(ShipsPlugin.getPlugin().getConfig().getEntityTrackingLimit(), l -> event.getEntity().equals(l), s -> {}, c -> {
                Map<UUID, Vector3<Double>> map = flag.getValue().get();
                for(LiveEntity entity : c) {
                    double x = entity.getPosition().getX() - vessel.getPosition().getX();
                    double y = entity.getPosition().getY() - vessel.getPosition().getY();
                    double z = entity.getPosition().getZ() - vessel.getPosition().getZ();
                    map.put(((LivePlayer)entity).getUniqueId(), Vector3.valueOf(x, y, z));
                }
                if(c.isEmpty()){
                    map.remove(event.getEntity().getUniqueId());
                }
                flag.setValue(map);
                vessel.set(flag);
                vessel.save();
            });
        }
    }

    @HEvent
    public void onPlayerInteractWithBlock(EntityInteractEvent.WithBlock.AsPlayer event){
        SyncBlockPosition position = event.getInteractPosition();
        Optional<LiveTileEntity> opTE = position.getTileEntity();
        if(!opTE.isPresent()){
            return;
        }
        if(!(opTE.get() instanceof LiveSignTileEntity)){
            return;
        }
        LiveSignTileEntity lste = (LiveSignTileEntity) opTE.get();
        ShipsPlugin.getPlugin().getAll(ShipsSign.class).stream().filter(s -> s.isSign(lste)).forEach(s -> {
            if(ShipsSign.LOCKED_SIGNS.stream().anyMatch(b -> b.equals(position))){
                event.getEntity().sendMessagePlain("Sign is moving ship already");
                return;
            }
            boolean cancel = event.getClickAction() == EntityInteractEvent.PRIMARY_CLICK_ACTION ? s.onPrimaryClick(event.getEntity(), position) : s.onSecondClick(event.getEntity(), position);
            if(cancel){
                event.setCancelled(true);
            }
        });
    }

    @HEvent
    public void onSignChangeEvent(SignChangeEvent.ByPlayer event){
        if(ShipsPlugin.getPlugin().getConfig().getDisabledWorlds().contains(event.getEntity().getPosition().getWorld())){
            return;
        }
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
            ShipType<? extends Vessel> type = ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> typeText.equalsPlain(t.getDisplayName(), true)).findAny().get();
            String permission = Permissions.getMakePermission(type);
            if(!(event.getEntity().hasPermission(permission) || event.getEntity().hasPermission(Permissions.SHIP_REMOVE_OTHER))){
                event.getEntity().sendMessage(CorePlugin.buildText(TextColours.RED + "Missing permission: " + permission));
                event.setCancelled(true);
                return;
            }
            try {
                new ShipsIDFinder(type.getName().toLowerCase() + "." + stes.getLine(2).get().toPlain().toLowerCase()).load();
                event.getEntity().sendMessage(CorePlugin.buildText(TextColours.RED + "Name has already been taken"));
                event.setCancelled(true);
                return;
            } catch (LoadVesselException ignored) {
            }
            try {
                for(Direction direction : FourFacingDirection.getFourFacingDirections()) {
                    new ShipsBlockFinder(event.getPosition().getRelative(direction)).load();
                    event.getEntity().sendMessage(CorePlugin.buildText(TextColours.RED + "Can not create a new ship ontop of another ship"));
                }
            } catch (LoadVesselException ignored) {
            }
            ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
            int trackSize = config.getDefaultTrackSize();
            ServerBossBar bar = null;
            if(ShipsPlugin.getPlugin().getConfig().isBossBarVisible()) {
                bar = CorePlugin.createBossBar().setMessage(CorePlugin.buildText("0 / " + trackSize)).register(event.getEntity());
            }
            final ServerBossBar finalBar = bar;
            SyncExactPosition bp = event.getEntity().getPosition();
            ShipsPlugin.getPlugin().getConfig().getDefaultFinder().getConnectedBlocksOvertime(event.getPosition(), new OvertimeBlockFinderUpdate() {
                @Override
                public void onShipsStructureUpdated(PositionableShipsStructure structure) {
                    if(finalBar != null) {
                        finalBar.setMessage(CorePlugin.buildText("Complete"));
                    }
                    Vessel vessel = type.createNewVessel(stes, event.getPosition());
                    if(vessel instanceof TeleportToVessel){
                        ((TeleportToVessel) vessel).setTeleportPosition(bp);
                    }
                    vessel.setStructure(structure);
                    if(vessel instanceof CrewStoredVessel){
                        ((CrewStoredVessel)vessel).getCrew().put(event.getEntity().getUniqueId(), CrewPermission.CAPTAIN);
                    }
                    VesselCreateEvent.Pre preEvent = new VesselCreateEvent.Pre.BySign(vessel, event.getEntity());
                    CorePlugin.getEventManager().callEvent(preEvent);
                    if(preEvent.isCancelled()){
                        if (finalBar != null) {
                            finalBar.deregisterPlayers();
                        }
                        event.setCancelled(true);
                        return;
                    }
                    vessel.setLoading(false);
                    vessel.save();
                    ShipsPlugin.getPlugin().registerVessel(vessel);
                    VesselCreateEvent postEvent = new VesselCreateEvent.Post.BySign(vessel, event.getEntity());
                    CorePlugin.getEventManager().callEvent(postEvent);
                    if (finalBar != null) {
                        finalBar.deregisterPlayers();
                    }
                }

                @Override
                public boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                    if(finalBar != null) {
                        int blockAmount = (currentStructure.getPositions().size() + 1);
                        finalBar.setMessage(CorePlugin.buildText(blockAmount + " / " + trackSize));
                        finalBar.setValue(blockAmount, trackSize);
                    }
                    return true;
                }
            });

        }
        event.setTo(stes);
    }

    @HEvent
    public void onBlockExplode(BlockChangeEvent.Break.Pre.ByExplosion event){
        SyncBlockPosition position = event.getPosition();
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
    public void onBlockBreak(BlockChangeEvent.Break.Pre event){
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        List<Direction> list = new ArrayList<>(Arrays.asList(FourFacingDirection.getFourFacingDirections()));
        list.add(FourFacingDirection.NONE);
        SyncBlockPosition position = event.getPosition();
        for(Direction direction : list) {
            SyncBlockPosition pos = position.getRelative(direction);
            if(config.isStructureClickUpdating()) {
                try {
                    Vessel vessel = new ShipsBlockFinder(pos).load();
                    vessel.getStructure().removePosition(pos);
                } catch (LoadVesselException ignored) {
                }
            }
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
            Optional<Vessel> opVessel = licenceSign.getShip(lste);
            if (!opVessel.isPresent()) {
                continue;
            }
            Vessel vessel = opVessel.get();
            if(vessel instanceof CrewStoredVessel && event instanceof BlockChangeEvent.Break.Pre.ByPlayer) {
                LivePlayer player = ((BlockChangeEvent.Break.Pre.ByPlayer) event).getEntity();
                if (!(((CrewStoredVessel)vessel).getPermission(player.getUniqueId()).canRemove() || (player.hasPermission(Permissions.ABSTRACT_SHIP_MOVE_OTHER)))) {
                    event.setCancelled(true);
                    return;
                }
            }
            if(vessel instanceof FileBasedVessel) {
                File file = ((FileBasedVessel)vessel).getFile();
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    event.setCancelled(true);
                    e.printStackTrace();
                }
            }
            ShipsPlugin.getPlugin().unregisterVessel(vessel);
            if(event instanceof BlockChangeEvent.Break.Pre.ByPlayer) {
                ((BlockChangeEvent.Break.Pre.ByPlayer) event).getEntity().sendMessage(CorePlugin.buildText(TextColours.AQUA + vessel.getName() + " removed successfully"));
            }
            return;
        }
    }
}
