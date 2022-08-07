package org.ships.movement;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.world.boss.ServerBossBar;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.BlockType;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.configuration.ShipsConfig;
import org.ships.event.vessel.move.VesselMoveEvent;
import org.ships.exceptions.MoveException;
import org.ships.movement.instruction.MovementInstruction;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.SignBasedVessel;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.sign.ShipsSign;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MovementContext {

    private final Map<EntitySnapshot<? extends LiveEntity>, MovingBlock> entities = new HashMap<>();
    private ServerBossBar bar;
    private BlockPosition clicked;
    //protected boolean strictMovement;
    //protected MovingBlockSet blocks;
    //protected BasicMovement movement;
    //protected Movement.MidMovement[] midMovementProcess = new Movement.MidMovement[0];
    //protected Movement.PostMovement[] postMovementProcess = new Movement.PostMovement[0];
    //protected Consumer<VesselMoveEvent.Post> post = (e) -> {
    //};
    private MovementInstruction instructions;

    public void setInstruction(MovementInstruction instruction) {
        this.instructions = instruction;
    }

    public Optional<BlockPosition> getClicked() {
        return Optional.ofNullable(this.clicked);
    }

    public MovementContext setClicked(BlockPosition position) {
        this.clicked = position;
        return this;
    }

    public Optional<ServerBossBar> getBar() {
        return Optional.ofNullable(this.bar);
    }

    public MovementContext setBar(ServerBossBar bar) {
        this.bar = bar;
        return this;
    }

    public boolean isStrictMovement() {
        //return this.strictMovement;
        return this.instructions.isStrictMovement();
    }

    @Deprecated
    public MovementContext setStrictMovement(boolean check) {
        //this.strictMovement = check;
        return this;
    }

    public MovingBlockSet getMovingStructure() {
        //return this.blocks;

        return this.instructions.getMovingBlocks();
    }

    @Deprecated
    public MovementContext setMovingStructure(MovingBlockSet set) {
        //this.blocks = set;
        return this;
    }

    public BasicMovement getMovement() {
        //return this.movement;

        return this.instructions.getMovementAlgorithm();
    }

    @Deprecated
    public MovementContext setMovement(BasicMovement movement) {
        //this.movement = movement;
        return this;
    }

    public Map<EntitySnapshot<? extends LiveEntity>, MovingBlock> getEntities() {
        return this.entities;
    }

    public Movement.PostMovement[] getPostMovementProcess() {
        //return this.postMovementProcess;
        return this.instructions.getPostMoveEvent();
    }

    @Deprecated
    public MovementContext setPostMovementProcess(Movement.PostMovement... postMovement) {
        //this.postMovementProcess = postMovement;
        return this;
    }

    public Movement.MidMovement[] getMidMovementProcess() {
        //return this.midMovementProcess;
        return this.instructions.getMidMoveEvent();
    }

    @Deprecated
    public MovementContext setMidMovementProcess(Movement.MidMovement... midMovement) {
        //this.midMovementProcess = midMovement;
        return this;
    }

    @Deprecated
    public Consumer<VesselMoveEvent.Post> getPostMovement() {
        //return this.post;
        return (post) -> {
            Arrays.stream(this.instructions.getPostMoveEvent()).forEach(post1 -> post1.postMove(post.getVessel()));
        };
    }

    @Deprecated
    public MovementContext setPostMovement(Consumer<VesselMoveEvent.Post> consumer) {
        //this.post = consumer;
        return this;
    }

    public void move(Vessel vessel) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        if (this.isVesselLoading(vessel)) {
            return;
        }
        if (this.isVesselMoving(vessel)) {
            return;
        }
        vessel.set(MovingFlag.class, this);
        this.collectEntities(vessel, config, entities -> {
            try {
                this.movePostEntity(vessel, entities);
            } catch (Throwable e) {
                if (this.bar != null) {
                    this.bar.deregisterPlayers();
                }
                vessel.set(new MovingFlag());
                entities.forEach(entity -> entity.setGravity(true));
                this.instructions.getException().accept(e);

                if (e instanceof MoveException) {
                    return;
                }
                e.printStackTrace();
            }
        });
    }

    private void movePostEntity(Vessel vessel, Collection<LiveEntity> entities) throws Exception {
        if (this.bar != null) {
            this.bar.setValue(100);
            this.bar.setTitle(AText.ofPlain("Processing: Pre"));
        }
        if (this.isPreMoveEventCancelled(vessel)) {
            return;
        }
        if (vessel instanceof SignBasedVessel signBasedVessel) {
            this.isLicenceSignValid(signBasedVessel);
        }
        if (vessel instanceof VesselRequirement vesselRequirement) {
            this.isRequirementsValid(vesselRequirement);
        }
        if (this.bar != null) {
            this.bar.setTitle(AText.ofPlain("Checking requirements: Collide"));
        }
        this.isClearFromColliding(vessel);
        if (vessel instanceof VesselRequirement vesselRequirement) {
            this.processRequirements(vesselRequirement);
        }
        if (this.bar != null) {
            this.bar.setTitle(AText.ofPlain("Processing: Movement setup"));
        }
        this.processMovement(vessel);
    }

    private void processMovement(Vessel vessel) throws Exception {
        VesselMoveEvent.Main eventMain = new VesselMoveEvent.Main(vessel, this);
        if (TranslateCore.getPlatform().callEvent(eventMain).isCancelled()) {
            throw new Exception("MoveEvent Main was cancelled");
        }
        this.entities.keySet().forEach(e -> e.getCreatedFrom().ifPresent(entity -> entity.setGravity(false)));
        if (this.bar != null) {
            this.bar.setTitle(AText.ofPlain("Processing: Moving"));
        }
        this.instructions.getMovingBlocks().applyMovingBlocks();
        Result result = this.instructions.getMovementAlgorithm().move(vessel, this);
        if (this.bar != null) {
            this.bar.setTitle(AText.ofPlain("Processing: Post Moving"));
        }
        result.run(vessel, this);
        for (Movement.PostMovement postMovement : this.instructions.getPostMoveEvent()) {
            postMovement.postMove(vessel);
        }
    }

    private void processRequirements(VesselRequirement vessel) throws MoveException {
        if (this.bar != null) {
            this.bar.setTitle(AText.ofPlain("Processing requirements:"));
        }
        vessel.finishRequirements(this);
    }

    private void isClearFromColliding(Vessel vessel) throws MoveException {
        Set<SyncBlockPosition> collided = this
                .getMovingStructure()
                .stream()
                .filter(mb -> {
                    SyncBlockPosition after = mb.getAfterPosition();
                    if (this
                            .getMovingStructure()
                            .stream()
                            .anyMatch(mb1 -> after.equals(mb1.getBeforePosition()))) {
                        return false;
                    }
                    for (BlockType type : vessel.getType().getIgnoredTypes()) {
                        if (type.equals(after.getBlockType())) {
                            return false;
                        }
                    }
                    BlockList list = ShipsPlugin.getPlugin().getBlockList();
                    BlockInstruction bi = list.getBlockInstruction(after.getBlockType());
                    return bi.getCollideType() != BlockInstruction.CollideType.IGNORE;
                })
                .map(MovingBlock::getAfterPosition)
                .collect(Collectors.toSet());
        if (collided.isEmpty()) {
            return;
        }

        VesselMoveEvent.CollideDetected collideEvent = new VesselMoveEvent.CollideDetected(vessel, this,
                collided);
        TranslateCore.getPlatform().callEvent(collideEvent);

        throw new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.COLLIDE_DETECTED,
                new HashSet<>(collideEvent.getCollisions())));

    }

    private void isRequirementsValid(VesselRequirement vessel) throws MoveException {
        if (this.bar != null) {
            this.bar.setTitle(AText.ofPlain("Checking requirements:"));
        }
        vessel.checkRequirements(this);
    }

    private void isLicenceSignValid(SignBasedVessel vessel) throws MoveException {
        Optional<MovingBlock> opLicence = this
                .getMovingStructure()
                .get(ShipsPlugin
                        .getPlugin()
                        .get(LicenceSign.class)
                        .orElseThrow(() -> new RuntimeException("Could not find licence sign class")));
        if (opLicence.isPresent()) {
            return;
        }
        throw new MoveException(new AbstractFailedMovement<>(vessel, MovementResult.NO_LICENCE_FOUND, null));
    }

    private boolean isPreMoveEventCancelled(Vessel vessel) {
        VesselMoveEvent.Pre preEvent = new VesselMoveEvent.Pre(vessel, this);
        if (!TranslateCore.getPlatform().callEvent(preEvent).isCancelled()) {
            return false;
        }
        if (this.bar != null) {
            this.bar.deregisterPlayers();
        }
        if (this.clicked != null) {
            ShipsSign.LOCKED_SIGNS.remove(this.clicked);
        }
        vessel.set(MovingFlag.class, null);
        return true;

    }

    private void collectEntities(Vessel vessel, ShipsConfig config, Consumer<Collection<LiveEntity>> after) {
        vessel.getEntitiesOvertime(config.getEntityTrackingLimit(), e -> true, e -> {
            EntitySnapshot<? extends LiveEntity> snapshot = e.createSnapshot();
            if (snapshot == null) {
                return;
            }
            Optional<SyncBlockPosition> opAttached = e.getAttachedTo();
            if (opAttached.isEmpty()) {
                return;
            }
            Optional<MovingBlock> mBlock = this.getMovingStructure().getBefore(opAttached.get());
            if (mBlock.isEmpty()) {
                SyncBlockPosition position = snapshot.getPosition().toBlockPosition();
                Collection<SyncBlockPosition> positions = vessel
                        .getStructure()
                        .getPositions((Function<? super SyncBlockPosition, ? extends SyncBlockPosition>) t -> t);
                Optional<SyncBlockPosition> opDown = positions
                        .stream()
                        .filter(f -> position.isInLineOfSight(f.getPosition(), FourFacingDirection.DOWN))
                        .findAny();
                if (opDown.isEmpty()) {
                    return;
                }
                mBlock = this.instructions.getMovingBlocks().getBefore(opDown.get());
            }
            if (mBlock.isEmpty()) {
                return;
            }
            this.entities.put(snapshot, mBlock.get());
            if (this.bar != null) {
                this.bar.setTitle(AText.ofPlain("Collecting entities: " + this.entities.size()));
            }
        }, after);
    }

    private boolean isVesselMoving(Vessel vessel) {
        Optional<MovementContext> opMoving = vessel.getValue(MovingFlag.class);
        if (opMoving.isEmpty()) {
            return false;
        }
        if (this.bar != null) {
            this.bar.deregisterPlayers();
        }
        this.instructions.getException().accept(new MoveException(
                new AbstractFailedMovement<>(vessel, MovementResult.VESSEL_MOVING_ALREADY, true)));
        return true;
    }

    private boolean isVesselLoading(Vessel vessel) {
        if (!vessel.isLoading()) {
            return false;
        }
        if (this.bar != null) {
            this.bar.deregisterPlayers();
        }
        this.instructions
                .getException()
                .accept(new MoveException(
                        new AbstractFailedMovement<>(vessel, MovementResult.VESSEL_STILL_LOADING, null)));
        return true;

    }
}
