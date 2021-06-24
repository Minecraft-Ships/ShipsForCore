package org.ships.event.listener;

import org.array.utils.ArrayUtils;
import org.core.CorePlugin;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.Player;
import org.core.entity.scene.droppeditem.DroppedItem;
import org.core.event.EventListener;
import org.core.event.EventPriority;
import org.core.event.HEvent;
import org.core.event.events.block.BlockChangeEvent;
import org.core.event.events.block.tileentity.SignChangeEvent;
import org.core.event.events.connection.ClientConnectionEvent;
import org.core.event.events.entity.EntityCommandEvent;
import org.core.event.events.entity.EntityInteractEvent;
import org.core.event.events.entity.EntitySpawnEvent;
import org.core.schedule.unit.TimeUnit;
import org.core.utils.Else;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.details.data.keyed.AttachableKeyedData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.ExactPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.event.vessel.create.VesselCreateEvent;
import org.ships.exceptions.NoLicencePresent;
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
import org.ships.vessel.common.loader.ShipsOvertimeBlockFinder;
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
    public void onPlayerCommand(EntityCommandEvent event) {
        Optional<String> opLoginCommand = ShipsPlugin.getPlugin().getConfig().getDefaultLoginCommand();
        if (!opLoginCommand.isPresent()) {
            return;
        }
        if (!ArrayUtils.toString(", ", t -> t, event.getCommand()).toLowerCase().startsWith(opLoginCommand.get().toLowerCase())) {
            return;
        }
        CorePlugin
                .createSchedulerBuilder()
                .setDelay(2)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setExecutor(() -> onPlayerJoin(event.getEntity()))
                .build(ShipsPlugin.getPlugin());
    }

    @HEvent
    public void onPlaceEvent(BlockChangeEvent.Place event) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        DefaultBlockList list = ShipsPlugin.getPlugin().getBlockList();
        if (!config.isStructureClickUpdating()) {
            return;
        }
        for (Direction direction : Direction.withYDirections(FourFacingDirection.getFourFacingDirections())) {
            SyncBlockPosition position = event.getPosition().getRelative(direction);
            if (!list.getBlockInstruction(position.getBlockType()).getCollideType().equals(BlockInstruction.CollideType.MATERIAL)) {
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
    public void onEntitySpawnEvent(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof DroppedItem)) {
            return;
        }
        boolean bool = ShipsPlugin.getPlugin().getVessels().stream().filter(e -> {
            Optional<MovementContext> opValue = e.getValue(MovingFlag.class);
            return opValue.filter(movementContext -> !movementContext.getMovingStructure().isEmpty()).isPresent();
        }).anyMatch(v -> {
            Optional<MovementContext> opSet = v.getValue(MovingFlag.class);
            return opSet.map(movementContext -> movementContext.getMovingStructure().stream().anyMatch(mb -> (mb.getBeforePosition().equals(event.getPosition().toBlockPosition())) || (mb.getAfterPosition().equals(event.getPosition().toBlockPosition())))).orElse(false);
        });
        if (bool) {
            event.setCancelled(true);
        }
    }

    @HEvent
    public void onPlayerJoinEvent(ClientConnectionEvent.Incoming.Joined event) {
        onPlayerJoin(event.getEntity());
    }

    public void onPlayerJoin(Player<?> player) {
        for (Vessel vessel : ShipsPlugin.getPlugin().getVessels()) {
            if (!(vessel instanceof ShipsVessel)) {
                continue;
            }
            ShipsVessel shipsVessel = (ShipsVessel) vessel;
            Optional<PlayerStatesFlag> opFlag = shipsVessel.get(PlayerStatesFlag.class);
            if (!opFlag.isPresent()) {
                continue;
            }
            Map<UUID, Vector3<Double>> map = opFlag.get().getValue().orElse(new HashMap<>());
            Vector3<Double> vector = map.get(player.getUniqueId());
            if (vector == null) {
                continue;
            }
            SyncBlockPosition sPos = vessel.getPosition();
            SyncExactPosition position = sPos.toExactPosition().getRelative(vector);
            if (!position.equals(player.getPosition())) {
                player.setPosition(position);
            }
            Optional<MovingFlag> opMovingFlag = shipsVessel.get(MovingFlag.class);
            player.setGravity(!opMovingFlag.isPresent() || !opMovingFlag.get().getValue().isPresent());
        }
    }

    public void onPlayerLeave(Player<?> player, ExactPosition position) {
        BlockPosition block = position.toBlockPosition().getRelative(FourFacingDirection.DOWN);
        new ShipsOvertimeBlockFinder(block).loadOvertime(vessel -> {
            if (!(vessel instanceof ShipsVessel)) {
                return;
            }
            ShipsVessel shipsVessel = (ShipsVessel) vessel;
            PlayerStatesFlag flag = shipsVessel.get(PlayerStatesFlag.class).orElse(new PlayerStatesFlag());
            Map<UUID, Vector3<Double>> map = flag.getValue().orElse(new HashMap<>());
            UUID uuid = player.getUniqueId();
            Vector3<Double> vector = position.getPosition().minus(shipsVessel.getPosition().toExactPosition().getPosition());

            if (map.containsKey(uuid)) {
                map.replace(uuid, vector);
            } else {
                map.put(uuid, vector);
            }
            flag.setValue(map);
            vessel.set(flag);
            vessel.save();
        }, (pss) -> {
        });
    }

    @HEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeaveEvent(ClientConnectionEvent.Leave event) {
        onPlayerLeave(event.getEntity(), event.getEntity().getPosition());
    }

    @HEvent
    public void onPlayerInteractWithBlock(EntityInteractEvent.WithBlock.AsPlayer event) {
        SyncBlockPosition position = event.getInteractPosition();
        Optional<LiveTileEntity> opTE = position.getTileEntity();
        if (!opTE.isPresent()) {
            return;
        }
        if (!(opTE.get() instanceof LiveSignTileEntity)) {
            return;
        }
        LiveSignTileEntity lste = (LiveSignTileEntity) opTE.get();
        ShipsPlugin.getPlugin().getAll(ShipsSign.class).stream().filter(s -> s.isSign(lste)).forEach(s -> {
            if (ShipsSign.LOCKED_SIGNS.stream().anyMatch(b -> b.equals(position))) {
                LivePlayer player = event.getEntity();
                AText text = AdventureMessageConfig.ERROR_SHIPS_SIGN_IS_MOVING.parse();
                player.sendMessage(text);
                return;
            }
            boolean cancel = event.getClickAction() == EntityInteractEvent.PRIMARY_CLICK_ACTION ? s.onPrimaryClick(event.getEntity(), position) : s.onSecondClick(event.getEntity(), position);
            if (cancel) {
                event.setCancelled(true);
            }
        });
    }

    @HEvent
    public void onSignChangeEvent(SignChangeEvent.ByPlayer event) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        if (config.getDisabledWorlds().contains(event.getEntity().getPosition().getWorld())) {
            return;
        }
        boolean register = false;
        Optional<AText> opFirstLine = event.getFrom().getTextAt(0);
        if (!opFirstLine.isPresent()) {
            return;
        }
        AText line = opFirstLine.get();
        ShipsSign sign = ShipsPlugin
                .getPlugin()
                .getAll(ShipsSign.class)
                .stream()
                .filter(s -> s.isSign(event.getFrom().getText()))
                .findFirst()
                .orElse(null);
        if (sign == null) {
            return;
        }
        if (sign instanceof LicenceSign) {
            register = true;
        }
        SignTileEntitySnapshot stes;
        try {
            stes = sign.changeInto(event.getTo());
        } catch (IOException e) {
            event.setCancelled(true);
            event.getEntity().sendMessage(AText.ofPlain("Error: " + e.getMessage()).withColour(NamedTextColours.RED));
            return;
        }
        if (register) {
            Optional<AText> opTypeText = stes.getTextAt(1);
            if (!opTypeText.isPresent()) {
                event.setCancelled(true);
                return;
            }
            String typeText = opTypeText.get().toPlain();
            Optional<ShipType> opType = ShipsPlugin
                    .getPlugin()
                    .getAll(ShipType.class)
                    .stream()
                    .filter(t -> typeText.equalsIgnoreCase(t.getDisplayName()))
                    .findAny();
            if (!opType.isPresent()) {
                event.getEntity().sendMessage(AdventureMessageConfig.ERROR_INVALID_SHIP_TYPE.process(typeText));
                event.setCancelled(true);
                return;
            }
            ShipType<? extends Vessel> type = opType.get();
            String permission = Permissions.getMakePermission(type);
            if (!(event.getEntity().hasPermission(permission) || event.getEntity().hasPermission(Permissions.SHIP_REMOVE_OTHER))) {
                AText text = AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.process(AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.parse(ShipsPlugin.getPlugin().getAdventureMessageConfig()), new AbstractMap.SimpleImmutableEntry<>(event.getEntity(), permission));
                event.getEntity().sendMessage(text);
                event.setCancelled(true);
                return;
            }
            try {
                Optional<AText> opName = stes.getTextAt(2);
                if (!opName.isPresent()) {
                    event.setCancelled(true);
                    return;
                }
                String name = opName.get().toPlain();
                new ShipsIDFinder("ships:" + type.getName().toLowerCase() + "." + name.toLowerCase()).load();
                event.getEntity().sendMessage(AdventureMessageConfig.ERROR_INVALID_SHIP_NAME.process(name));
                event.setCancelled(true);
                return;
            } catch (LoadVesselException ignored) {
            }
            try {
                for (Direction direction : FourFacingDirection.getFourFacingDirections()) {
                    Vessel vessel = new ShipsBlockFinder(event.getPosition().getRelative(direction)).load();
                    event.getEntity().sendMessage(AdventureMessageConfig.ERROR_CANNOT_CREATE_ONTOP.process(vessel));
                    event.setCancelled(true);
                    return;
                }
            } catch (LoadVesselException ignored) {
            }
            int trackSize = config.getDefaultTrackSize();
            ServerBossBar bar = null;
            if (ShipsPlugin.getPlugin().getConfig().isBossBarVisible()) {
                bar = CorePlugin.createBossBar().register(event.getEntity());
            }
            final ServerBossBar finalBar = bar;
            SyncExactPosition bp = event.getEntity().getPosition();
            config
                    .getDefaultFinder()
                    .getConnectedBlocksOvertime(event.getPosition(), new OvertimeBlockFinderUpdate() {
                        @Override
                        public void onShipsStructureUpdated(@NotNull PositionableShipsStructure structure) {
                            if (finalBar != null) {
                                finalBar.setTitle(AText.ofPlain("Complete"));
                            }
                            Vessel vessel = type.createNewVessel(stes, event.getPosition());
                            if (vessel instanceof TeleportToVessel) {
                                ((TeleportToVessel) vessel).setTeleportPosition(bp);
                            }
                            vessel.setStructure(structure);
                            if (vessel instanceof CrewStoredVessel) {
                                ((CrewStoredVessel) vessel).getCrew().put(event.getEntity().getUniqueId(), CrewPermission.CAPTAIN);
                            }
                            VesselCreateEvent.Pre preEvent = new VesselCreateEvent.Pre.BySign(vessel, event.getEntity());
                            CorePlugin.getEventManager().callEvent(preEvent);
                            if (preEvent.isCancelled()) {
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
                        public BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure, @NotNull BlockPosition block) {
                            if (finalBar != null) {
                                AText text = AdventureMessageConfig.BAR_BLOCK_FINDER_ON_FIND.process(currentStructure);
                                int blockAmount = (currentStructure.getPositions().size() + 1);
                                finalBar.setTitle(text);
                                finalBar.setValue(blockAmount, trackSize);
                            }
                            return BlockFindControl.USE;
                        }
                    });
        }
        event.setTo(stes);
    }

    @HEvent
    public void onBlockExplode(BlockChangeEvent.Break.Pre.ByExplosion event) {
        SyncBlockPosition position = event.getPosition();
        List<Direction> directions = new ArrayList<>(Arrays.asList(FourFacingDirection.getFourFacingDirections()));
        directions.add(FourFacingDirection.NONE);
        for (Direction direction : directions) {
            if (!(position.getRelative(direction).getTileEntity().isPresent())) {
                continue;
            }
            LiveTileEntity lte = position.getRelative(direction).getTileEntity().get();
            if (!(lte instanceof LiveSignTileEntity)) {
                continue;
            }
            LiveSignTileEntity sign = (LiveSignTileEntity) lte;
            LicenceSign licenceSign = ShipsPlugin.getPlugin().get(LicenceSign.class).orElseThrow(() -> new IllegalStateException("Could not get Licence sign from register. Something is really wrong"));
            if (!licenceSign.isSign(sign)) {
                continue;
            }
            if (!direction.equals(FourFacingDirection.NONE)) {
                Optional<Direction> opAttachable = position.getRelative(direction).getBlockDetails().get(AttachableKeyedData.class);
                if (!opAttachable.isPresent()) {
                    continue;
                }
                if (!opAttachable.get().getOpposite().equals(direction)) {
                    continue;
                }
            }
            event.setCancelled(true);
            return;
        }
    }

    @HEvent
    public void onBlockBreak(BlockChangeEvent.Break.Pre event) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        List<Direction> list = new ArrayList<>(Arrays.asList(FourFacingDirection.getFourFacingDirections()));
        list.add(FourFacingDirection.NONE);
        SyncBlockPosition position = event.getPosition();
        for (Direction direction : list) {
            SyncBlockPosition pos = position.getRelative(direction);
            if (config.isStructureClickUpdating()) {
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
            if (!(lte instanceof LiveSignTileEntity)) {
                continue;
            }
            LiveSignTileEntity lste = (LiveSignTileEntity) lte;
            LicenceSign licenceSign = ShipsPlugin.getPlugin().get(LicenceSign.class).orElseThrow(() -> new IllegalStateException("Licence sign could not be found from register. Something is really wrong."));
            if (!licenceSign.isSign(lste)) {
                continue;
            }
            if (!direction.equals(FourFacingDirection.NONE)) {
                Optional<Direction> opAttachable = position.getRelative(direction).getBlockDetails().get(AttachableKeyedData.class);
                if (!opAttachable.isPresent()) {
                    continue;
                }
                if (!opAttachable.get().getOpposite().equals(direction)) {
                    continue;
                }
            }
            Optional<Vessel> opVessel = licenceSign.getShip(lste);
            if (!opVessel.isPresent()) {
                continue;
            }
            Vessel vessel = opVessel.get();
            if (vessel instanceof CrewStoredVessel && event instanceof BlockChangeEvent.Break.Pre.ByPlayer) {
                LivePlayer player = ((BlockChangeEvent.Break.Pre.ByPlayer) event).getEntity();
                if (!(((CrewStoredVessel) vessel).getPermission(player.getUniqueId()).canRemove() || (player.hasPermission(Permissions.ABSTRACT_SHIP_MOVE_OTHER)))) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (vessel instanceof FileBasedVessel) {
                File file = ((FileBasedVessel) vessel).getFile();
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    event.setCancelled(true);
                    e.printStackTrace();
                }
            }
            ShipsPlugin.getPlugin().unregisterVessel(vessel);
            if (event instanceof BlockChangeEvent.Break.Pre.ByPlayer) {
                LivePlayer player = ((BlockChangeEvent.Break.Pre.ByPlayer) event).getEntity();
                player.sendMessage(AText.ofPlain(Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown") + " removed successfully"));
            }
            return;
        }
    }
}
