package org.ships.movement.autopilot.scheduler;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.viewer.CommandViewer;
import org.core.vector.types.Vector3Int;
import org.core.world.boss.ServerBossBar;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.MovementResult;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.assits.FlightPathType;

import java.util.Optional;

public class FlightPathExecutor implements Runnable {

    protected FlightPathType vessel;
    protected CommandViewer viewer;

    public FlightPathExecutor(FlightPathType vessel) {
        this.vessel = vessel;
    }

    public FlightPathExecutor setVessel(FlightPathType type) {
        this.vessel = type;
        return this;
    }

    public FlightPathType getVessel() {
        return this.vessel;
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
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        this.vessel.getFlightPath().ifPresent(fp -> {
            Optional<Vector3Int> opVector = fp.getNext();
            if (!opVector.isPresent()) {
                this.vessel.setFlightPath(null);
                return;
            }
            MovementContext context = new MovementContext().setMovement(config.getDefaultMovement());
            if (ShipsPlugin.getPlugin().getConfig().isBossBarVisible()) {
                ServerBossBar bar = CorePlugin.createBossBar();
                final ServerBossBar finalBar = bar;
                vessel.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> finalBar.register((LivePlayer) e));
                context.setBar(bar);
            }
            context.setPostMovement(e -> this.vessel.setFlightPath(this.vessel.getFlightPath().get().createUpdatedPath(this.vessel.getPosition().getPosition(), this.vessel.getFlightPath().get().getEndingPosition())));
            this.vessel.moveTo(this.vessel.getPosition().getWorld().getPosition(opVector.get()), context, exc -> {
                if (exc instanceof MoveException) {
                    MoveException e = (MoveException) exc;
                    if (e.getMovement() instanceof MovementResult.VesselMovingAlready) {
                        return;
                    }
                    if (this.viewer == null && this.vessel instanceof CrewStoredVessel) {
                        CrewStoredVessel vessel = (CrewStoredVessel) this.vessel;
                        vessel.getCrew(CrewPermission.CAPTAIN).forEach(p -> {
                            LivePlayer player = CorePlugin.getServer().getOnlinePlayers().stream().filter(play -> play.getUniqueId().equals(p)).findAny().get();
                            e.getMovement().sendMessage(player);
                        });
                    } else {
                        e.getMovement().sendMessage(this.viewer);
                    }
                } else {
                    vessel.getEntities().forEach(e -> e.setGravity(true));
                }
            });
        });
    }
}
