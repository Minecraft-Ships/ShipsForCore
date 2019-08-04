package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.viewer.CommandViewer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.exceptions.MoveException;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.movement.result.FailedMovement;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsOvertimeUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class WheelSign implements ShipsSign {

    @Override
    public boolean isSign(SignTileEntity entity) {
        Optional<Text> opValue = entity.getLine(0);
        return opValue.isPresent() && opValue.get().equals(getFirstLine());
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setLine(0, CorePlugin.buildText(TextColours.YELLOW + "[Wheel]"));
        stes.setLine(1, CorePlugin.buildText(TextColours.RED + "\\\\||//"));
        stes.setLine(2, CorePlugin.buildText(TextColours.RED + "==||=="));
        stes.setLine(3, CorePlugin.buildText(TextColours.RED + "//||\\\\"));
        return stes;
    }

    @Override
    public Text getFirstLine() {
        return CorePlugin.buildText(TextColours.YELLOW + "[Wheel]");
    }

    @Override
    public boolean onPrimaryClick(LivePlayer player, BlockPosition position){
        if(player.isSneaking()){
            return false;
        }
        ServerBossBar bar = CorePlugin.createBossBar();
        bar.setMessage(CorePlugin.buildText("Finding structure: 0")).register(player);
            new ShipsOvertimeUpdateBlockLoader(position) {
                @Override
                protected void onStructureUpdate(Vessel vessel) {
                    bar.deregister(player);
                    vessel.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> bar.register((LivePlayer) e));
                    BasicMovement movement = ShipsPlugin.getPlugin().getConfig().getDefaultMovement();
                    try{
                        vessel.rotateLeftAround(vessel.getPosition(), movement, bar);
                    }catch (MoveException e){
                        sendErrorMessage(player, e.getMovement(), e.getMovement().getValue().orElse(null));
                    }catch (Throwable e2){
                        vessel.getEntities().forEach(e -> e.setGravity(true));
                    }
                }

                @Override
                protected boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                    return true;
                }

                @Override
                protected void onExceptionThrown(LoadVesselException e) {
                    bar.deregisterPlayers();
                    if(e instanceof UnableToFindLicenceSign){
                        UnableToFindLicenceSign e1 = (UnableToFindLicenceSign)e;
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getReason()));
                        e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
                        CorePlugin.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
                    }else{
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getReason()));
                    }
                }
            }.loadOvertime();
        return false;
    }

    @Override
    public boolean onSecondClick(LivePlayer player, BlockPosition position) {
        if(player.isSneaking()){
            return false;
        }
        ServerBossBar bar = CorePlugin.createBossBar();
        bar.setMessage(CorePlugin.buildText("Finding structure: 0")).register(player);
        new ShipsOvertimeUpdateBlockLoader(position) {
            @Override
            protected void onStructureUpdate(Vessel vessel) {
                bar.deregister(player);
                vessel.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> bar.register((LivePlayer) e));
                BasicMovement movement = ShipsPlugin.getPlugin().getConfig().getDefaultMovement();
                try{
                    vessel.rotateRightAround(vessel.getPosition(), movement, bar);
                }catch (MoveException e){
                    sendErrorMessage(player, e.getMovement(), e.getMovement().getValue().orElse(null));
                }catch (Throwable e2){
                    vessel.getEntities().forEach(e -> e.setGravity(true));
                }
            }

            @Override
            protected boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                return true;
            }

            @Override
            protected void onExceptionThrown(LoadVesselException e) {
                bar.deregisterPlayers();
                if(e instanceof UnableToFindLicenceSign){
                    UnableToFindLicenceSign e1 = (UnableToFindLicenceSign)e;
                    player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getReason()));
                    e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
                    CorePlugin.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
                }else{
                    player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getReason()));
                }
            }
        }.loadOvertime();
        return false;
    }

    @Override
    public String getId() {
        return "ships:wheel_sign";
    }

    @Override
    public String getName() {
        return "Wheel Sign";
    }

    private <T> void sendErrorMessage(CommandViewer viewer, FailedMovement<T> movement, Object value){
        movement.sendMessage(viewer, (T)value);
    }
}
