package org.ships.movement.autopilot.scheduler;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.world.boss.ServerBossBar;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.FileBasedVessel;
import org.ships.vessel.common.flag.SuccessfulMoveFlag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ConcurrentModificationException;
import java.util.Optional;

public class FallExecutor implements Runnable {

    @Override
    public void run() {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        try {
            ShipsPlugin.getPlugin().getVessels().stream().filter(s -> s instanceof Fallable).forEach(v -> {
                Optional<SuccessfulMoveFlag> opFlag = v.get(SuccessfulMoveFlag.class);
                if(!opFlag.isPresent()){
                    return;
                }
                if(!opFlag.get().getValue().orElse(false)){
                    return;
                }
                if (!((Fallable) v).shouldFall()) {
                    MovementContext context = new MovementContext().setMovement(config.getDefaultMovement());
                    if (config.isBossBarVisible()) {
                        ServerBossBar bar = CorePlugin.createBossBar().setMessage(CorePlugin.buildText("Falling"));
                        v.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> bar.register((LivePlayer) e));
                        context.setBar(bar);
                    }
                    v.moveTowards(0, -(ShipsPlugin.getPlugin().getConfig().getFallingSpeed()), 0, context, exc -> {
                        if (exc instanceof MoveException) {
                            MoveException e = (MoveException) exc;
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
                        } else {
                            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                            v.getEntities().forEach(e -> e.setGravity(true));
                        }
                    });
                }
            });
        } catch (ConcurrentModificationException ignore) {

        }
        if(config.isFallingEnabled()){
            createScheduler().run();
        }
    }

    public static Scheduler createScheduler() {
        return CorePlugin.createSchedulerBuilder()
                .setExecutor(new FallExecutor())
                .setDelayUnit(ShipsPlugin.getPlugin().getConfig().getFallingDelayUnit())
                .setDelay(ShipsPlugin.getPlugin().getConfig().getFallingDelay())
                .setDisplayName("Ships fall scheduler")
                .build(ShipsPlugin.getPlugin());
    }
}
