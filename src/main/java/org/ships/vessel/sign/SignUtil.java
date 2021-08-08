package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.source.viewer.CommandViewer;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.exceptions.MoveException;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.movement.MovementContext;
import org.ships.movement.result.FailedMovement;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.loader.ShipsBlockFinder;
import org.ships.vessel.common.loader.ShipsOvertimeUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.AbstractMap;
import java.util.function.Consumer;

public interface SignUtil {

    interface MovementReady {

        void onMovementReady(MovementContext context, Vessel vessel, Consumer<Throwable> throwableConsumer);
    }

    class OnOvertimeAutoUpdate extends ShipsOvertimeUpdateBlockLoader {

        private final MovementContext context;
        private final int trackLimit;
        private final LivePlayer player;
        private final MovementReady movementReady;

        public OnOvertimeAutoUpdate(SyncBlockPosition sign, MovementContext context, LivePlayer player, MovementReady ready, int trackLimit) {
            super(sign);
            this.context = context;
            this.trackLimit = trackLimit;
            this.player = player;
            this.movementReady = ready;
        }

        @Override
        protected void onStructureUpdate(Vessel vessel) {
            SignUtil.postMovementReady(this.context, vessel, this.player, this.original, this.movementReady);
        }

        @Override
        protected OvertimeBlockFinderUpdate.BlockFindControl onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
            if (context.getBar().isPresent()) {
                ServerBossBar bar = context.getBar().get();
                int foundBlocks = currentStructure.getPositions().size() + 1;
                try {
                    bar.setValue(foundBlocks, trackLimit);
                } catch (IllegalArgumentException ignore) {
                }
            }
            return OvertimeBlockFinderUpdate.BlockFindControl.USE;
        }

        @Override
        protected void onExceptionThrown(LoadVesselException e) {
            ShipsSign.LOCKED_SIGNS.remove(this.original);
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            if (e instanceof UnableToFindLicenceSign) {
                UnableToFindLicenceSign e1 = (UnableToFindLicenceSign) e;
                this.player.sendMessage(AText.ofPlain(e1.getReason()).withColour(NamedTextColours.RED));
                e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
                CorePlugin.createSchedulerBuilder().setDelay(5).setDisplayName("bedrock reset").setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
            } else {
                this.player.sendMessage(AText.ofPlain(e.getReason()).withColour(NamedTextColours.RED));
            }
        }
    }

    static void postMovementReady(MovementContext context, Vessel vessel, LivePlayer player, SyncBlockPosition position, MovementReady ready) {
        if (vessel instanceof CrewStoredVessel) {
            context.getBar().ifPresent(bar -> bar.setTitle(AText.ofPlain("Checking permissions")));
            CrewStoredVessel stored = (CrewStoredVessel) vessel;
            if (!((stored.getPermission(player.getUniqueId()).canMove() && player.hasPermission(Permissions.getMovePermission(stored.getType()))) || player.hasPermission(Permissions.getOtherMovePermission(stored.getType())))) {
                if (!stored.getPermission(player.getUniqueId()).canMove()) {
                    AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.process(new AbstractMap.SimpleImmutableEntry<>(player, "Vessel crew rank"));
                } else if (!player.hasPermission(Permissions.getMovePermission(stored.getType()))) {
                    AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.process(new AbstractMap.SimpleImmutableEntry<>(player, Permissions.getMovePermission(stored.getType())));
                } else if (!player.hasPermission(Permissions.getOtherMovePermission(stored.getType()))) {
                    AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.process(new AbstractMap.SimpleImmutableEntry<>(player, Permissions.getOtherMovePermission(stored.getType())));
                }
                ShipsSign.LOCKED_SIGNS.remove(position);
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                return;
            }
        }
        context.setMovement(ShipsPlugin.getPlugin().getConfig().getDefaultMovement());
        context.setClicked(position);

        Consumer<Throwable> exception = (exc) -> {
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            ShipsSign.LOCKED_SIGNS.remove(position);
            if (exc instanceof MoveException) {
                MoveException e = (MoveException) exc;
                sendErrorMessage(player, e.getMovement(), e.getMovement().getValue().orElse(null));
            }
            context.getEntities().keySet().forEach(s -> {
                if (s instanceof EntitySnapshot.NoneDestructibleSnapshot) {
                    ((EntitySnapshot.NoneDestructibleSnapshot<? extends LiveEntity>) s).getEntity().setGravity(true);
                }
            });
        };

        ready.onMovementReady(context, vessel, exception);
    }

    static <T> void sendErrorMessage(@NotNull CommandViewer viewer, @NotNull FailedMovement<T> movement, @Nullable Object value) {
        movement.sendMessage(viewer, (T) value);
    }

    static void onMovement(SyncBlockPosition sign, LivePlayer player, MovementReady movement) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        MovementContext context = new MovementContext();
        context.setPostMovement(e -> ShipsSign.LOCKED_SIGNS.remove(sign));
        if (config.isBossBarVisible()) {
            ServerBossBar bar = CorePlugin.createBossBar();
            //TODO - Set bar message
            bar.setTitle(AText.ofPlain("Starting block getter"));
            bar.register(player);
            context.setBar(bar);
        }
        ShipsSign.LOCKED_SIGNS.add(sign);
        if (config.isStructureAutoUpdating()) {
            new OnOvertimeAutoUpdate(sign, context, player, movement, config.getDefaultTrackSize()).loadOvertime();
            return;
        }
        try {
            Vessel vessel = new ShipsBlockFinder(sign).load();
            SignUtil.postMovementReady(context, vessel, player, sign, movement);
        } catch (UnableToFindLicenceSign e1) {
            player.sendMessage(AText.ofPlain(e1.getReason()).withColour(NamedTextColours.RED));
            e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
            CorePlugin.createSchedulerBuilder().setDelay(5).setDisplayName("bedrock reset").setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
        } catch (LoadVesselException e) {
            player.sendMessage(AText.ofPlain(e.getReason()).withColour(NamedTextColours.RED));
        } finally {
            ShipsSign.LOCKED_SIGNS.remove(sign);
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
        }
    }
}
