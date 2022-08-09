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
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.configuration.ShipsConfig;
import org.ships.event.vessel.move.VesselMoveEvent;
import org.ships.exceptions.MoveException;
import org.ships.movement.instruction.MovementInstruction;
import org.ships.movement.instruction.actions.MidMovement;
import org.ships.movement.instruction.actions.PostMovement;
import org.ships.movement.instruction.details.MovementDetails;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MovementContext {

    private final @NotNull Map<EntitySnapshot<? extends LiveEntity>, MovingBlock> entities = new HashMap<>();
    private final @NotNull MovementDetails details;
    private final @NotNull MovementInstruction instructions;

    public MovementContext(@NotNull MovementDetails details, @NotNull MovementInstruction instruction) {
        this.details = details;
        this.instructions = instruction;
    }

    public Optional<BlockPosition> getClicked() {
        return this.details.getClickedBlock();
    }

    public Optional<ServerBossBar> getBossBar() {
        return this.details.getBossBar();
    }

    public boolean isStrictMovement() {
        return this.instructions.isStrictMovement();
    }

    public MovingBlockSet getMovingStructure() {
        return this.instructions.getMovingBlocks();
    }

    public @NotNull BasicMovement getMovement() {
        return this.instructions.getMovementAlgorithm();
    }

    public BiConsumer<MovementContext, ? super Throwable> getException() {
        return this.details.getException();
    }

    public @NotNull Map<EntitySnapshot<? extends LiveEntity>, MovingBlock> getEntities() {
        return this.entities;
    }

    public PostMovement[] getPostMovementProcess() {
        PostMovement[] instructPost = this.instructions.getPostMoveEvent();
        PostMovement[] detailsPost = this.details.getPostMovementEvents();
        PostMovement[] ret = new PostMovement[instructPost.length + detailsPost.length];
        int i = 0;
        for (; i < instructPost.length; i++) {
            ret[i] = instructPost[i];
            i++;
        }
        for (; (i - instructPost.length) < detailsPost.length; i++) {
            ret[i - instructPost.length] = detailsPost[i];
            i++;
        }
        return ret;
    }

    public MidMovement[] getMidMovementProcess() {
        MidMovement[] instructMid = this.instructions.getMidMoveEvent();
        MidMovement[] detailsMid = this.details.getMidMovementEvents();
        MidMovement[] ret = new MidMovement[instructMid.length + detailsMid.length];
        int i = 0;
        for (; i < instructMid.length; i++) {
            ret[i] = instructMid[i];
            i++;
        }
        for (; (i - instructMid.length) < detailsMid.length; i++) {
            ret[i - instructMid.length] = detailsMid[i];
            i++;
        }
        return ret;
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
                this.getBossBar().ifPresent(ServerBossBar::deregisterPlayers);
                vessel.set(new MovingFlag());
                entities.forEach(entity -> entity.setGravity(true));
                this.getException().accept(this, e);

                if (e instanceof MoveException) {
                    return;
                }
                e.printStackTrace();
            }
        });
    }

    private void movePostEntity(Vessel vessel, Collection<LiveEntity> entities) throws Exception {
        this.getBossBar().ifPresent(bossBar -> {
            bossBar.setValue(100);
            bossBar.setTitle(AText.ofPlain("Processing: Pre"));
        });

        if (this.isPreMoveEventCancelled(vessel)) {
            return;
        }
        this.getBossBar().ifPresent(bossBar -> {
            bossBar.setTitle(AText.ofPlain("Checking requirements: Sign"));
            bossBar.setValue(25);
        });
        if (vessel instanceof SignBasedVessel signBasedVessel) {
            this.isLicenceSignValid(signBasedVessel);
        }
        this.getBossBar().ifPresent(bossBar -> {
            bossBar.setTitle(AText.ofPlain("Checking requirements: Vessel specific"));
            bossBar.setValue(50);
        });
        if (vessel instanceof VesselRequirement vesselRequirement) {
            this.isRequirementsValid(vesselRequirement);
        }
        this.getBossBar().ifPresent(bossBar -> {
            bossBar.setTitle(AText.ofPlain("Checking requirements: Collide"));
            bossBar.setValue(75);
        });

        this.isClearFromColliding(vessel);
        if (vessel instanceof VesselRequirement vesselRequirement) {
            this.processRequirements(vesselRequirement);
        }
        this.getBossBar().ifPresent(bossBar -> {
            bossBar.setValue(100);
            bossBar.setTitle(AText.ofPlain("Processing: Movement setup"));
        });
        this.processMovement(vessel);
    }

    private void processMovement(Vessel vessel) throws Exception {
        VesselMoveEvent.Main eventMain = new VesselMoveEvent.Main(vessel, this);
        if (TranslateCore.getPlatform().callEvent(eventMain).isCancelled()) {
            throw new Exception("MoveEvent Main was cancelled");
        }
        this.entities.keySet().forEach(e -> e.getCreatedFrom().ifPresent(entity -> entity.setGravity(false)));
        this.getBossBar().ifPresent(bossBar -> bossBar.setTitle(AText.ofPlain("Processing: Moving")));

        this.getMovingStructure().applyMovingBlocks();
        Result result = this.getMovement().move(vessel, this);
        this.getBossBar().ifPresent(bossBar -> bossBar.setTitle(AText.ofPlain("Processing: Post Moving")));
        result.run(vessel, this);
        for (PostMovement postMovement : this.getPostMovementProcess()) {
            postMovement.postMove(vessel);
        }
    }

    private void processRequirements(VesselRequirement vessel) throws MoveException {
        AText.ofPlain("Processing: Requirements");
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
        this.getBossBar().ifPresent(ServerBossBar::deregisterPlayers);
        this.getClicked().ifPresent(ShipsSign.LOCKED_SIGNS::remove);
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
                mBlock = this.getMovingStructure().getBefore(opDown.get());
            }
            if (mBlock.isEmpty()) {
                return;
            }
            this.entities.put(snapshot, mBlock.get());
            this.getBossBar().ifPresent(bossBar -> AText.ofPlain("Collecting entities: " + this.entities.size()));
        }, after);
    }

    private boolean isVesselMoving(Vessel vessel) {
        Optional<MovementContext> opMoving = vessel.getValue(MovingFlag.class);
        if (opMoving.isEmpty()) {
            return false;
        }

        this.getBossBar().ifPresent(ServerBossBar::deregisterPlayers);
        this.getException().accept(this, new MoveException(
                new AbstractFailedMovement<>(vessel, MovementResult.VESSEL_MOVING_ALREADY, true)));
        return true;
    }

    private boolean isVesselLoading(Vessel vessel) {
        if (!vessel.isLoading()) {
            return false;
        }
        this.getBossBar().ifPresent(ServerBossBar::deregisterPlayers);
        this
                .getException()
                .accept(this, new MoveException(
                        new AbstractFailedMovement<>(vessel, MovementResult.VESSEL_STILL_LOADING, null)));
        return true;

    }
}
