package org.ships.movement.autopilot.scheduler;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.utils.time.TimeRange;
import org.core.world.boss.ServerBossBar;
import org.jetbrains.annotations.NotNull;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.Fallable;
import org.ships.vessel.common.assits.FileBasedVessel;
import org.ships.vessel.common.flag.CooldownFlag;
import org.ships.vessel.common.flag.SuccessfulMoveFlag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.ConcurrentModificationException;
import java.util.Optional;

public class FallExecutor implements Runnable {

    public static Scheduler createScheduler() {
        return TranslateCore
                .getScheduleManager()
                .schedule()
                .setExecutor(new FallExecutor())
                .setDelayUnit(ShipsPlugin.getPlugin().getConfig().getFallingDelayUnit())
                .setDelay(ShipsPlugin.getPlugin().getConfig().getFallingDelay())
                .setDisplayName("Ships fall scheduler")
                .build(ShipsPlugin.getPlugin());
    }

    @Override
    public void run() {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        try {
            ShipsPlugin.getPlugin().getVessels().stream().filter(s -> s instanceof Fallable).map(s -> (Fallable) s).forEach(v -> {
                Optional<SuccessfulMoveFlag> opFlag = v.get(SuccessfulMoveFlag.class);
                if (opFlag.isPresent()) {
                    if (opFlag.get().getValue().orElse(false)) {
                        return;
                    }
                }
                @NotNull Optional<CooldownFlag> opCooldownFlag = v.get(CooldownFlag.class);
                if (opCooldownFlag.isPresent() && opCooldownFlag.get().getValue().isPresent()) {
                    TimeRange range = opCooldownFlag.get().getValue().get();
                    if (range.getEndTime().isAfter(LocalTime.now())) {
                        return;
                    }
                }
                if (!v.shouldFall()) {
                    return;
                }
                MovementContext context = new MovementContext().setMovement(config.getDefaultMovement());
                if (config.isBossBarVisible()) {
                    ServerBossBar bar =
                            TranslateCore.createBossBar().setTitle(AText.ofPlain("Failling"));
                    v.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> bar.register((LivePlayer) e));
                    context.setBar(bar);
                }
                context.setPostMovementProcess(vessel -> vessel.set(new CooldownFlag(new TimeRange((int) config.getFallingDelayUnit().toTicks(config.getFallingDelay())))));
                v.moveTowards(0, -(ShipsPlugin.getPlugin().getConfig().getFallingSpeed()), 0, context, exc -> {
                    context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                    v.getEntities().forEach(e -> e.setGravity(true));
                    if (!(exc instanceof MoveException e)) {
                        return;
                    }
                    context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                    if (!e.getMovement().getResult().equals(MovementResult.COLLIDE_DETECTED)) {
                        return;
                    }
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


                });

            });
        } catch (ConcurrentModificationException ignore) {

        }
        if (config.isFallingEnabled()) {
            createScheduler().run();
        }
    }
}
