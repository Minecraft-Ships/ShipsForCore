package org.ships.movement.autopilot.scheduler;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.vector.types.Vector3Int;
import org.core.world.boss.ServerBossBar;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.FlightPathType;

import java.util.Optional;

public class FlightPathExecutor implements Runnable {

    protected FlightPathType vessel;

    public FlightPathExecutor(FlightPathType vessel){
        this.vessel = vessel;
    }

    public FlightPathExecutor setVessel(FlightPathType type){
        this.vessel = type;
        return this;
    }

    public FlightPathType getVessel(){
        return this.vessel;
    }

    @Override
    public void run() {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        this.vessel.getFlightPath().ifPresent(fp -> {
            Optional<Vector3Int> opVector = fp.getNext();
            if(!opVector.isPresent()){
                this.vessel.setFlightPath(null);
                return;
            }
            MovementContext context = new MovementContext().setMovement(config.getDefaultMovement());
            if(ShipsPlugin.getPlugin().getConfig().isBossBarVisible()) {
                ServerBossBar bar = CorePlugin.createBossBar();
                final ServerBossBar finalBar = bar;
                vessel.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> finalBar.register((LivePlayer) e));
                context.setBar(bar);
            }
            try {
                this.vessel.moveTo(this.vessel.getPosition().getWorld().getPosition(opVector.get()), context);
                this.vessel.setFlightPath(this.vessel.getFlightPath().get().createUpdatedPath(this.vessel.getPosition().getPosition(), this.vessel.getFlightPath().get().getEndingPosition()));
            } catch (MoveException e) {
                if(e.getMovement() instanceof MovementResult.VesselMovingAlready){
                    return;
                }
                e.printStackTrace();
            }catch (Throwable e2){
                vessel.getEntities().forEach(e -> e.setGravity(true));
            }
        });
    }
}
