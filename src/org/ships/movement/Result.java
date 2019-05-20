package org.ships.movement;

import org.core.entity.Entity;
import org.core.vector.Vector3;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipsVessel;
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
            Run.COMMON_SAVE);

    public interface Run {

        Run COMMON_TELEPORT_ENTITIES = (v, b, m) -> m.forEach((entity, value) -> {
            double pitch = entity.getPitch();
            double yaw = entity.getYaw();
            double roll = entity.getRoll();
            Vector3<Double> position = entity.getPosition().getPosition().minus(value.getBeforePosition().toExactPosition().getPosition());
            Vector3<Double> position2 = value.getAfterPosition().toExactPosition().getPosition();
            position = position2.add(position);
            entity.setPosition(position);
            entity.setYaw(yaw).setRoll(roll).setPitch(pitch);
        });

        Run COMMON_RESET_GRAVITY = (v, b, m) -> m.keySet().forEach(e -> e.setGravity(true));

        Run COMMON_SET_NEW_POSITIONS = (v, b, m) -> {
            PositionableShipsStructure pss = v.getStructure();
            pss.clear();
            b.forEach((mb) -> pss.addPosition(mb.getAfterPosition()));
        };

        Run COMMON_SET_POSITION_OF_LICENCE_SIGN = (v, b, m) -> b.get(ShipsPlugin.getPlugin().get(LicenceSign.class).get()).ifPresent(mb -> v.getStructure().setPosition(mb.getAfterPosition()));

        Run COMMON_SAVE = (v, b, m) -> v.save();

        void run(ShipsVessel vessel, MovingBlockSet blocks, Map<Entity, MovingBlock> map);

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

    public void run(ShipsVessel vessel, MovingBlockSet blocks, Map<Entity, MovingBlock> map){
        this.forEach(e -> e.run(vessel, blocks, map));
    }
}
