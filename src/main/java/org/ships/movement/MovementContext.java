package org.ships.movement;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.core.TranslateCore;
import org.core.entity.Entity;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.utils.BarUtils;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.instruction.BlockInstruction;
import org.ships.config.blocks.instruction.CollideType;
import org.ships.config.messages.Messages;
import org.ships.config.messages.messages.error.data.CollideDetectedMessageData;
import org.ships.event.vessel.move.VesselMoveEvent;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.instruction.MovementInstruction;
import org.ships.movement.instruction.actions.MidMovement;
import org.ships.movement.instruction.actions.PostMovement;
import org.ships.movement.instruction.details.MovementDetails;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.SignBasedVessel;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.ShipsSigns;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public Optional<BossBar> getAdventureBossBar() {
        return this.details.getAdventureBossBar();
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

    public CompletableFuture<Void> move(Vessel vessel, boolean updateStructure) throws MoveException {
        this.isVesselLoading(vessel);
        this.isVesselMoving(vessel);
        vessel.set(MovingFlag.class, this);
        Consumer<? super Collection<LiveEntity>> consumer = entities -> {
            try {
                this.movePostEntity(vessel);
            } catch (Throwable e) {
                this
                        .getAdventureBossBar()
                        .ifPresent(bar -> BarUtils.getPlayers(bar).forEach(player -> player.hideBossBar(bar)));
                vessel.set(new MovingFlag());
                entities.forEach(entity -> entity.setGravity(true));
                this.getException().accept(this, e);

                if (e instanceof MoveException) {
                    return;
                }
                e.printStackTrace();
            }
        };
        if (updateStructure) {
            return vessel
                    .updateStructure((currentStructure, block) -> OvertimeBlockFinderUpdate.BlockFindControl.USE)
                    .thenCompose(update -> this.collectEntities(vessel))
                    .thenAccept(consumer);
        }
        return this.collectEntities(vessel).thenAccept(consumer);
    }

    private void movePostEntity(Vessel vessel) throws Exception {
        this.getAdventureBossBar().ifPresent(bossBar -> {
            bossBar.progress(1);
            bossBar.name(Component.text("Processing: Pre"));
        });

        if (ShipsPlugin.getPlugin().getPreventMovementManager().isMovementPrevented()) {
            throw new MoveException(this, Messages.ERROR_PREVENT_MOVEMENT, vessel);
        }

        if (this.isPreMoveEventCancelled(vessel)) {
            return;
        }
        this.getAdventureBossBar().ifPresent(bossBar -> {
            bossBar.name(Component.text("Checking requirements: Sign"));
            bossBar.progress(0.25f);
        });
        if (vessel instanceof SignBasedVessel) {
            this.isLicenceSignValid((SignBasedVessel) vessel);
        }
        this.getAdventureBossBar().ifPresent(bossBar -> {
            bossBar.name(Component.text("Checking requirements: Vessel specific"));
            bossBar.progress(0.50f);
        });
        if (vessel instanceof VesselRequirement) {
            this.isRequirementsValid((VesselRequirement) vessel);
        }
        this.getAdventureBossBar().ifPresent(bossBar -> {
            bossBar.name(Component.text("Checking requirements: Collide"));
            bossBar.progress(0.75f);
        });

        this.isClearFromColliding(vessel);
        if (vessel instanceof VesselRequirement) {
            this.processRequirements((VesselRequirement) vessel);
        }
        this.getAdventureBossBar().ifPresent(bossBar -> {
            bossBar.progress(1);
            bossBar.name(Component.text("Processing: Movement setup"));
        });
        this.processMovement(vessel);
    }

    private void processMovement(Vessel vessel) throws Exception {
        VesselMoveEvent.Main eventMain = new VesselMoveEvent.Main(vessel, this);
        if (TranslateCore.getPlatform().callEvent(eventMain).isCancelled()) {
            throw new Exception("MoveEvent Main was cancelled");
        }
        this.entities.keySet().forEach(e -> e.getCreatedFrom().ifPresent(entity -> entity.setGravity(false)));
        this.getAdventureBossBar().ifPresent(bossBar -> bossBar.name(Component.text("Processing: Moving")));

        Result result = this.getMovement().move(vessel, this);
        this.getAdventureBossBar().ifPresent(bossBar -> bossBar.name(Component.text("Processing: Post Moving")));
        result.run(vessel, this);
        for (PostMovement postMovement : this.getPostMovementProcess()) {
            postMovement.postMove(vessel);
        }
    }

    private void processRequirements(VesselRequirement vessel) throws MoveException {
        this.getAdventureBossBar().ifPresent(bossBar -> bossBar.name(Component.text("Processing: Requirements")));
        vessel.finishRequirements(this);
    }

    private void isClearFromColliding(@NotNull Vessel vessel) throws MoveException {
        var player = vessel
                .getPosition()
                .getWorld()
                .getLiveEntities()
                .filter(e -> e instanceof LivePlayer)
                .map(e -> (LivePlayer) e)
                .findAny()
                .orElseThrow();
        Set<SyncBlockPosition> collided = this.getMovingStructure().stream().filter(mb -> {
            SyncBlockPosition after = mb.getAfterPosition();

            mb.getBeforePosition().setBlock(BlockTypes.GLOWSTONE.getDefaultBlockDetails(), player);
            after.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player);


            if (this.getMovingStructure().stream().anyMatch(mb1 -> {
                return after.equals(mb1.getBeforePosition());
            })) {
                return false;
            }
            for (BlockType type : vessel.getType().getIgnoredTypes()) {
                if (type.equals(after.getBlockType())) {
                    return false;
                }
            }
            BlockList list = ShipsPlugin.getPlugin().getBlockList();
            BlockInstruction bi = list.getBlockInstruction(after.getBlockType());
            if (bi.getCollide() == CollideType.IGNORE) {
                return false;
            }
            return true;
        }).map(MovingBlock::getAfterPosition).collect(Collectors.toSet());
        if (collided.isEmpty()) {
            return;
        }

        VesselMoveEvent.CollideDetected collideEvent = new VesselMoveEvent.CollideDetected(vessel, this, collided);
        TranslateCore.getPlatform().callEvent(collideEvent);

        throw new MoveException(this, Messages.ERROR_COLLIDE_DETECTED, new CollideDetectedMessageData(vessel, collided
                .parallelStream()
                .collect(Collectors.toSet())));
    }

    private void isRequirementsValid(VesselRequirement vessel) throws MoveException {
        vessel.checkRequirements(this);
    }

    private void isLicenceSignValid(Vessel vessel) throws MoveException {
        Optional<MovingBlock> opLicence = this.getMovingStructure().get(ShipsSigns.LICENCE);
        if (opLicence.isPresent()) {
            return;
        }
        throw new MoveException(this, Messages.ERROR_FAILED_TO_FIND_LICENCE_SIGN, vessel.getStructure());
    }

    private boolean isPreMoveEventCancelled(Vessel vessel) {
        VesselMoveEvent.Pre preEvent = new VesselMoveEvent.Pre(vessel, this);
        if (!TranslateCore.getPlatform().callEvent(preEvent).isCancelled()) {
            return false;
        }
        this.getAdventureBossBar().ifPresent(bar -> BarUtils.getPlayers(bar).forEach(user -> user.hideBossBar(bar)));
        this.getClicked().ifPresent(clicked -> ShipsPlugin.getPlugin().getLockedSignManager().unlock(clicked));
        vessel.set(MovingFlag.class, null);
        return true;

    }

    private CompletableFuture<Collection<LiveEntity>> collectEntities(Vessel vessel) {
        return vessel.getEntitiesOvertime(e -> true).thenApply(e -> {
            e.forEach(entity -> {
                try {
                    this.saveEntity(vessel, entity, e.size());
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            });
            return e;
        });
    }

    private void saveEntity(@NotNull Vessel vessel, @NotNull Entity<LiveEntity> entity, int totalSize) {
        EntitySnapshot<? extends LiveEntity> snapshot = entity.createSnapshot();
        if (snapshot == null) {
            return;
        }
        Optional<SyncBlockPosition> opAttached = entity.getAttachedTo();
        if (opAttached.isEmpty()) {
            return;
        }
        Optional<MovingBlock> mBlock = this.getMovingStructure().getBefore(opAttached.get());
        if (mBlock.isEmpty()) {
            SyncBlockPosition position = snapshot.getPosition().toBlockPosition();
            Stream<SyncBlockPosition> positions = vessel.getStructure().getPositionsRelativeToWorld();
            Optional<SyncBlockPosition> opDown = positions
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
        this.getAdventureBossBar().ifPresent(bossBar -> {
            float progress = this.entities.size() / (float) totalSize;
            progress = progress / 100;

            bossBar.name(Component.text("Collecting entities: " + this.entities.size()));
            bossBar.progress(progress);
        });
    }

    private void isVesselMoving(Vessel vessel) throws MoveException {
        Optional<MovementContext> opMoving = vessel.getValue(MovingFlag.class);
        if (opMoving.isEmpty()) {
            return;
        }
        throw new MoveException(this, Messages.ERROR_ALREADY_MOVING, vessel);
    }

    private void isVesselLoading(Vessel vessel) throws MoveException {
        if (!vessel.isLoading()) {
            return;
        }
        throw new MoveException(this, Messages.ERROR_VESSEL_STILL_LOADING, vessel);
    }
}
