package org.ships.vessel.structure;

import org.core.exceptions.DirectionNotSupported;
import org.core.vector.types.Vector3Int;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.sync.SyncBlockPosition;

import java.util.*;
import java.util.stream.Collectors;

public class AbstractPosititionableShipsStructure implements PositionableShipsStructure {

    protected Set<Vector3Int> vectors = new HashSet<>();
    protected SyncBlockPosition position;

    public AbstractPosititionableShipsStructure(SyncBlockPosition position){
        this.position = position;
    }

    @Override
    public SyncBlockPosition getPosition() {
        return this.position;
    }

    @Override
    public PositionableShipsStructure setPosition(SyncBlockPosition pos) {
        this.position = pos;
        return this;
    }

    @Override
    public PositionableShipsStructure addAir() {
        Collection<SyncBlockPosition> positions = getPositions();
        List<SyncBlockPosition> toAdd = new ArrayList<>();
        Direction[] directions = FourFacingDirection.getFourFacingDirections();
        positions.forEach(p -> {
            for(Direction dir : directions){
                try {
                    getNextInLine(p, dir, positions).ifPresent(p1 -> {
                        SyncBlockPosition target = p;
                        Vector3Int dirV = dir.getAsVector();
                        int disX = p1.getX() - target.getX();
                        int disY = p1.getY() - target.getY();
                        int disZ = p1.getZ() - target.getZ();
                        while(!((disX == 0) && (disY == 0) && (disZ == 0))){
                            target = target.getRelative(dir);
                            if(disX != 0) {
                                disX = disX - dirV.getX();
                            }
                            if(disY != 0) {
                                disY = disY - dirV.getY();
                            }
                            if(disZ != 0) {
                                disZ = disZ - dirV.getZ();
                            }
                            if(!target.getBlockType().equals(BlockTypes.AIR.get())){
                                break;
                            }
                            if(toAdd.contains(target)){
                                continue;
                            }
                            toAdd.add(target);
                        }
                    });
                } catch (DirectionNotSupported directionNotSupported) {
                    directionNotSupported.printStackTrace();
                }
            }
        });
        toAdd.forEach(this::addRawPosition);
        return this;
    }

    @Override
    public Set<Vector3Int> getRelativePositions() {
        Set<Vector3Int> vectors = new HashSet<>(getOriginalRelativePositions());
        if (!vectors.stream().anyMatch(v -> v.equals(new Vector3Int(0, 0, 0)))){
            vectors.add(new Vector3Int(0, 0, 0));
        }
        return vectors;
    }

    @Override
    public Set<Vector3Int> getOriginalRelativePositions() {
        return this.vectors;
    }

    @Override
    public boolean addPosition(Vector3Int add) {
        if(this.vectors.stream().anyMatch(v -> v.equals(add))){
            return false;
        }
        return this.vectors.add(add);
    }

    private void addRawPosition(SyncBlockPosition position){
        Vector3Int original = getPosition().getPosition();
        Vector3Int next = position.getPosition();
        this.vectors.add(new Vector3Int((next.getX() - original.getX()), (next.getY() - original.getY()), (next.getZ() - original.getZ())));
    }

    @Override
    public boolean removePosition(Vector3Int remove) {
        if(!this.vectors.stream().anyMatch(v -> v.equals(remove))){
            return false;
        }
        return this.vectors.remove(remove);
    }

    @Override
    public ShipsStructure clear() {
        this.vectors.clear();
        return this;
    }

    @Override
    public ShipsStructure setRaw(Collection<Vector3Int> collection) {
        this.vectors = new HashSet<>(collection);
        return this;
    }

    private static Optional<SyncBlockPosition> getNextInLine(SyncBlockPosition pos, Direction direction, Collection<SyncBlockPosition> collections) throws DirectionNotSupported {
        Vector3Int original = pos.getPosition();
        List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.withYDirections(FourFacingDirection.getFourFacingDirections())));
        if(!directions.contains(direction)){
            throw new DirectionNotSupported(direction, "");
        }
        List<SyncBlockPosition> positions = collections.stream().filter(p -> {
            Vector3Int vector = p.getPosition();
            if(vector.getX().equals(original.getX()) && vector.getY().equals(original.getY())){
                int oz = original.getZ();
                int vz = vector.getZ();
                if(oz < vz && direction.equals(FourFacingDirection.SOUTH)){
                    return true;
                }else if(oz > vz && direction.equals(FourFacingDirection.NORTH)){
                    return true;
                }
            }
            if(vector.getZ().equals(original.getZ()) && vector.getY().equals(original.getY())){
                int ox = original.getX();
                int vx = vector.getX();
                if(ox < vx && direction.equals(FourFacingDirection.EAST)){
                    return true;
                }else if(ox > vx && direction.equals(FourFacingDirection.WEST)){
                    return true;
                }
            }
            if(vector.getX().equals(original.getX()) && vector.getZ().equals(original.getZ())){
                int oy = original.getY();
                int vy = vector.getY();
                if(oy < vy && direction.equals(FourFacingDirection.UP)){
                    return true;
                }else if(oy > vy && direction.equals(FourFacingDirection.DOWN)){
                    return true;
                }
            }
            return false;
        }).filter(p -> !p.getPosition().equals(original)).collect(Collectors.toList());
        double min = Double.MAX_VALUE;
        SyncBlockPosition current = null;
        for(SyncBlockPosition position : positions){
            double distance = position.getPosition().distance(original);
            if(min > distance){
                min = distance;
                current = position;
            }
        }
        return Optional.ofNullable(current);
    }

}
