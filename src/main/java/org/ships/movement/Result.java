package org.ships.movement;

import org.core.TranslateCore;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.config.configuration.ShipsConfig;
import org.ships.event.vessel.move.ResultEvent;
import org.ships.movement.autopilot.scheduler.AutopilotExecutor;
import org.ships.movement.autopilot.scheduler.EOTExecutor;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.EotFlag;
import org.ships.vessel.common.flag.FlightPathFlag;
import org.ships.vessel.common.flag.SuccessfulMoveFlag;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class Result extends ArrayList<Result.Run> {

    public static final Result DEFAULT_RESULT = new Result(Run.COMMON_TELEPORT_ENTITIES, Run.COMMON_RESET_GRAVITY,
                                                           Run.COMMON_SET_POSITION_OF_LICENCE_SIGN,
                                                           Run.COMMON_SET_NEW_POSITIONS, Run.COMMON_SPAWN_ENTITIES,
                                                           Run.COMMON_SET_SUCCESSFUL, Run.COMMON_SAVE, Run.REMOVE_BAR,
                                                           Run.COMMON_RERUN_EOT, Run.COMMON_RERUN_AUTO_PILOT);

    public Result() {
    }

    public Result(Run... run) {
        super(Arrays.asList(run));
    }

    public Result(Collection<? extends Run> collection) {
        super(collection);
    }

    public void run(Vessel vessel, MovementContext context) {
        this.forEach(e -> {
            ResultEvent.PreRun event = new ResultEvent.PreRun(vessel, this, e, context);
            TranslateCore.getEventManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            e.run(vessel, context);
        });
    }

    public interface Run {

        Run COMMON_RERUN_EOT = (v, c) -> {
            Optional<EotFlag> opEOTFlag = v.get(EotFlag.class);
            if (opEOTFlag.isEmpty()) {
                return;
            }
            ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
            int delay = config.getEOTDelay();
            TimeUnit unit = config.getEOTDelayUnit();
            LivePlayer player = opEOTFlag
                    .get()
                    .getWhoClicked()
                    .flatMap(uuid -> TranslateCore
                            .getServer()
                            .getOnlinePlayers()
                            .parallelStream()
                            .filter(p -> p.getUniqueId().equals(uuid))
                            .findAny())
                    .orElse(null);
            TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDelay(delay)
                    .setDelayUnit(unit)
                    .setDisplayName("Repeating display name")
                    .setRunner(new EOTExecutor(v, player))
                    .build(ShipsPlugin.getPlugin())
                    .run();
        };

        Run COMMON_RERUN_AUTO_PILOT = (v, c) -> {
            Optional<FlightPathFlag> opFlightPath = v.get(FlightPathFlag.class);
            if (opFlightPath.isEmpty()) {
                return;
            }
            TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDelay(3)
                    .setDelayUnit(TimeUnit.SECONDS)
                    .setDisplayName("Repeating autopilot")
                    .setRunner(new AutopilotExecutor(v))
                    .build(ShipsPlugin.getPlugin())
                    .run();
        };

        Run COMMON_SET_SUCCESSFUL = (v, c) -> {
            SuccessfulMoveFlag flag = v.get(SuccessfulMoveFlag.class).orElse(new SuccessfulMoveFlag());
            flag.setValue(true);
            v.set(flag);
        };

        Run REMOVE_BAR = (v, c) -> c.getBossBar().ifPresent(ServerBossBar::deregisterPlayers);

        Run COMMON_TELEPORT_ENTITIES = (v, c) -> c.getEntities().forEach((entity, value) -> {
            double pitch = entity.getPitch();
            double yaw = entity.getYaw();
            double roll = entity.getRoll();
            SyncBlockPosition before = value.getBeforePosition();
            SyncBlockPosition after = value.getAfterPosition();
            Vector3<Double> position = entity.getPosition().getPosition().minus(before.toExactPosition().getPosition());
            Vector3<Double> position2 = after.toExactPosition().getPosition();
            position = position2.plus(position);
            entity.setPosition(position);
            entity.setYaw(yaw);
            entity.setRoll(roll);
            entity.setPitch(pitch);
        });

        Run COMMON_RESET_GRAVITY = (v, c) -> c.getEntities().keySet().forEach(e -> e.setGravity(true));

        Run COMMON_SET_NEW_POSITIONS = (v, c) -> {
            PositionableShipsStructure pss = v.getStructure();
            pss.clear();
            c.getMovingStructure().getOriginal().forEach((mb) -> pss.addPositionRelativeToWorld(mb.getAfterPosition()));
        };

        Run COMMON_SET_POSITION_OF_LICENCE_SIGN = (v, c) -> {
            Optional<MovingBlock> opSign = c
                    .getMovingStructure()
                    .getOriginal()
                    .get(ShipsPlugin
                                 .getPlugin()
                                 .get(LicenceSign.class)
                                 .orElseThrow(
                                         () -> new RuntimeException("Cannot find licence sign, is it registered")));
            if (opSign.isEmpty()) {
                return;
            }
            v.getStructure().setPosition(opSign.get().getAfterPosition());
        };

        Run COMMON_SPAWN_ENTITIES = (v, c) -> c.getEntities().keySet().forEach(e -> {
            if (e instanceof EntitySnapshot.NoneDestructibleSnapshot<? extends LiveEntity> snapshot) {
                snapshot.teleportEntity(true);
                return;
            }
            e.getCreatedFrom().ifPresent(LiveEntity::remove);
            try {
                e.spawnEntity();
            } catch (IllegalStateException ignored) {
            }

        });

        Run COMMON_SAVE = (v, c) -> v.save();

        void run(Vessel vessel, MovementContext context);

    }
}
