package org.ships.event.listener;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.TranslateCore;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.User;
import org.core.entity.scene.droppeditem.DroppedItem;
import org.core.event.EventListener;
import org.core.event.EventPriority;
import org.core.event.HEvent;
import org.core.event.events.block.BlockChangeEvent;
import org.core.event.events.block.ExplosionEvent;
import org.core.event.events.block.tileentity.SignChangeEvent;
import org.core.event.events.command.PlayerCommandEvent;
import org.core.event.events.connection.ClientConnectionEvent;
import org.core.event.events.entity.EntityInteractEvent;
import org.core.event.events.entity.EntitySpawnEvent;
import org.core.schedule.unit.TimeUnit;
import org.core.utils.BarUtils;
import org.core.utils.ComponentUtils;
import org.core.utils.Else;
import org.core.vector.type.Vector3;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.BlockSnapshot;
import org.core.world.position.block.details.data.keyed.AttachableKeyedData;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.block.details.data.keyed.TileEntityKeyedData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.blocks.DefaultBlockList;
import org.ships.config.blocks.instruction.CollideType;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.config.messages.Messages;
import org.ships.event.vessel.create.VesselCreateEvent;
import org.ships.exceptions.NoLicencePresent;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.permissions.Permissions;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.assits.FileBasedVessel;
import org.ships.vessel.common.assits.TeleportToVessel;
import org.ships.vessel.common.assits.WaterType;
import org.ships.vessel.common.finder.IdVesselFinder;
import org.ships.vessel.common.finder.VesselBlockFinder;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.flag.PlayerStatesFlag;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.ShipTypes;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.ShipsVessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.sign.ShipsSign;
import org.ships.vessel.sign.ShipsSigns;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CoreEventListener implements EventListener {

    @HEvent
    public void onPlayerCommand(PlayerCommandEvent event) {
        Optional<String> opLoginCommand = ShipsPlugin.getPlugin().getConfig().getDefaultLoginCommand();
        if (opLoginCommand.isEmpty()) {
            return;
        }

        if (!String.join(", ", event.getCommand()).toLowerCase().startsWith(opLoginCommand.get().toLowerCase())) {
            return;
        }
        TranslateCore
                .getScheduleManager()
                .schedule()
                .setDelay(2)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setRunner((sch) -> this.onPlayerJoin(event.getEntity()))
                .buildDelayed(ShipsPlugin.getPlugin())
                .run();
    }

    @HEvent
    public void onPlaceEvent(BlockChangeEvent.Place event) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        DefaultBlockList list = ShipsPlugin.getPlugin().getBlockList();
        if (!config.isStructureClickUpdating()) {
            return;
        }
        ASyncBlockPosition asyncPos = event.getPosition().toAsyncPosition();
        Optional<CompletableFuture<Map.Entry<PositionableShipsStructure, Optional<Vessel>>>> opFuture = Arrays
                .stream(Direction.withYDirections(FourFacingDirection.getFourFacingDirections()))
                .map(asyncPos::getRelative)
                .filter(position -> list.getBlockInstruction(position.getBlockType()).getCollide()
                        != CollideType.MATERIAL)
                .map(VesselBlockFinder::findOvertime)
                .findAny();
        if (opFuture.isEmpty()) {
            return;
        }
        CompletableFuture<Map.Entry<PositionableShipsStructure, Optional<Vessel>>> future = opFuture.get();
        future
                .thenApply(Map.Entry::getValue)
                .thenAccept(opVessel -> opVessel.ifPresent(
                        vessel -> vessel.getStructure().addPositionRelativeToWorld(asyncPos)));
    }

    @HEvent
    public void onEntitySpawnEvent(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof DroppedItem)) {
            return;
        }
        VesselBlockFinder
                .findOvertime(event.getPosition().toBlockPosition())
                .thenApply(entry -> entry
                        .getValue()
                        .filter(v -> v
                                .getValue(MovingFlag.class)
                                .filter(context -> !context.getMovingStructure().isEmpty())
                                .isPresent()))
                .thenAccept(opVessel -> {
                    if (opVessel.isPresent()) {
                        event.getEntity().remove();
                    }
                });
    }

    @HEvent
    public void onPlayerJoinEvent(ClientConnectionEvent.Incoming.Joined event) {
        this.onPlayerJoin(event.getEntity());
    }

    public void onPlayerJoin(LivePlayer player) {
        for (Vessel vessel : ShipsPlugin.getPlugin().getVessels()) {
            if (!(vessel instanceof ShipsVessel)) {
                continue;
            }
            Optional<PlayerStatesFlag> opFlag = vessel.get(PlayerStatesFlag.class);
            if (opFlag.isEmpty()) {
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
                player.sendMessage(AdventureMessageConfig.INFO_PLAYER_SPAWNED_ON_SHIP.processMessage(vessel));
                map.remove(player.getUniqueId());
                vessel.set(PlayerStatesFlag.class, map);
            }
            Optional<MovingFlag> opMovingFlag = vessel.get(MovingFlag.class);
            player.setGravity(opMovingFlag.isEmpty() || opMovingFlag.get().getValue().isEmpty());
        }
    }

    public void onPlayerLeave(User player, Position<Double> position) {
        BlockPosition block = position.toBlockPosition().getRelative(FourFacingDirection.DOWN);
        ShipsPlugin
                .getPlugin()
                .getVessels()
                .parallelStream()
                .filter(vessel -> vessel.getValue(PlayerStatesFlag.class).isPresent())
                .map(vessel -> vessel.getValue(PlayerStatesFlag.class).orElse(new HashMap<>()))
                .forEach(map -> map.remove(player.getUniqueId()));

        UUID uuid = player.getUniqueId();

        VesselBlockFinder.findOvertime(block).thenAccept(entry -> {
            Optional<Vessel> opVessel = entry.getValue();
            if (opVessel.isEmpty()) {
                return;
            }
            Vessel vessel = opVessel.get();
            PlayerStatesFlag flag = vessel.get(PlayerStatesFlag.class).orElseGet(PlayerStatesFlag::new);
            Map<UUID, Vector3<Double>> map = flag.getValue().orElseGet(HashMap::new);
            Vector3<Double> vector = position.getPosition().minus(vessel.getPosition().toExactPosition().getPosition());
            if (map.containsKey(uuid)) {
                map.replace(uuid, vector);
            } else {
                map.put(uuid, vector);
            }
            flag.setValue(map);
            vessel.set(flag);
            vessel.save();
        });
    }

    @HEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeaveEvent(ClientConnectionEvent.Leave event) {
        this.onPlayerLeave(event.getEntity(), event.getEntity().getPosition());
    }

    @HEvent
    public void onPlayerInteractWithBlock(EntityInteractEvent.WithBlock.AsPlayer event) {
        SyncBlockPosition position = event.getInteractPosition();
        Optional<LiveTileEntity> opTE = position.getTileEntity();
        if (opTE.isEmpty()) {
            return;
        }
        if (!(opTE.get() instanceof LiveSignTileEntity)) {
            return;
        }
        SignTileEntity lste = (SignTileEntity) opTE.get();
        CollideType collideType = ShipsPlugin
                .getPlugin()
                .getBlockList()
                .getBlockInstruction(position.getBlockType())
                .getCollide();
        if (collideType != CollideType.MATERIAL) {
            return;
        }
        ShipsSigns.signs().stream().filter(s -> s.isSign(lste)).forEach(s -> {
            if (ShipsPlugin.getPlugin().getLockedSignManager().isLocked(position)) {
                LivePlayer player = event.getEntity();
                Component text = Messages.ERROR_SHIPS_SIGN_IS_MOVING.parseMessage();
                player.sendMessage(text);
                return;
            }
            boolean cancel = event.getClickAction() == EntityInteractEvent.PRIMARY_CLICK_ACTION ? s.onPrimaryClick(
                    event.getEntity(), position) : s.onSecondClick(event.getEntity(), position);
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
        CollideType collideType = ShipsPlugin
                .getPlugin()
                .getBlockList()
                .getBlockInstruction(event.getPosition().getBlockType())
                .getCollide();
        if (collideType != CollideType.MATERIAL) {
            return;
        }
        boolean register = false;
        SignSide side = event.getChangingSide();
        Optional<Component> opFirstLine = side.getLineAt(0);
        if (opFirstLine.isEmpty()) {
            return;
        }
        ShipsSign sign = ShipsSigns
                .signs()
                .stream()
                .filter(s -> s.isSign(event.getChangingSide().getLines()))
                .findFirst()
                .orElse(null);
        if (sign == null) {
            return;
        }

        if (sign instanceof LicenceSign) {
            register = true;
        }
        try {
            sign.changeInto(event.getChangingSide());
        } catch (IOException e) {
            event.setCancelled(true);
            event.getEntity().sendMessage(Component.text("Error: " + e.getMessage()).color(NamedTextColor.RED));
            return;
        }
        if (register) {
            Optional<Component> opTypeText = side.getLineAt(1);
            if (opTypeText.isEmpty()) {
                event.setCancelled(true);
                return;
            }
            String typeText = ComponentUtils.toPlain(opTypeText.get());
            Optional<ShipType<?>> opType = ShipTypes
                    .shipTypes()
                    .stream()
                    .filter(t -> typeText.equalsIgnoreCase(t.getDisplayName()))
                    .findAny();
            if (opType.isEmpty()) {
                event.getEntity().sendMessage(Messages.ERROR_INVALID_SHIP_TYPE.processMessage(typeText));
                event.setCancelled(true);
                return;
            }
            ShipType<? extends Vessel> type = opType.get();
            if (!(event.getEntity().hasPermission(type.getMakePermission()) || event
                    .getEntity()
                    .hasPermission(Permissions.SHIP_REMOVE_OTHER))) {
                Component text = Messages.ERROR_PERMISSION_MISS_MATCH.processMessage(
                        Messages.ERROR_PERMISSION_MISS_MATCH.parseMessage(
                                ShipsPlugin.getPlugin().getAdventureMessageConfig()),
                        new AbstractMap.SimpleImmutableEntry<>(event.getEntity(),
                                                               type.getMakePermission().getPermissionValue()));
                event.getEntity().sendMessage(text);
                event.setCancelled(true);
                return;
            }
            try {
                Optional<Component> opName = side.getLineAt(2);
                if (opName.isEmpty()) {
                    event.setCancelled(true);
                    return;
                }
                String name = ComponentUtils.toPlain(opName.get());
                IdVesselFinder.load("ships:" + type.getName().toLowerCase() + "." + name.toLowerCase());
                event.getEntity().sendMessage(Messages.ERROR_INVALID_SHIP_NAME.processMessage(name));
                event.setCancelled(true);
                return;
            } catch (LoadVesselException ignored) {
            }

            try {
                for (Direction direction : FourFacingDirection.getFourFacingDirections()) {
                    SyncBlockPosition position = event.getPosition().getRelative(direction);
                    Vessel vessel = VesselBlockFinder.findCached(position);
                    event.getEntity().sendMessage(Messages.ERROR_CANNOT_CREATE_ONTOP.processMessage(vessel));
                    event.setCancelled(true);
                    return;
                }
            } catch (LoadVesselException ignored) {
            }
            int trackSize = config.getDefaultTrackSize();
            BossBar bar = null;
            if (ShipsPlugin.getPlugin().getConfig().isBossBarVisible()) {
                bar = BossBar.bossBar(Component.empty(), 0, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
            }
            final BossBar finalBar = bar;
            SyncExactPosition bp = event.getEntity().getPosition();

            config.getDefaultFinder().getConnectedBlocksOvertime(event.getPosition(), (currentStructure, block) -> {
                if (finalBar != null) {
                    TranslateCore
                            .getScheduleManager()
                            .schedule()
                            .setDisplayName("OnBlockFind Message")
                            .setRunner((sch) -> {
                                if ((finalBar.progress() * 100) > trackSize) {
                                    return;
                                }
                                Component text = Messages.BAR_BLOCK_FINDER_ON_FIND.processMessage(currentStructure);
                                int blockAmount = (currentStructure.size() + 1);
                                float progress = (trackSize / (float) Math.max(trackSize, blockAmount));
                                finalBar.name(text);
                                finalBar.progress(progress);
                            })
                            .buildDelayed(ShipsPlugin.getPlugin())
                            .run();
                }
                return OvertimeBlockFinderUpdate.BlockFindControl.USE;
            }).thenCompose(structure -> {
                if (finalBar != null) {
                    finalBar.name(Component.text("Complete"));
                }
                Vessel vessel = type.createNewVessel(side, event.getPosition());
                if (vessel instanceof TeleportToVessel) {
                    ((TeleportToVessel) vessel).setTeleportPosition(bp);
                }
                vessel.setStructure(structure);
                if (vessel instanceof WaterType) {
                    return structure.fillAir().thenApply(str -> vessel);
                }
                return CompletableFuture.completedFuture(vessel);
            }).thenAccept(vessel -> {
                if (vessel instanceof CrewStoredVessel) {
                    ((CrewStoredVessel) vessel).getCrew().put(event.getEntity().getUniqueId(), CrewPermission.CAPTAIN);
                }
                VesselCreateEvent.Pre preEvent = new VesselCreateEvent.Pre.BySign(vessel, event.getEntity());
                TranslateCore.getEventManager().callEvent(preEvent);
                if (preEvent.isCancelled()) {
                    if (finalBar != null) {
                        BarUtils.getPlayers(finalBar).forEach(user -> user.hideBossBar(finalBar));
                    }
                    event.setCancelled(true);
                    TranslateCore.getScheduleManager().schedule().setDisplayName("event cancelled").setRunner((sch) -> {
                        Optional<LiveTileEntity> opTileEntity = event.getPosition().getTileEntity();
                        if (opTileEntity.isEmpty()) {
                            return;
                        }
                        if (!(opTileEntity.get() instanceof SignTileEntity)) {
                            return;
                        }
                        SignTileEntity ste = (SignTileEntity) opTileEntity.get();
                        ste.getSide(event.getChangingSide().isFront()).setLines(Collections.emptyList());
                    }).buildDelayed(ShipsPlugin.getPlugin()).run();
                    return;
                }
                vessel.setLoading(false);
                vessel.save();
                ShipsPlugin.getPlugin().registerVessel(vessel);
                VesselCreateEvent postEvent = new VesselCreateEvent.Post.BySign(vessel, event.getEntity());
                TranslateCore.getEventManager().callEvent(postEvent);
                if (finalBar != null) {
                    BarUtils.getPlayers(finalBar).forEach(player -> player.hideBossBar(finalBar));
                }
            });
        }
    }

    @HEvent
    public void onBlockExplode(ExplosionEvent.Post event) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        if (!config.isPreventingExplosions()) {
            return;
        }
        LicenceSign licenceSign = ShipsSigns.LICENCE;
        Optional<BlockSnapshot.SyncBlockSnapshot> opLicenceSignSnapshot = event
                .getExplosion()
                .getBlocks()
                .stream()
                .filter(sbs -> sbs.get(KeyedData.TILED_ENTITY).isPresent())
                .filter(sbs -> {
                    TileEntitySnapshot<? extends TileEntity> tileEntity = sbs
                            .get(KeyedData.TILED_ENTITY)
                            .orElseThrow(() -> new RuntimeException("Broken logic"));
                    return tileEntity instanceof SignTileEntity;
                })
                .filter(sbs -> licenceSign.isSign((SignTileEntity) sbs
                        .get(KeyedData.TILED_ENTITY)
                        .orElseThrow(() -> new RuntimeException("Broken logic"))))
                .findAny();

        if (opLicenceSignSnapshot.isEmpty()) {
            return;
        }

        Set<BlockSnapshot.SyncBlockSnapshot> restoreBlocks = new HashSet<>(
                Collections.singleton(opLicenceSignSnapshot.get()));

        Optional<Direction> opSign = opLicenceSignSnapshot.get().get(KeyedData.ATTACHABLE);
        if (opSign.isPresent()) {
            SyncBlockPosition attachedPosition = opLicenceSignSnapshot.get().getPosition().getRelative(opSign.get());
            Optional<BlockSnapshot.SyncBlockSnapshot> attached = event
                    .getExplosion()
                    .getBlocks()
                    .stream()
                    .filter(sbs -> sbs.getPosition().equals(attachedPosition))
                    .findAny();
            attached.ifPresent(restoreBlocks::add);
        }

        if (!restoreBlocks.isEmpty()) {
            TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setRunner((sch) -> restoreBlocks.forEach(BlockSnapshot.SyncBlockSnapshot::restore))
                    .setDisplayName("restoring blocks")
                    .setDelay(1)
                    .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                    .buildDelayed(ShipsPlugin.getPlugin())
                    .run();
        }
    }

    @HEvent
    public void onBlockBreak(BlockChangeEvent.Break.Pre event) {
        if (event instanceof BlockChangeEvent.Break.Pre.ByExplosion) {
            return;
        }
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        BlockDetails beforeDetails = event.getBeforeState();
        LicenceSign licenceSign = ShipsSigns.LICENCE;
        Collection<Direction> list = new ArrayList<>(Arrays.asList(FourFacingDirection.getFourFacingDirections()));
        list.add(FourFacingDirection.NONE);
        SyncBlockPosition position = event.getPosition();
        for (Direction direction : list) {
            SyncBlockPosition pos = position.getRelative(direction);
            Optional<TileEntity> opTileEntity = pos.getTileEntity().map(lte -> lte);
            if (opTileEntity.isEmpty() && direction.equals(FourFacingDirection.NONE)) {
                //little hack -> breaking block isn't guaranteed to get the tileEntity -> this will get the tileEntity from the event
                opTileEntity = event.getBeforeState().get(TileEntityKeyedData.class).map(tes -> tes);
            }
            if (opTileEntity.isEmpty()) {
                continue;
            }
            TileEntity lte = opTileEntity.get();
            if (!(lte instanceof SignTileEntity)) {
                continue;
            }
            SignTileEntity lste = (SignTileEntity) lte;
            if (!licenceSign.isSign(lste)) {
                continue;
            }
            if (!direction.equals(FourFacingDirection.NONE)) {
                Optional<Direction> opAttachable = position
                        .getRelative(direction)
                        .getBlockDetails()
                        .get(AttachableKeyedData.class);
                if (opAttachable.isEmpty()) {
                    continue;
                }
                if (!opAttachable.get().getOpposite().equals(direction)) {
                    continue;
                }
            }

            VesselBlockFinder.findOvertime(pos).thenAccept(entry -> {
                Optional<Vessel> opVessel = entry.getValue();
                if (opVessel.isEmpty()) {
                    return;
                }
                Vessel vessel = opVessel.get();
                if (config.isStructureClickUpdating()) {
                    vessel.getStructure().removePositionRelativeToWorld(event.getPosition());
                }
                if (vessel instanceof CrewStoredVessel && event instanceof BlockChangeEvent.Break.Pre.ByPlayer) {
                    BlockChangeEvent.Break.Pre.ByPlayer eventBreak = (BlockChangeEvent.Break.Pre.ByPlayer) event;
                    LivePlayer player = eventBreak.getEntity();
                    CrewStoredVessel csVessel = (CrewStoredVessel) vessel;
                    if (!(csVessel.getPermission(player.getUniqueId()).canRemove() || (player.hasPermission(
                            vessel.getType().getMoveOtherPermission())))) {
                        event.getPosition().setBlock(beforeDetails);
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
                    BlockChangeEvent.Break.Pre.ByPlayer eventBreak = (BlockChangeEvent.Break.Pre.ByPlayer) event;
                    LivePlayer player = eventBreak.getEntity();
                    String name = vessel
                            .getCachedName()
                            .orElseGet(() -> Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown"));
                    player.sendMessage(Component.text(name + " removed successfully"));
                }

            });
        }
    }
}
