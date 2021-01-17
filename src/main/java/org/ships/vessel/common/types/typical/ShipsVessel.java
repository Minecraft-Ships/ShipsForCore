package org.ships.vessel.common.types.typical;

import org.core.CorePlugin;
import org.core.utils.Identifable;
import org.core.vector.type.Vector3;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.core.world.position.impl.sync.SyncPosition;
import org.ships.config.blocks.BlockListable;
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

public interface ShipsVessel extends SignBasedVessel, TeleportToVessel, CrewStoredVessel, WritableNameVessel, BlockListable, FileBasedVessel, Identifable {

    Map<String, String> getExtraInformation();
    Collection<VesselFlag<?>> getFlags();

    @Override
    default LiveSignTileEntity getSign() throws NoLicencePresent {
        Optional<LiveTileEntity> opTile = this.getPosition().getTileEntity();
        if(!opTile.isPresent()){
            throw new NoLicencePresent(this);
        }
        LiveTileEntity tile = opTile.get();
        if(!(tile instanceof LiveSignTileEntity)){
            throw new NoLicencePresent(this);
        }
        LiveSignTileEntity sign = (LiveSignTileEntity)tile;
        LicenceSign licenceSign = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
        if(!licenceSign.isSign(sign)){
            throw new NoLicencePresent(this);
        }
        return sign;
    }

    @Override
    default ShipsVessel setName(String name) throws NoLicencePresent{
        getSign().setLine(2, CorePlugin.buildText(name));
        File file = getFile();
        String[] ext = file.getName().split(Pattern.quote("."));
        file.renameTo(new File(file.getParentFile(), name + "." + ext[ext.length - 1]));
        return this;
    }

    @Override
    default String getName() {
        try {
            return getSign().getLine(2).get().toPlain();
        } catch (NoLicencePresent e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    default void moveTowards(int x, int y, int z, MovementContext context, Consumer<Throwable> exception){
        Movement.MidMovement.ADD_TO_POSITION.move(this, x, y, z, context, exception);
    }

    @Override
    default void moveTowards(Vector3<Integer> vector, MovementContext context, Consumer<Throwable> exception){
        Movement.MidMovement.ADD_TO_POSITION.move(this, vector, context, exception);
    }

    @Override
    default void moveTo(SyncPosition<? extends Number> location, MovementContext context, Consumer<Throwable> exception){
        SyncBlockPosition position = location instanceof SyncBlockPosition ? (SyncBlockPosition)location : ((SyncExactPosition)location).toBlockPosition();
        Movement.MidMovement.TELEPORT_TO_POSITION.move(this, position, context, exception);
    }

    @Override
    default void rotateRightAround(SyncPosition<? extends Number> location, MovementContext context, Consumer<Throwable> exception){
        SyncBlockPosition position = location instanceof SyncBlockPosition ? (SyncBlockPosition)location : ((SyncExactPosition)location).toBlockPosition();
        Movement.MidMovement.ROTATE_RIGHT_AROUND_POSITION.move(this, position, context, exception);
    }

    @Override
    default void rotateLeftAround(SyncPosition<? extends Number> location, MovementContext context, Consumer<Throwable> exception){
        SyncBlockPosition position = location instanceof SyncBlockPosition ? (SyncBlockPosition)location : ((SyncExactPosition)location).toBlockPosition();
        Movement.MidMovement.ROTATE_LEFT_AROUND_POSITION.move(this, position, context, exception);
    }

    @Override
    default String getId(){
        return getType().getId() + "." + getName().toLowerCase();
    }
}
