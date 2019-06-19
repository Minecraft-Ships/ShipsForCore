package org.ships.movement.autopilot.scheduler;

import org.core.entity.living.human.player.LivePlayer;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.exceptions.MoveException;
import org.ships.movement.result.FailedMovement;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.EOTSign;

import java.util.Collection;
import java.util.Optional;

public class EOTExecutor implements Runnable {

    protected Vessel vessel;
    protected LivePlayer player;

    public EOTExecutor(LivePlayer player, Vessel vessel){
        this.vessel = vessel;
        this.player = player;
    }

    public EOTExecutor setPlayer(LivePlayer player){
        this.player = player;
        return this;
    }

    public EOTExecutor setVessel(Vessel type){
        this.vessel = type;
        return this;
    }

    public Vessel getVessel(){
        return this.vessel;
    }

    public LivePlayer getPlayer(){
        return this.player;
    }

    public Optional<BlockPosition> getSign(){
        Collection<BlockPosition> blocks = getVessel().getStructure().getAll(SignTileEntity.class);
        EOTSign sign = ShipsPlugin.getPlugin().get(EOTSign.class).get();
        return blocks.stream().filter(b -> sign.isSign((SignTileEntity) b.getTileEntity().get())).findFirst();
    }

    @Override
    public void run() {
        getSign().ifPresent(b -> {
            Optional<DirectionalData> directionalData = b.getBlockDetails().getDirectionalData();
            if(!directionalData.isPresent()){
                return;
            }
            try {
                getVessel().moveTowards(directionalData.get().getDirection().getAsVector().multiply(ShipsPlugin.getPlugin().getConfig().getEOTSpeed()), ShipsPlugin.getPlugin().getConfig().getDefaultMovement());
            } catch (MoveException e) {
                sendError(e.getMovement());
            }
        });
    }

    private <T> void sendError(FailedMovement<T> failedMovement){
        failedMovement.sendMessage(this.player, failedMovement.getValue().orElse(null));
    }
}
