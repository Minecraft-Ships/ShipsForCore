package org.ships.movement.autopilot.scheduler;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.result.FailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.EOTSign;

import java.util.Collection;
import java.util.Optional;

public class EOTExecutor implements Runnable {

    protected Vessel vessel;
    protected LivePlayer player;
    protected boolean disableOnNoPilot;

    public EOTExecutor(LivePlayer player, Vessel vessel){
        this.vessel = vessel;
        this.player = player;
    }

    public EOTExecutor setDisableOnNoPilot(boolean check){
        this.disableOnNoPilot = check;
        return this;
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

    public boolean willDisableIfNoPilot(){
        return this.disableOnNoPilot;
    }

    public Optional<SyncBlockPosition> getSign(){
        Collection<SyncBlockPosition> blocks = getVessel().getStructure().getAll(SignTileEntity.class);
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
            if(this.disableOnNoPilot && vessel instanceof CrewStoredVessel){
                boolean check = vessel.getEntities(LivePlayer.class).stream().anyMatch(e -> ((CrewStoredVessel)vessel).getPermission(e.getUniqueId()).canMove());
                if(!check){
                    return;
                }
            }
            MovementContext context = new MovementContext().setMovement(ShipsPlugin.getPlugin().getConfig().getDefaultMovement());
            if(ShipsPlugin.getPlugin().getConfig().isBossBarVisible()){
                ServerBossBar bar2 = CorePlugin.createBossBar();
                vessel.getEntities(LivePlayer.class).forEach(e -> bar2.register(e));
                context.setBar(bar2);
            }
            try {
                this.vessel.moveTowards(directionalData.get().getDirection().getOpposite().getAsVector().multiply(ShipsPlugin.getPlugin().getConfig().getEOTSpeed()), context);
            } catch (MoveException e) {
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                if(e.getMovement() instanceof MovementResult.VesselMovingAlready){
                    return;
                }
                sendError(e.getMovement());
            }catch (Throwable e2){
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                this.vessel.getEntities().forEach(e -> e.setGravity(true));
            }
        });
    }

    private <T> void sendError(FailedMovement<T> failedMovement){
        failedMovement.sendMessage(this.player, failedMovement.getValue().orElse(null));
    }
}
