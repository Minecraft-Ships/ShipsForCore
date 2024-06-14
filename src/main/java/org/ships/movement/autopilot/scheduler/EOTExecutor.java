package org.ships.movement.autopilot.scheduler;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.core.entity.LiveEntity;
import org.core.schedule.Scheduler;
import org.core.source.Messageable;
import org.core.vector.type.Vector3;
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
import org.ships.vessel.sign.ShipsSigns;

import java.util.Optional;
import java.util.function.Consumer;

public class EOTExecutor implements Consumer<Scheduler> {

    private final @NotNull Vessel vessel;
    private final @Nullable Messageable player;

    public EOTExecutor(@NotNull Vessel vessel, @Nullable Messageable player) {
        this.vessel = vessel;
        this.player = player;
    }

    public @NotNull Vessel getVessel() {
        return this.vessel;
    }

    public @NotNull Optional<Messageable> getMessenger() {
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
        if (!(tileEntity instanceof LiveSignTileEntity)) {
            return Optional.empty();
        }
        LiveSignTileEntity liveSignTileEntity = (LiveSignTileEntity) tileEntity;
        return Optional.of(liveSignTileEntity);
    }

    @Override
    public void accept(Scheduler scheduler) {
        Optional<LiveSignTileEntity> opLiveSignTileEntity = this.getSign();
        if (opLiveSignTileEntity.isEmpty()) {
            scheduler.cancel();
            return;
        }
        LiveSignTileEntity liveSignTileEntity = opLiveSignTileEntity.get();

        EOTSign signTools = ShipsSigns.EOT;
        if (!signTools.isSign(liveSignTileEntity)) {
            scheduler.cancel();
            return;
        }
        if (!signTools.isAhead(liveSignTileEntity)) {
            scheduler.cancel();
            return;
        }
        Optional<DirectionalData> opDirectionalData = liveSignTileEntity
                .getPosition()
                .getBlockDetails()
                .getDirectionalData();
        if (opDirectionalData.isEmpty()) {
            scheduler.cancel();
            return;
        }
        DirectionalData directionalData = opDirectionalData.get();

        MovementDetailsBuilder builder = new MovementDetailsBuilder();
        if (ShipsPlugin.getPlugin().getConfig().isBossBarVisible()) {
            BossBar bossBar = BossBar.bossBar(Component.empty(), 0, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
            this.vessel.getEntitiesOvertime(entity -> entity instanceof Audience).thenAccept(entities -> {
                for (LiveEntity entity : entities) {
                    ((Audience) entity).showBossBar(bossBar);
                }
            });
            builder.setAdventureBossBar(bossBar);
        }

        Messageable[] viewers = this.player == null ? new Messageable[0] : new Messageable[]{this.player};

        builder.setException(new SimpleMovementException(viewers));

        this.vessel.moveTowards(directionalData
                                        .getDirection()
                                        .getOpposite()
                                        .getAsVector()
                                        .multiply(ShipsPlugin.getPlugin().getConfig().getEOTSpeed()), builder.build());
    }
}
