package org.ships.vessel.common.types.typical;

import org.core.adventureText.AText;
import org.core.vector.type.Vector3;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.NoLicencePresent;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.instruction.MovementInstructionBuilder;
import org.ships.movement.instruction.actions.MidMovements;
import org.ships.movement.instruction.details.MovementDetails;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.*;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.sign.LicenceSign;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public interface ShipsVessel
        extends SignBasedVessel, TeleportToVessel, CrewStoredVessel, WritableNameVessel, FileBasedVessel,
        IdentifiableShip {

    @NotNull Map<String, String> getExtraInformation();

    @NotNull Collection<VesselFlag<?>> getFlags();

    @Override
    default @NotNull LiveSignTileEntity getSign() throws NoLicencePresent {
        Optional<LiveTileEntity> opTile = this.getPosition().getTileEntity();
        if (opTile.isEmpty()) {
            throw new NoLicencePresent(this);
        }
        LiveTileEntity tile = opTile.get();
        if (!(tile instanceof LiveSignTileEntity)) {
            throw new NoLicencePresent(this);
        }
        LiveSignTileEntity sign = (LiveSignTileEntity)tile;
        LicenceSign licenceSign = ShipsPlugin
                .getPlugin()
                .get(LicenceSign.class)
                .orElseThrow(() -> new IllegalStateException("Could not get licence sign builder"));
        if (!licenceSign.isSign(sign)) {
            throw new NoLicencePresent(this);
        }
        return sign;
    }

    @Override
    default @NotNull ShipsVessel setName(@NotNull String name) throws NoLicencePresent {
        this.getSign().setTextAt(2, AText.ofPlain(name));
        File file = this.getFile();
        String[] ext = file.getName().split(Pattern.quote("."));
        file.renameTo(new File(file.getParentFile(), name + "." + ext[ext.length - 1]));
        return this;
    }

    @Override
    default void moveTowards(int x, int y, int z, @NotNull MovementDetails details) {
        boolean strict = x == z && z == 0 && y < 0;
        MovementContext context = new MovementContext(details, new MovementInstructionBuilder()
                .setAddToMovementBlocks(this.getStructure(), x, y, z)
                .setStrictMovement(strict)
                .build());
        try {
            context.move(this, details.isUpdatingStructure());
        } catch (MoveException e) {
            details.getException().accept(context, e);
        }
    }

    @Override
    default void moveTowards(@NotNull Vector3<Integer> vector, @NotNull MovementDetails details) {
        boolean strict = Objects.equals(vector.getX(), vector.getZ()) && vector.getZ() == 0 && vector.getY() < 0;
        MovementContext context = new MovementContext(details, new MovementInstructionBuilder()
                .setAddToMovementBlocks(this.getStructure(), vector)
                .setStrictMovement(strict)
                .build());
        try {
            context.move(this, details.isUpdatingStructure());
        } catch (MoveException e) {
            details.getException().accept(context, e);
        }


    }

    @Override
    default void moveTo(@NotNull BlockPosition location, @NotNull MovementDetails details) {
        MovementContext context = new MovementContext(details, new MovementInstructionBuilder()
                .setTeleportToMovementBlocks(this.getStructure(), location)
                .build());
        try {
            context.move(this, details.isUpdatingStructure());
        } catch (MoveException e) {
            details.getException().accept(context, e);
        }
    }

    @Override
    default void rotateRightAround(@NotNull BlockPosition location, @NotNull MovementDetails details) {
        MovementContext context = new MovementContext(details, new MovementInstructionBuilder()
                .setRotateRightAroundPosition(this.getStructure(), location)
                .setMidMoveEvent(MidMovements.ROTATE_BLOCKS_RIGHT)
                .build());
        try {
            context.move(this, details.isUpdatingStructure());
        } catch (MoveException e) {
            details.getException().accept(context, e);
        }
    }

    @Override
    default void rotateLeftAround(@NotNull BlockPosition location, @NotNull MovementDetails details) {
        MovementContext context = new MovementContext(details, new MovementInstructionBuilder()
                .setRotateLeftAroundPosition(this.getStructure(), location)
                .setMidMoveEvent(MidMovements.ROTATE_BLOCKS_LEFT)
                .build());
        try {
            context.move(this, details.isUpdatingStructure());
        } catch (MoveException e) {
            details.getException().accept(context, e);
        }
    }

    @Override
    default @NotNull String getId() throws NoLicencePresent {
        Optional<String> opCachedName = this.getCachedName();
        if (opCachedName.isPresent()) {
            return this.getType().getId() + "." + opCachedName.get().toLowerCase();
        }
        return this.getType().getId() + "." + this.getName().toLowerCase();
    }
}
