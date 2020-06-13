package org.ships.movement.autopilot.scheduler;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.world.boss.ServerBossBar;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.FailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.FileBasedVessel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;

public class FallExecutor implements Runnable {

    @Override
    public void run() {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        try {
            ShipsPlugin.getPlugin().getVessels().stream().filter(s -> s instanceof Fallable).forEach(v -> {
                if (!((Fallable) v).shouldFall()) {
                    MovementContext context = new MovementContext().setMovement(config.getDefaultMovement());
                    if (config.isBossBarVisible()) {
                        ServerBossBar bar = CorePlugin.createBossBar().setMessage(CorePlugin.buildText("Falling"));
                        v.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> bar.register((LivePlayer) e));
                        context.setBar(bar);
                    }
                    try {
                        v.moveTowards(0, -(ShipsPlugin.getPlugin().getConfig().getFallingSpeed()), 0, context);
                    } catch (MoveException e) {
                        context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                        if (e.getMovement().getResult().equals(MovementResult.COLLIDE_DETECTED)) {
                            if (v instanceof FileBasedVessel) {
                                File file = ((FileBasedVessel) v).getFile();
                                try {
                                    Files.delete(file.toPath());
                                } catch (IOException e2) {
                                    e.printStackTrace();
                                }
                            }
                            ShipsPlugin.getPlugin().unregisterVessel(v);
                            v.getPosition().destroy();
                        }
                    } catch (Throwable e2) {
                        context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                        v.getEntities().forEach(e -> e.setGravity(true));
                    }
                }
            });
        }catch (ConcurrentModificationException ignore){

        }
    }

    public static Scheduler createScheduler(){
        return CorePlugin.createSchedulerBuilder()
                .setExecutor(new FallExecutor())
                .setIterationUnit(ShipsPlugin.getPlugin().getConfig().getFallingDelayUnit())
                .setIteration(ShipsPlugin.getPlugin().getConfig().getFallingDelay())
                .setDisplayName("Ships fall scheduler")
                .build(ShipsPlugin.getPlugin());
    }
}
