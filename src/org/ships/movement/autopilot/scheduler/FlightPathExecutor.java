package org.ships.movement.autopilot.scheduler;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.vector.types.Vector3Int;
import org.core.world.boss.ServerBossBar;
import org.ships.exceptions.MoveException;
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
        this.vessel.getFlightPath().ifPresent(fp -> {
            Optional<Vector3Int> opVector = fp.getNext();
            if(!opVector.isPresent()){
                this.vessel.setFlightPath(null);
                return;
            }
            System.out.println(opVector.get().toString());
            ServerBossBar bar = CorePlugin.createBossBar();
            vessel.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> bar.register((LivePlayer) e));
            try {
                this.vessel.moveTo(this.vessel.getPosition().getWorld().getPosition(opVector.get()), ShipsPlugin.getPlugin().getConfig().getDefaultMovement(), bar);
                this.vessel.setFlightPath(this.vessel.getFlightPath().get().createUpdatedPath(this.vessel.getPosition().getPosition(), this.vessel.getFlightPath().get().getEndingPosition()));
            } catch (MoveException e) {
                e.printStackTrace();
            }catch (Throwable e2){
                vessel.getEntities().forEach(e -> e.setGravity(true));
            }
        });
    }
}
