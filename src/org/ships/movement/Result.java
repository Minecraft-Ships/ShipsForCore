package org.ships.movement;

import org.core.entity.LiveEntity;
import org.core.vector.Vector3;
import org.core.world.boss.ServerBossBar;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class Result extends ArrayList<Result.Run> {

    public static final Result DEFAULT_RESULT = new Result(
            Run.COMMON_TELEPORT_ENTITIES,
            Run.COMMON_RESET_GRAVITY,
            Run.COMMON_SET_POSITION_OF_LICENCE_SIGN,
            Run.COMMON_SET_NEW_POSITIONS,
            Run.COMMON_SAVE,
            Run.REMOVE_BAR);

    public interface Run {

        Run REMOVE_BAR = (v, b, bar, m) -> {
            if(bar == null){
                return;
            }
            bar.deregisterPlayers();
        };

        Run COMMON_TELEPORT_ENTITIES = (v, b, bar, m) -> m.forEach((entity, value) -> {
            double pitch = entity.getPitch();
            double yaw = entity.getYaw();
            double roll = entity.getRoll();
            Vector3<Double> position = entity.getPosition().getPosition().minus(value.getBeforePosition().toExactPosition().getPosition());
            Vector3<Double> position2 = value.getAfterPosition().toExactPosition().getPosition();
            position = position2.add(position);
            entity.setPosition(position);
            entity.setYaw(yaw).setRoll(roll).setPitch(pitch);
        });

        Run COMMON_RESET_GRAVITY = (v, b, bar, m) -> m.keySet().forEach(e -> e.setGravity(true));

        Run COMMON_SET_NEW_POSITIONS = (v, b, bar, m) -> {
            PositionableShipsStructure pss = v.getStructure();
            pss.clear();
            b.forEach((mb) -> pss.addPosition(mb.getAfterPosition()));
        };

        Run COMMON_SET_POSITION_OF_LICENCE_SIGN = (v, b, bar, m) -> b.get(ShipsPlugin.getPlugin().get(LicenceSign.class).get()).ifPresent(mb -> v.getStructure().setPosition(mb.getAfterPosition()));

        Run COMMON_SAVE = (v, b, bar, m) -> v.save();

        void run(Vessel vessel, MovingBlockSet blocks, ServerBossBar bar, Map<LiveEntity, MovingBlock> map);

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

    public void run(Vessel vessel, MovingBlockSet blocks, ServerBossBar bar, Map<LiveEntity, MovingBlock> map){
        this.forEach(e -> e.run(vessel, blocks, bar, map));
    }
}
