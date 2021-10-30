package org.ships.vessel.common.types.typical;

import org.core.adventureText.AText;
import org.core.vector.type.Vector3;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.core.world.position.impl.sync.SyncPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.Movement;
import org.ships.movement.MovementContext;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.*;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.sign.LicenceSign;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public interface ShipsVessel extends SignBasedVessel, TeleportToVessel, CrewStoredVessel, WritableNameVessel, FileBasedVessel, IdentifiableShip {

    @NotNull Map<String, String> getExtraInformation();

    @NotNull Collection<VesselFlag<?>> getFlags();

    @Override
    default @NotNull LiveSignTileEntity getSign() throws NoLicencePresent {
        Optional<LiveTileEntity> opTile = this.getPosition().getTileEntity();
        if (!opTile.isPresent()) {
            throw new NoLicencePresent(this);
        }
        LiveTileEntity tile = opTile.get();
        if (!(tile instanceof LiveSignTileEntity)) {
            throw new NoLicencePresent(this);
        }
        LiveSignTileEntity sign = (LiveSignTileEntity) tile;
        LicenceSign licenceSign = ShipsPlugin.getPlugin().get(LicenceSign.class).orElseThrow(() -> new IllegalStateException("Could not get licence sign builder"));
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
    default void moveTowards(int x, int y, int z, @NotNull MovementContext context, @NotNull Consumer<Throwable> exception) {
        Movement.MidMovement.ADD_TO_POSITION.move(this, x, y, z, context, exception);
    }

    @Override
    default void moveTowards(@NotNull Vector3<Integer> vector, @NotNull MovementContext context, @NotNull Consumer<Throwable> exception) {
        Movement.MidMovement.ADD_TO_POSITION.move(this, vector, context, exception);
    }

    @Override
    default void moveTo(@NotNull SyncPosition<? extends Number> location, @NotNull MovementContext context, @NotNull Consumer<Throwable> exception) {
        SyncBlockPosition position = location instanceof SyncBlockPosition ? (SyncBlockPosition) location:((SyncExactPosition) location).toBlockPosition();
        Movement.MidMovement.TELEPORT_TO_POSITION.move(this, position, context, exception);
    }

    @Override
    default void rotateRightAround(@NotNull SyncPosition<? extends Number> location, @NotNull MovementContext context, @NotNull Consumer<Throwable> exception) {
        SyncBlockPosition position = location instanceof SyncBlockPosition ? (SyncBlockPosition) location:((SyncExactPosition) location).toBlockPosition();
        Movement.MidMovement.ROTATE_RIGHT_AROUND_POSITION.move(this, position, context, exception);
    }

    @Override
    default void rotateLeftAround(@NotNull SyncPosition<? extends Number> location, @NotNull MovementContext context, @NotNull Consumer<Throwable> exception) {
        SyncBlockPosition position = location instanceof SyncBlockPosition ? (SyncBlockPosition) location:((SyncExactPosition) location).toBlockPosition();
        Movement.MidMovement.ROTATE_LEFT_AROUND_POSITION.move(this, position, context, exception);
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
