package org.ships.movement.autopilot.scheduler;

import org.core.TranslateCore;
import org.core.entity.living.human.player.LivePlayer;
import org.core.utils.time.TimeRange;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.MoveException;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.movement.result.FailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.flag.CooldownFlag;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.EOTSign;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Optional;

/**
 * @deprecated Will be replaced with completely rewritten EOT code
 */
@Deprecated(forRemoval = true)
public class EOTExecutor implements Runnable {

    protected Vessel vessel;
    protected LivePlayer player;
    protected boolean disableOnNoPilot;

    public EOTExecutor(LivePlayer player, Vessel vessel) {
        this.vessel = vessel;
        this.player = player;
    }

    public EOTExecutor setDisableOnNoPilot(boolean check) {
        this.disableOnNoPilot = check;
        return this;
    }

    public Vessel getVessel() {
        return this.vessel;
    }

    public EOTExecutor setVessel(Vessel type) {
        this.vessel = type;
        return this;
    }

    public LivePlayer getPlayer() {
        return this.player;
    }

    public EOTExecutor setPlayer(LivePlayer player) {
        this.player = player;
        return this;
    }

    public boolean willDisableIfNoPilot() {
        return this.disableOnNoPilot;
    }

    public Optional<SyncBlockPosition> getSign() {
        Collection<SyncBlockPosition> blocks = this.getVessel().getStructure().getAll(SignTileEntity.class);
        EOTSign sign = ShipsPlugin
                .getPlugin()
                .get(EOTSign.class)
                .orElseThrow(() -> new RuntimeException("Could not find eot sign rules"));
        return blocks
                .stream()
                .filter(b -> sign.isSign((SignTileEntity) b
                        .getTileEntity()
                        .orElseThrow(() -> new RuntimeException("Matched sign but couldnt convert to sign"))))
                .findFirst();
    }

    @Override
    public void run() {
        @NotNull Optional<CooldownFlag> opCooldownFlag = this.vessel.get(CooldownFlag.class);
        if (opCooldownFlag.isPresent() && opCooldownFlag.get().getValue().isPresent()) {
            TimeRange range = opCooldownFlag.get().getValue().get();
            if (range.getEndTime().isAfter(LocalTime.now())) {
                return;
            }
        }
        Optional<SyncBlockPosition> opSign = this.getSign();
        if (opSign.isEmpty()) {
            return;
        }
        SyncBlockPosition b = opSign.get();
        Optional<DirectionalData> directionalData = b.getBlockDetails().getDirectionalData();
        if (directionalData.isEmpty()) {
            return;
        }
        if (this.disableOnNoPilot && this.vessel instanceof CrewStoredVessel) {
            boolean check = this.vessel
                    .getEntities(LivePlayer.class)
                    .stream()
                    .anyMatch(e -> ((CrewStoredVessel) this.vessel).getPermission(e.getUniqueId()).canMove());
            if (!check) {
                return;
            }
        }
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
                .get()
                .getDirection()
                .getOpposite()
                .getAsVector()
                .multiply(ShipsPlugin.getPlugin().getConfig().getEOTSpeed()), builder.build());

    }

    private <T> void sendError(FailedMovement<T> failedMovement) {
        failedMovement.sendMessage(this.player, failedMovement.getValue().orElse(null));
    }
}
