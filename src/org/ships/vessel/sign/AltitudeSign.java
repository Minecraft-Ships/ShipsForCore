package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.viewer.CommandViewer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.exceptions.MoveException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.movement.result.FailedMovement;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.AltitudeLockFlag;
import org.ships.vessel.common.loader.shipsvessel.ShipsUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AltitudeSign implements ShipsSign {

    @Override
    public boolean isSign(SignTileEntity entity) {
        Optional<Text> opValue = entity.getLine(0);
        return opValue.isPresent() && opValue.get().equals(getFirstLine());
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setLine(0, CorePlugin.buildText(TextColours.YELLOW + "[Altitude]"));
        stes.setLine(1, CorePlugin.buildText("{Increase}"));
        stes.setLine(2, CorePlugin.buildText("decrease"));
        stes.setLine(3, CorePlugin.buildText("1"));
        return stes;
    }

    @Override
    public Text getFirstLine() {
        return CorePlugin.buildText(TextColours.YELLOW + "[Altitude]");
    }

    @Override
    public boolean onPrimaryClick(LivePlayer player, BlockPosition position) {
        ShipsPlugin.getPlugin().getDebugFile().addMessage("--[Start of AltitudeSign:onPrimaryClick(LivePlayer, BlockPosition)]--");
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if(!opTile.isPresent()){
            ShipsPlugin.getPlugin().getDebugFile().addMessage("Returned false due to Position not being a Tile Entity", "--[End of AltitudeSign:onPrimaryClick(LivePlayer, BlockPosition)]--");
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if(!(lte instanceof LiveSignTileEntity)){
            ShipsPlugin.getPlugin().getDebugFile().addMessage("Returned false due to Position not being a Live Sign Tile Entity", "--[End of AltitudeSign:onPrimaryClick(LivePlayer, BlockPosition)]--");
            return false;
        }
        LiveSignTileEntity stes = (LiveSignTileEntity) lte;
        if(stes.getLine(1).get().toPlain().startsWith("{")) {
            stes.setLine(1, CorePlugin.buildText("Increase"));
            stes.setLine(2, CorePlugin.buildText("{decrease}"));
        }else{
            stes.setLine(1, CorePlugin.buildText("{Increase}"));
            stes.setLine(2, CorePlugin.buildText("decrease"));
        }
        ShipsPlugin.getPlugin().getDebugFile().addMessage("--[End of AltitudeSign:onPrimaryClick(LivePlayer, BlockPosition)]--");
        return false;
    }

    @Override
    public boolean onSecondClick(LivePlayer player, BlockPosition position) {
        Optional<LiveTileEntity> opTileEntity = position.getTileEntity();
        SignTileEntity ste = (SignTileEntity) opTileEntity.get();
        String line1 = ste.getLine(1).get().toPlain();
        String line3 = ste.getLine(3).get().toPlain();
        int altitude = Integer.parseInt(line3);
        try {
            Vessel vessel = new ShipsUpdateBlockLoader(position).load();
            Optional<Boolean> opFlag = vessel.getValue(AltitudeLockFlag.class);
            if (opFlag.isPresent()) {
                if (opFlag.get()) {
                    player.sendMessage(CorePlugin.buildText("The altitude is locked on this ship"));
                    return false;
                }
            }
            try {
                if (line1.startsWith("{")) {
                    vessel.moveTowards(0, altitude, 0, ShipsPlugin.getPlugin().getConfig().getDefaultMovement());
                } else {
                    vessel.moveTowards(0, -altitude, 0, ShipsPlugin.getPlugin().getConfig().getDefaultMovement());
                }
            }catch (MoveException e){
                FailedMovement<?> movement = e.getMovement();
                sendErrorMessage(player, movement, movement.getValue().orElse(null));
                ShipsPlugin.getPlugin().getDebugFile().addMessage("Returned due to " + movement.getResult().getClass().getSimpleName(), "--[End of AltitudeSign:onSecondClick(LivePlayer, BlockPosition)]--");

            }
        }catch (UnableToFindLicenceSign e1){
            player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getReason()));
            e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
            CorePlugin.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();

        } catch (IOException e) {
            ShipsPlugin.getPlugin().getDebugFile().addMessage("Returned due to " + e.getMessage(), "--[End of AltitudeSign:onSecondClick(LivePlayer, BlockPosition)]--");
            player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
            return false;
        }
        ShipsPlugin.getPlugin().getDebugFile().addMessage("--[End of AltitudeSign:onSecondClick(LivePlayer, BlockPosition)]--");
        return false;
    }

    @Override
    public String getId() {
        return "ships:altitude_sign";
    }

    @Override
    public String getName() {
        return "Altitude Sign";
    }

    private <T> void sendErrorMessage(CommandViewer viewer, FailedMovement<T> movement, Object value){
        movement.sendMessage(viewer, (T)value);
    }
}
