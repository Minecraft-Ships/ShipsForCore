package org.ships.vessel.sign;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.TranslateCore;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.utils.BarUtils;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.Nullable;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.instruction.details.MovementDetails;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.loader.ShipsOvertimeUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface SignUtil {

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
            super(position, !ShipsPlugin.getPlugin().getConfig().isStructureClickUpdating());
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
            var bossBar = this.context.getAdventureBossBar();
            if (bossBar == null) {
                return OvertimeBlockFinderUpdate.BlockFindControl.USE;
            }
            int foundBlocks = currentStructure.getOriginalRelativePositionsToCenter().size() + 1;
            int trackLimit = Math.max(foundBlocks, this.trackLimit);
            float progress = foundBlocks / (float) trackLimit;
            progress = progress / 100;
            bossBar.progress(progress);
            bossBar.name(Component.text(foundBlocks + " / " + trackLimit));
            return OvertimeBlockFinderUpdate.BlockFindControl.USE;
        }

        @Override
        protected void onExceptionThrown(LoadVesselException e) {
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(this.original);
            BossBar bar = this.context.getAdventureBossBar();
            if (bar != null) {
                BarUtils.getPlayers(bar).forEach(player -> player.hideBossBar(bar));
            }
            if (!(e instanceof UnableToFindLicenceSign)) {
                this.player.sendMessage(Component.text(e.getReason()).color(NamedTextColor.RED));
                return;
            }
            UnableToFindLicenceSign e1 = (UnableToFindLicenceSign) e;
            this.player.sendMessage(Component.text(e1.getReason()).color(NamedTextColor.RED));
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
                    .buildDelayed(ShipsPlugin.getPlugin())
                    .run();
        }
    }

    interface MovementReady {

        void onMovementReady(MovementDetails context, Vessel vessel);
    }

    private static boolean checkPermissions(CrewStoredVessel stored, LivePlayer player) {
        if (player.hasPermission(stored.getType().getMoveOtherPermission())) {
            return true;
        }
        CrewPermission permission = stored.getPermission(player.getUniqueId());
        return permission.canMove();
    }

    private static boolean checkCrewVessel(Vessel vessel,
                                           Position<?> position,
                                           LivePlayer player,
                                           @Nullable BossBar bar) {
        if (!(vessel instanceof CrewStoredVessel)) {
            return false;
        }
        CrewStoredVessel stored = (CrewStoredVessel) vessel;

        if (bar != null) {
            bar.name(Component.text("Checking permissions"));
        }
        if (checkPermissions(stored, player)) {
            return false;
        }

        Component permissionMessage = AdventureMessageConfig.ERROR_PERMISSION_MISS_MATCH.processMessage(
                Map.entry(player, stored.getType().getMoveOtherPermission().getPermissionValue()));
        player.sendMessage(permissionMessage);
        ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
        if (bar != null) {
            BarUtils.getPlayers(bar).forEach(user -> user.hideBossBar(bar));
        }
        return true;
    }

    static void postMovementReady(MovementDetailsBuilder details,
                                  Vessel vessel,
                                  LivePlayer player,
                                  SyncBlockPosition position,
                                  MovementReady ready) {
        BossBar bar = details.getAdventureBossBar();
        if (checkCrewVessel(vessel, position, player, bar)) {
            return;
        }

        details.setClickedBlock(position);

        BiConsumer<MovementContext, Throwable> exception = (context, exc) -> {
            if (bar != null) {
                BarUtils.getPlayers(bar).forEach(user -> user.hideBossBar(bar));
            }
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
            if (exc instanceof MoveException) {
                MoveException e = (MoveException)exc;
                player.sendMessage(e.getErrorMessage());
            }
            context
                    .getEntities()
                    .keySet()
                    .stream()
                    .filter(snapshot -> snapshot instanceof EntitySnapshot.NoneDestructibleSnapshot)
                    .map(snapshot -> (EntitySnapshot.NoneDestructibleSnapshot<? extends LiveEntity>) snapshot)
                    .forEach(s -> s.getEntity().setGravity(true));
        };

        details.setException(exception);
        ready.onMovementReady(details.build(), vessel);
    }

    static void onMovement(SyncBlockPosition sign, LivePlayer player, MovementReady movement) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        MovementDetailsBuilder builder = new MovementDetailsBuilder();
        if (config.isBossBarVisible()) {
            BossBar bossBar = BossBar.bossBar(
                    Component.text("starting block getter: " + config.getDefaultFinder().getName()), 0,
                    BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
            player.showBossBar(bossBar);
            builder.setAdventureBossBar(bossBar);
        }
        ShipsPlugin.getPlugin().getLockedSignManager().lock(sign);
        if (config.isStructureClickUpdating()) {
            Optional<Vessel> opVessel = ShipsPlugin
                    .getPlugin()
                    .getVessels()
                    .parallelStream()
                    .filter(vessel -> vessel.getStructure().getBounds().contains(sign.getPosition()))
                    .filter(vessel -> vessel
                            .getStructure()
                            .getAsyncedPositionsRelativeToWorld()
                            .parallelStream()
                            .anyMatch(pos -> pos.getPosition().equals(sign.getPosition())))
                    .findAny();
            if (opVessel.isPresent()) {
                new OnOvertimeAutoUpdate(sign, builder, player, movement,
                                         config.getDefaultTrackSize()).onStructureUpdate(opVessel.get());
                return;
            }
        }
        new OnOvertimeAutoUpdate(sign, builder, player, movement, config.getDefaultTrackSize()).loadOvertime();

    }
}
