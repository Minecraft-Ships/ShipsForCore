package org.ships.movement.autopilot.scheduler;

import org.core.CorePlugin;
import org.core.schedule.Scheduler;
import org.ships.exceptions.MoveException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.Fallable;

import java.util.concurrent.TimeUnit;

public class FallExecutor implements Runnable {

    @Override
    public void run() {
        ShipsPlugin.getPlugin().getVessels().stream().filter(s -> s instanceof Fallable).forEach(v -> {
            if (!((Fallable)v).shouldFall()) {
                try {
                    v.moveTowards(0, -1, 0, ShipsPlugin.getPlugin().getConfig().getDefaultMovement());
                } catch (MoveException e) {
                }catch (Throwable e2){
                    v.getEntities().forEach(e -> e.setGravity(true));
                }
            }
        });
    }

    public static Scheduler createScheduler(){
        return CorePlugin.createSchedulerBuilder()
                .setExecutor(new FallExecutor())
                .setIterationUnit(TimeUnit.MINUTES)
                .setIteration(1)
                .build(ShipsPlugin.getPlugin());
    }
}
