package org.ships.movement.autopilot.scheduler;

import org.core.TranslateCore;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.exceptions.MoveException;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.movement.result.FailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.EotFlag;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.EOTSign;

import java.util.Optional;
import java.util.function.Consumer;

public class EOTExecutor implements Consumer<Scheduler> {

    private final Vessel vessel;
    private final LivePlayer player;

    public EOTExecutor(Vessel vessel, LivePlayer player) {
        this.vessel = vessel;
        this.player = player;
    }

    public Vessel getVessel() {
        return this.vessel;
    }

    public LivePlayer getPlayer() {
        return this.player;
    }

    public Optional<LiveSignTileEntity> getSign() {
        Optional<Vector3<Integer>> opFlagValue = this.vessel.get(EotFlag.class).flatMap(EotFlag::getValue);
        if (opFlagValue.isEmpty()) {
            return Optional.empty();
        }
        Vector3<Integer> eotRelative = opFlagValue.get();
        SyncBlockPosition mainPosition = this.vessel.getStructure().getPosition();
        SyncBlockPosition eotSignLocation = mainPosition.getRelative(eotRelative);
        Optional<LiveTileEntity> opTileEntity = eotSignLocation.getTileEntity();
        if (opTileEntity.isEmpty()) {
            return Optional.empty();
        }
        LiveTileEntity tileEntity = opTileEntity.get();
        if (!(tileEntity instanceof LiveSignTileEntity liveSignTileEntity)) {
            return Optional.empty();
        }
        return Optional.of(liveSignTileEntity);
    }

    @Override
    public void accept(Scheduler scheduler) {
        Optional<LiveSignTileEntity> opLiveSignTileEntity = this.getSign();
        if (opLiveSignTileEntity.isEmpty()) {
            if (scheduler instanceof Scheduler.Native nativeScheduler) {
                nativeScheduler.cancel();
            }
            return;
        }
        LiveSignTileEntity liveSignTileEntity = opLiveSignTileEntity.get();

        EOTSign signTools = ShipsPlugin
                .getPlugin()
                .get(EOTSign.class)
                .orElseThrow(() -> new RuntimeException("EOT sign could not be found"));
        if (!signTools.isSign(liveSignTileEntity.getText())) {
            if (scheduler instanceof Scheduler.Native nativeScheduler) {
                nativeScheduler.cancel();
            }
            return;
        }
        if (!signTools.isAhead(liveSignTileEntity)) {
            if (scheduler instanceof Scheduler.Native nativeScheduler) {
                nativeScheduler.cancel();
            }
            return;
        }
        Optional<DirectionalData> opDirectionalData = liveSignTileEntity
                .getPosition()
                .getBlockDetails()
                .getDirectionalData();
        if (opDirectionalData.isEmpty()) {
            if (scheduler instanceof Scheduler.Native nativeScheduler) {
                nativeScheduler.cancel();
            }
            return;
        }
        DirectionalData directionalData = opDirectionalData.get();

        MovementDetailsBuilder builder = new MovementDetailsBuilder();
        if (ShipsPlugin.getPlugin().getConfig().isBossBarVisible()) {
            ServerBossBar bossBar = TranslateCore.createBossBar();
            this.vessel.getEntities(LivePlayer.class).forEach(bossBar::register);
            builder.setBossBar(bossBar);
        }

        builder.setException((context, exc) -> {
            context.getBossBar().ifPresent(ServerBossBar::deregisterPlayers);
            this.vessel.getEntities().forEach(e -> e.setGravity(true));
            if (exc instanceof MoveException e) {
                if (e.getMovement() instanceof MovementResult.VesselMovingAlready) {
                    return;
                }
                this.sendError(e.getMovement());
            }
        });


        this.vessel.moveTowards(directionalData
                .getDirection()
                .getOpposite()
                .getAsVector()
                .multiply(ShipsPlugin.getPlugin().getConfig().getEOTSpeed()), builder.build());
    }

    private <T> void sendError(FailedMovement<T> failedMovement) {
        failedMovement.sendMessage(this.player, failedMovement.getValue().orElse(null));
    }
}
