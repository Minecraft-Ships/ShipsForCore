package org.ships.vessel.sign;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.instruction.details.MovementDetails;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.loader.ShipsOvertimeUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.AbstractMap;
import java.util.function.BiConsumer;

public interface SignUtil {

    static void postMovementReady(MovementDetailsBuilder details,
                                  Vessel vessel,
                                  LivePlayer player,
                                  SyncBlockPosition position,
                                  MovementReady ready) {
        if (vessel instanceof CrewStoredVessel stored) {
            if (details.getBossBar() != null) {
                details.getBossBar().setTitle(AText.ofPlain("Checking permissions"));
                ShipsPlugin.getPlugin().getLogger().log("Checking permissions");
            }

            if (!((stored.getPermission(player.getUniqueId()).canMove() && player.hasPermission(
                    stored.getType().getMoveOwnPermission())) || player.hasPermission(
                    stored.getType().getMoveOtherPermission()))) {
                ShipsPlugin.getPlugin().getLogger().log("Found that you don't have any permissions to move");
                AText permission = AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.process(
                        new AbstractMap.SimpleImmutableEntry<>(player, "Unknown"));
                if (!stored.getPermission(player.getUniqueId()).canMove()) {
                    permission = AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.process(
                            new AbstractMap.SimpleImmutableEntry<>(player, "Vessel crew rank"));
                } else if (!player.hasPermission(stored.getType().getMoveOwnPermission())) {
                    permission = AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.process(
                            new AbstractMap.SimpleImmutableEntry<>(player, stored
                                    .getType()
                                    .getMoveOwnPermission()
                                    .getPermissionValue()));
                } else if (!player.hasPermission(stored.getType().getMoveOtherPermission())) {
                    permission = AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.process(
                            new AbstractMap.SimpleImmutableEntry<>(player, stored
                                    .getType()
                                    .getMoveOtherPermission()
                                    .getPermissionValue()));
                }
                player.sendMessage(permission);
                ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
                if (details.getBossBar() != null) {
                    details.getBossBar().deregisterPlayers();
                }
                return;
            }
            ShipsPlugin.getPlugin().getLogger().log("Checked permissions");
        }
        details.setClickedBlock(position);

        BiConsumer<MovementContext, Throwable> exception = (context, exc) -> {
            context.getBossBar().ifPresent(ServerBossBar::deregisterPlayers);
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
            if (exc instanceof MoveException e) {
                player.sendMessage(e.getErrorMessageText());
            }
            context.getEntities().keySet().forEach(s -> {
                if (s instanceof EntitySnapshot.NoneDestructibleSnapshot) {
                    ((EntitySnapshot.NoneDestructibleSnapshot<? extends LiveEntity>) s).getEntity().setGravity(true);
                }
            });
        };

        details.setException(exception);

        ShipsPlugin.getPlugin().getLogger().log("Movement context created");
        ready.onMovementReady(details.build(), vessel);
    }

    static void onMovement(SyncBlockPosition sign, LivePlayer player, MovementReady movement) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        MovementDetailsBuilder builder = new MovementDetailsBuilder();
        if (config.isBossBarVisible()) {
            ServerBossBar bossBar = TranslateCore.createBossBar();
            //TODO - Set bossBar message
            bossBar.setTitle(AText.ofPlain("Starting block getter"));
            bossBar.register(player);
            builder.setBossBar(bossBar);
        }
        ShipsPlugin.getPlugin().getLockedSignManager().lock(sign);
        new OnOvertimeAutoUpdate(sign, builder, player, movement, config.getDefaultTrackSize()).loadOvertime();

    }

    interface MovementReady {

        void onMovementReady(MovementDetails context, Vessel vessel);
    }

    class OnOvertimeAutoUpdate extends ShipsOvertimeUpdateBlockLoader {

        private final MovementDetailsBuilder context;
        private final int trackLimit;
        private final LivePlayer player;
        private final MovementReady movementReady;

        public OnOvertimeAutoUpdate(SyncBlockPosition position,
                                    MovementDetailsBuilder context,
                                    LivePlayer player,
                                    MovementReady ready,
                                    int trackLimit) {
            super(position, ShipsPlugin.getPlugin().getConfig().isStructureAutoUpdating());
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
        protected OvertimeBlockFinderUpdate.BlockFindControl onBlockFind(PositionableShipsStructure currentStructure,
                                                                         BlockPosition block) {
            if (this.context.getBossBar() != null) {
                ServerBossBar bossBar = this.context.getBossBar();
                int foundBlocks = currentStructure.getOriginalRelativePositionsToCenter().size() + 1;
                try {
                    bossBar.setValue(foundBlocks, this.trackLimit);
                } catch (IllegalArgumentException ignore) {
                }
            }
            return OvertimeBlockFinderUpdate.BlockFindControl.USE;
        }

        @Override
        protected void onExceptionThrown(LoadVesselException e) {
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(this.original);
            if (this.context.getBossBar() != null) {
                this.context.getBossBar().deregisterPlayers();
            }
            if (!(e instanceof UnableToFindLicenceSign e1)) {
                this.player.sendMessage(AText.ofPlain(e.getReason()).withColour(NamedTextColours.RED));
                return;
            }
            this.player.sendMessage(AText.ofPlain(e1.getReason()).withColour(NamedTextColours.RED));
            e1
                    .getFoundStructure()
                    .getSyncedPositionsRelativeToWorld()
                    .forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), this.player));
            TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDelay(5)
                    .setDisplayName("bedrock reset")
                    .setDelayUnit(TimeUnit.SECONDS)
                    .setRunner((sch) -> e1
                            .getFoundStructure()
                            .getSyncedPositionsRelativeToWorld()
                            .forEach(bp -> bp.resetBlock(this.player)))
                    .build(ShipsPlugin.getPlugin())
                    .run();
        }
    }
}
