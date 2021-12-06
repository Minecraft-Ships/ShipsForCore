package org.ships.event.listener;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.living.human.player.LivePlayer;
import org.core.entity.living.human.player.Player;
import org.core.entity.living.human.player.User;
import org.core.entity.scene.droppeditem.DroppedItem;
import org.core.event.EventListener;
import org.core.event.EventPriority;
import org.core.event.HEvent;
import org.core.event.events.block.BlockChangeEvent;
import org.core.event.events.block.ExplosionEvent;
import org.core.event.events.block.tileentity.SignChangeEvent;
import org.core.event.events.connection.ClientConnectionEvent;
import org.core.event.events.entity.EntityCommandEvent;
import org.core.event.events.entity.EntityEvent;
import org.core.event.events.entity.EntityInteractEvent;
import org.core.event.events.entity.EntitySpawnEvent;
import org.core.schedule.unit.TimeUnit;
import org.core.utils.Else;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.details.BlockSnapshot;
import org.core.world.position.block.details.data.keyed.AttachableKeyedData;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
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
import org.ships.vessel.common.assits.WaterType;
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
import java.util.stream.Collectors;

public class CoreEventListener implements EventListener {

    @HEvent
    public void onPlayerCommand(EntityCommandEvent event) {
        Optional<String> opLoginCommand = ShipsPlugin.getPlugin().getConfig().getDefaultLoginCommand();
        if (!opLoginCommand.isPresent()) {
            return;
        }

        if (!String
                .join(", ", event.getCommand())
                .toLowerCase()
                .startsWith(opLoginCommand.get().toLowerCase())) {
            return;
        }
        TranslateCore
                .createSchedulerBuilder()
                .setDelay(2)
                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                .setExecutor(() -> this.onPlayerJoin(event.getEntity()))
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
            if (list.getBlockInstruction(position.getBlockType()).getCollideType()!=BlockInstruction.CollideType.MATERIAL) {
                continue;
            }

            new ShipsOvertimeBlockFinder(position)
                    .loadOvertime(vessel -> vessel.getStructure().addPosition(position), structure -> {
                    });
        }
    }

    @HEvent
    public void onEntitySpawnEvent(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof DroppedItem)) {
            return;
        }
        new ShipsOvertimeBlockFinder(event.getPosition().toBlockPosition())
                .fromVessels(ShipsPlugin
                        .getPlugin()
                        .getVessels()
                        .stream()
                        .filter(e -> {
                            Optional<MovementContext> opValue = e.getValue(MovingFlag.class);
                            return opValue.filter(movementContext -> !movementContext.getMovingStructure().isEmpty()).isPresent();
                        })
                        .collect(Collectors.toSet()))
                .loadOvertime(vessel -> TranslateCore
                                .createSchedulerBuilder()
                                .setDelay(0)
                                .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                                .setExecutor(() -> event.getEntity().remove())
                                .setDisplayName("remove entity")
                                .build(ShipsPlugin.getPlugin())
                                .run(),
                        structure -> {

                        });
    }

    @HEvent
    public void onPlayerJoinEvent(ClientConnectionEvent.Incoming.Joined event) {
        this.onPlayerJoin(event.getEntity());
    }

    public void onPlayerJoin(Player<?> player) {
        for (Vessel vessel : ShipsPlugin.getPlugin().getVessels()) {
            if (!(vessel instanceof ShipsVessel)) {
                continue;
            }
            Optional<PlayerStatesFlag> opFlag = vessel.get(PlayerStatesFlag.class);
            if (!opFlag.isPresent()) {
                continue;
            }
            Map<UUID, Vector3<Double>> map = opFlag.get().getValue().orElse(new HashMap<>());
            Vector3<Double> vector = map.get(player.getUniqueId());
            if (vector==null) {
                continue;
            }
            SyncBlockPosition sPos = vessel.getPosition();
            SyncExactPosition position = sPos.toExactPosition().getRelative(vector);
            if (!position.equals(player.getPosition())) {
                player.setPosition(position);
                map.remove(player.getUniqueId());
                vessel.set(PlayerStatesFlag.class, map);
            }
            Optional<MovingFlag> opMovingFlag = vessel.get(MovingFlag.class);
            player.setGravity(!opMovingFlag.isPresent() || !opMovingFlag.get().getValue().isPresent());


        }
    }

    public void onPlayerLeave(User player, ExactPosition position) {
        BlockPosition block = position.toBlockPosition().getRelative(FourFacingDirection.DOWN);
        ShipsPlugin
                .getPlugin()
                .getVessels()
                .stream()
                .filter(vessel -> vessel.getValue(PlayerStatesFlag.class).isPresent())
                .map(vessel -> vessel
                        .getValue(PlayerStatesFlag.class)
                        .orElse(new HashMap<>())
                )
                .forEach(map -> map.remove(player.getUniqueId()));


        new ShipsOvertimeBlockFinder(block)
                .loadOvertime(vessel -> {
                    if (!(vessel instanceof ShipsVessel)) {
                        return;
                    }
                    PlayerStatesFlag flag = vessel
                            .get(PlayerStatesFlag.class)
                            .orElse(new PlayerStatesFlag());
                    Map<UUID, Vector3<Double>> map = flag
                            .getValue()
                            .orElse(new HashMap<>());
                    UUID uuid = player.getUniqueId();
                    Vector3<Double> vector = position
                            .getPosition()
                            .minus(vessel.getPosition().toExactPosition().getPosition());

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
        this.onPlayerLeave(event.getEntity(), event.getEntity().getPosition());
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
        SignTileEntity lste = (SignTileEntity) opTE.get();
        BlockInstruction.CollideType collideType = ShipsPlugin.getPlugin().getBlockList().getBlockInstruction(position.getBlockType()).getCollideType();
        if (collideType!=BlockInstruction.CollideType.MATERIAL) {
            return;
        }
        ShipsPlugin.getPlugin().getAll(ShipsSign.class).stream().filter(s -> s.isSign(lste)).forEach(s -> {
            if (ShipsSign.LOCKED_SIGNS.stream().anyMatch(b -> b.equals(position))) {
                LivePlayer player = event.getEntity();
                AText text = AdventureMessageConfig.ERROR_SHIPS_SIGN_IS_MOVING.parse();
                player.sendMessage(text);
                return;
            }
            boolean cancel = event.getClickAction()==EntityInteractEvent.PRIMARY_CLICK_ACTION ? s.onPrimaryClick(event.getEntity(), position):s.onSecondClick(event.getEntity(), position);
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
        BlockInstruction.CollideType collideType = ShipsPlugin.getPlugin().getBlockList().getBlockInstruction(event.getPosition().getBlockType()).getCollideType();
        if (collideType!=BlockInstruction.CollideType.MATERIAL) {
            return;
        }
        boolean register = false;
        Optional<AText> opFirstLine = event.getFrom().getTextAt(0);
        if (!opFirstLine.isPresent()) {
            return;
        }
        ShipsSign sign = ShipsPlugin
                .getPlugin()
                .getAll(ShipsSign.class)
                .stream()
                .filter(s -> s.isSign(event.getFrom().getText()))
                .findFirst()
                .orElse(null);
        if (sign==null) {
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
            Optional<ShipType<?>> opType = ShipsPlugin.getPlugin().getAllShipTypes()
                    .stream()
                    .filter(t -> typeText.equalsIgnoreCase(t.getDisplayName()))
                    .findAny();
            if (!opType.isPresent()) {
                event.getEntity().sendMessage(AdventureMessageConfig.ERROR_INVALID_SHIP_TYPE.process(typeText));
                event.setCancelled(true);
                return;
            }
            ShipType<? extends Vessel> type = opType.get();
            if (!(event.getEntity().hasPermission(type.getMakePermission()) || event.getEntity().hasPermission(Permissions.SHIP_REMOVE_OTHER))) {
                AText text =
                        AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.process(AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.parse(ShipsPlugin.getPlugin().getAdventureMessageConfig()), new AbstractMap.SimpleImmutableEntry<>(event.getEntity(), type.getMakePermission().getPermissionValue()));
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
                bar = TranslateCore.createBossBar().register(event.getEntity());
            }
            final ServerBossBar finalBar = bar;
            SyncExactPosition bp = event.getEntity().getPosition();
            config
                    .getDefaultFinder()
                    .getConnectedBlocksOvertime(event.getPosition(), new OvertimeBlockFinderUpdate() {
                        @Override
                        public void onShipsStructureUpdated(@NotNull PositionableShipsStructure structure) {
                            if (finalBar!=null) {
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
                            TranslateCore.getEventManager().callEvent(preEvent);
                            if (preEvent.isCancelled()) {
                                if (finalBar!=null) {
                                    finalBar.deregisterPlayers();
                                }
                                event.setCancelled(true);
                                TranslateCore
                                        .createSchedulerBuilder()
                                        .setDisplayName("event cancelled")
                                        .setExecutor(() -> {
                                            Optional<LiveTileEntity> opTileEntity = event.getPosition().getTileEntity();
                                            if (!opTileEntity.isPresent()) {
                                                return;
                                            }
                                            if (!(opTileEntity.get() instanceof SignTileEntity)) {
                                                return;
                                            }
                                            SignTileEntity ste = (SignTileEntity) opTileEntity.get();
                                            ste.setText(Collections.emptySet());
                                        })
                                        .build(ShipsPlugin.getPlugin())
                                        .run();
                                return;
                            }
                            vessel.setLoading(false);
                            vessel.save();
                            ShipsPlugin.getPlugin().registerVessel(vessel);
                            VesselCreateEvent postEvent = new VesselCreateEvent.Post.BySign(vessel, event.getEntity());
                            TranslateCore.getEventManager().callEvent(postEvent);
                            if (finalBar!=null) {
                                finalBar.deregisterPlayers();
                            }
                            if (vessel.getType() instanceof WaterType) {
                                structure.addAir(vessel::setStructure);
                            }
                        }

                        @Override
                        public BlockFindControl onBlockFind(@NotNull PositionableShipsStructure currentStructure, @NotNull BlockPosition block) {
                            if (finalBar!=null) {
                                TranslateCore
                                        .createSchedulerBuilder()
                                        .setDisplayName("OnBlockFind Message")
                                        .setExecutor(() -> {
                                            if (finalBar.getValue() > trackSize) {
                                                return;
                                            }
                                            AText text = AdventureMessageConfig.BAR_BLOCK_FINDER_ON_FIND.process(currentStructure);
                                            int blockAmount = (currentStructure.getPositions().size() + 1);
                                            finalBar.setTitle(text);
                                            finalBar.setValue(blockAmount, trackSize);
                                        })
                                        .build(ShipsPlugin.getPlugin());
                            }
                            return BlockFindControl.USE;
                        }
                    });
        }
        event.setTo(stes);
    }

    @HEvent
    public void onBlockExplode(ExplosionEvent.Post event) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        if (!config.isPreventingExplosions()) {
            return;
        }
        LicenceSign licenceSign =
                ShipsPlugin.getPlugin().get(LicenceSign.class).orElseThrow(() -> new RuntimeException("Could not " +
                        "find licence sign? is it registered?"));
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
                .filter(sbs -> licenceSign
                        .isSign((SignTileEntity) sbs
                                .get(KeyedData.TILED_ENTITY)
                                .orElseThrow(() -> new RuntimeException("Broken logic"))))
                .findAny();

        if (opLicenceSignSnapshot.isEmpty()) {
            return;
        }

        Set<BlockSnapshot.SyncBlockSnapshot> restoreBlocks =
                new HashSet<>(Collections.singleton(opLicenceSignSnapshot.get()));

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
                    .createSchedulerBuilder()
                    .setExecutor(() -> restoreBlocks.forEach(BlockSnapshot.SyncBlockSnapshot::restore))
                    .setDisplayName("restoring blocks")
                    .setDelay(1)
                    .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                    .build(ShipsPlugin.getPlugin())
                    .run();
        }
    }

    @HEvent
    public void onBlockBreak(BlockChangeEvent.Break.Pre event) {
        if (event instanceof BlockChangeEvent.Break.Pre.ByExplosion) {
            return;
        }
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        LicenceSign licenceSign = ShipsPlugin
                .getPlugin()
                .get(LicenceSign.class)
                .orElseThrow(() -> new IllegalStateException("Licence sign could not be found from register. Something is really wrong."));
        Collection<Direction> list = new ArrayList<>(Arrays.asList(FourFacingDirection.getFourFacingDirections()));
        list.add(FourFacingDirection.NONE);
        SyncBlockPosition position = event.getPosition();
        for (Direction direction : list) {
            SyncBlockPosition pos = position.getRelative(direction);
            Optional<LiveTileEntity> opTileEntity = pos.getTileEntity();
            if (opTileEntity.isEmpty()) {
                continue;
            }
            LiveTileEntity lte = opTileEntity.get();
            if (!(lte instanceof LiveSignTileEntity)) {
                continue;
            }
            SignTileEntity lste = (SignTileEntity) lte;
            if (!licenceSign.isSign(lste)) {
                continue;
            }
            if (!direction.equals(FourFacingDirection.NONE)) {
                Optional<Direction> opAttachable = position.getRelative(direction).getBlockDetails().get(AttachableKeyedData.class);
                if (opAttachable.isEmpty()) {
                    continue;
                }
                if (!opAttachable.get().getOpposite().equals(direction)) {
                    continue;
                }
            }
            Optional<Vessel> opVessel = licenceSign.getShip(lste);
            if (opVessel.isEmpty()) {
                continue;
            }
            Vessel vessel = opVessel.get();
            if (vessel instanceof CrewStoredVessel && event instanceof BlockChangeEvent.Break.Pre.ByPlayer) {
                LivePlayer player = ((EntityEvent<LivePlayer>) event).getEntity();
                if (!(
                        ((CrewStoredVessel) vessel).getPermission(player.getUniqueId()).canRemove() ||
                                (player.hasPermission(vessel.getType().getMoveOtherPermission())))
                ) {
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
                LivePlayer player = ((EntityEvent<LivePlayer>) event).getEntity();
                player.sendMessage(AText.ofPlain(Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown") + " removed successfully"));
            }
            return;
        }
        if (config.isStructureClickUpdating()) {
            new ShipsOvertimeBlockFinder(event.getPosition())
                    .loadOvertime(vessel -> vessel.getStructure().removePosition(event.getPosition()),
                            structure -> {
                            });
        }
    }
}
