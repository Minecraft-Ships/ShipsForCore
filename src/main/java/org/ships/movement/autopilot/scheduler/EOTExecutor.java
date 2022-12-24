package org.ships.movement.autopilot.scheduler;

import org.core.TranslateCore;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.source.viewer.CommandViewer;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.movement.instruction.details.SimpleMovementException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.EotFlag;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.EOTSign;

import java.util.Optional;
import java.util.function.Consumer;

public class EOTExecutor implements Consumer<Scheduler> {

    private final @NotNull Vessel vessel;
    private final @Nullable CommandViewer player;

    public EOTExecutor(@NotNull Vessel vessel, @Nullable CommandViewer player) {
        this.vessel = vessel;
        this.player = player;
    }

    public @NotNull Vessel getVessel() {
        return this.vessel;
    }

    public @NotNull Optional<CommandViewer> getPlayer() {
        return Optional.ofNullable(this.player);
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

        CommandViewer[] viewers = this.player == null ? new CommandViewer[0] : new CommandViewer[]{this.player};

        builder.setException(new SimpleMovementException(viewers));

        this.vessel.moveTowards(directionalData
                                        .getDirection()
                                        .getOpposite()
                                        .getAsVector()
                                        .multiply(ShipsPlugin.getPlugin().getConfig().getEOTSpeed()), builder.build());
    }
}
