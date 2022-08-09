package org.ships.movement.autopilot.scheduler;

import org.core.TranslateCore;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.viewer.CommandViewer;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncPosition;
import org.ships.exceptions.MoveException;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.movement.result.MovementResult;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.assits.FlightPathType;

import java.util.Optional;

/**
 * @deprecated Will be replaced with completely rewritten AutoPilot code
 */
@Deprecated(forRemoval = true)
public class FlightPathExecutor implements Runnable {

    protected FlightPathType vessel;
    protected CommandViewer viewer;

    public FlightPathExecutor(FlightPathType vessel) {
        this.vessel = vessel;
    }

    public FlightPathType getVessel() {
        return this.vessel;
    }

    public FlightPathExecutor setVessel(FlightPathType type) {
        this.vessel = type;
        return this;
    }

    public Optional<CommandViewer> getViewer() {
        return Optional.ofNullable(this.viewer);
    }

    public FlightPathExecutor setViewer(CommandViewer viewer) {
        this.viewer = viewer;
        return this;
    }

    @Override
    public void run() {
        this.vessel.getFlightPath().ifPresent(fp -> {
            Optional<Vector3<Integer>> opVector = fp.getNext();
            if (opVector.isEmpty()) {
                this.vessel.setFlightPath(null);
                return;
            }
            MovementDetailsBuilder builder = new MovementDetailsBuilder();
            if (ShipsPlugin.getPlugin().getConfig().isBossBarVisible()) {
                ServerBossBar bossBar = TranslateCore.createBossBar();
                final ServerBossBar finalBar = bossBar;
                this.vessel
                        .getEntities()
                        .stream()
                        .filter(e -> e instanceof LivePlayer)
                        .forEach(e -> finalBar.register((LivePlayer) e));
                builder.setBossBar(bossBar);
            }
            builder.setPostMovementEvents(e -> this.vessel.setFlightPath(this.vessel
                    .getFlightPath()
                    .get()
                    .createUpdatedPath(this.vessel.getPosition().getPosition(),
                            this.vessel.getFlightPath().get().getEndingPosition())));
            builder.setException((context, exc) -> {
                if (exc instanceof MoveException e) {
                    if (e.getMovement() instanceof MovementResult.VesselMovingAlready) {
                        return;
                    }
                    if (this.viewer == null && this.vessel instanceof CrewStoredVessel vessel) {
                        vessel.getCrew(CrewPermission.CAPTAIN).forEach(p -> {
                            LivePlayer player = TranslateCore
                                    .getServer()
                                    .getOnlinePlayers()
                                    .stream()
                                    .filter(play -> play.getUniqueId().equals(p))
                                    .findAny()
                                    .get();
                            e.getMovement().sendMessage(player);
                        });
                    } else {
                        e.getMovement().sendMessage(this.viewer);
                    }
                } else {
                    this.vessel.getEntities().forEach(e -> e.setGravity(true));
                }
            });
            SyncPosition<Integer> position = this.vessel.getPosition().getWorld().getPosition(opVector.get());
            this.vessel.moveTo(Position.toBlock(position), builder.build());
        });
    }
}
