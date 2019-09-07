package org.ships.movement.autopilot.scheduler;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.world.boss.ServerBossBar;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.Fallable;

import java.util.concurrent.TimeUnit;

public class FallExecutor implements Runnable {

    @Override
    public void run() {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        ShipsPlugin.getPlugin().getVessels().stream().filter(s -> s instanceof Fallable).forEach(v -> {
            if (!((Fallable)v).shouldFall()) {
                MovementContext context = new MovementContext().setMovement(config.getDefaultMovement());
                if(config.isBossBarVisible()) {
                    ServerBossBar bar = CorePlugin.createBossBar();
                    v.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> bar.register((LivePlayer) e));
                    context.setBar(bar);
                }
                try {
                    v.moveTowards(0, -1, 0, context);
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
