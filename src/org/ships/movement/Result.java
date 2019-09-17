package org.ships.movement;

import org.core.entity.living.human.player.Player;
import org.core.vector.Vector3;
import org.core.world.boss.ServerBossBar;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Result extends ArrayList<Result.Run> {

    public static final Result DEFAULT_RESULT = new Result(
            Run.COMMON_TELEPORT_ENTITIES,
            Run.COMMON_RESET_GRAVITY,
            Run.COMMON_SET_POSITION_OF_LICENCE_SIGN,
            Run.COMMON_SET_NEW_POSITIONS,
            Run.COMMON_SPAWN_ENTITIES,
            Run.COMMON_SAVE,
            Run.REMOVE_BAR);

    public interface Run {

        Run REMOVE_BAR = (v, c) -> c.getBar().ifPresent(ServerBossBar::deregisterPlayers);

        Run COMMON_TELEPORT_ENTITIES = (v, c) -> c.getEntities().forEach((entity, value) -> {
            double pitch = entity.getPitch();
            double yaw = entity.getYaw();
            double roll = entity.getRoll();
            Vector3<Double> position = entity.getPosition().getPosition().minus(value.getBeforePosition().toExactPosition().getPosition());
            Vector3<Double> position2 = value.getAfterPosition().toExactPosition().getPosition();
            position = position2.add(position);
            entity.setPosition(position);
            entity.setYaw(yaw);
            entity.setRoll(roll);
            entity.setPitch(pitch);
        });

        Run COMMON_RESET_GRAVITY = (v, c) -> c.getEntities().keySet().forEach(e -> e.setGravity(true));

        Run COMMON_SET_NEW_POSITIONS = (v, c) -> {
            PositionableShipsStructure pss = v.getStructure();
            pss.clear();
            c.getMovingStructure().forEach((mb) -> pss.addPosition(mb.getAfterPosition()));
        };

        Run COMMON_SET_POSITION_OF_LICENCE_SIGN = (v, c) -> c.getMovingStructure().get(ShipsPlugin.getPlugin().get(LicenceSign.class).get()).ifPresent(mb -> v.getStructure().setPosition(mb.getAfterPosition()));

        Run COMMON_SPAWN_ENTITIES = (v, c) -> {
            c.getEntities().keySet().stream().forEach(e -> {
                if(!(e instanceof Player)) {
                    e.getCreatedFrom().ifPresent(le -> le.remove());
                }
                try {
                    e.spawnEntity();
                }catch (IllegalStateException ex){

                }
            });
        };

        Run COMMON_SAVE = (v, c) -> v.save();

        void run(Vessel vessel, MovementContext context);

    }

    public Result(){
        super();
    }

    public Result(Run... run){
        super(Arrays.asList(run));
    }

    public Result(Collection<Run> collection){
        super(collection);
    }

    public void run(Vessel vessel, MovementContext context){
        this.forEach(e -> e.run(vessel, context));
    }
}
