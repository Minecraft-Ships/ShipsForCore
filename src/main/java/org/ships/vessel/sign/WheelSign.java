package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.source.viewer.CommandViewer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.movement.MovementContext;
import org.ships.movement.result.FailedMovement;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsBlockFinder;
import org.ships.vessel.common.loader.ShipsOvertimeUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;
import java.util.function.Consumer;

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
    public boolean onPrimaryClick(LivePlayer player, SyncBlockPosition position){
        return onClick(player, position, true);
    }

    @Override
    public boolean onSecondClick(LivePlayer player, SyncBlockPosition position) {
        return onClick(player, position, false);
    }

    @Override
    public String getId() {
        return "ships:wheel_sign";
    }

    @Override
    public String getName() {
        return "Wheel Sign";
    }

    private boolean onClick(LivePlayer player, SyncBlockPosition position, boolean left){
        if(player.isSneaking()){
            return false;
        }
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        int trackLimit = config.getDefaultTrackSize();
        MovementContext context = new MovementContext();
        context.setPostMovement(e -> ShipsSign.LOCKED_SIGNS.remove(position));
        if(ShipsPlugin.getPlugin().getConfig().isBossBarVisible()) {
            ServerBossBar bar = CorePlugin.createBossBar();
            bar.setMessage(CorePlugin.buildText("0 / " + trackLimit)).register(player);
            context.setBar(bar);
        }
        ShipsSign.LOCKED_SIGNS.add(position);
        if(config.isStructureAutoUpdating()) {
            new ShipsOvertimeUpdateBlockLoader(position) {
                @Override
                protected void onStructureUpdate(Vessel vessel) {
                    onVesselRotate(player, context, vessel, position, left);
                }

                @Override
                protected boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                    int foundBlocks = currentStructure.getPositions().size() + 1;
                    context.getBar().ifPresent(bar -> {
                        bar.setMessage(CorePlugin.buildText(foundBlocks + " / " + trackLimit));
                        try {
                            bar.setValue(foundBlocks, trackLimit);
                        }catch (IllegalArgumentException ignore){

                        }

                    });
                    return true;
                }

                @Override
                protected void onExceptionThrown(LoadVesselException e) {
                    ShipsSign.LOCKED_SIGNS.remove(position);
                    context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                    if (e instanceof UnableToFindLicenceSign) {
                        UnableToFindLicenceSign e1 = (UnableToFindLicenceSign) e;
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getReason()));
                        e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
                        CorePlugin.createSchedulerBuilder().setDelay(5).setDisplayName("Wheel bedrock reset").setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
                    } else {
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getReason()));
                    }
                }
            }.loadOvertime();
        }else{
            try {
                Vessel vessel = new ShipsBlockFinder(position).load();
                onVesselRotate(player, context, vessel, position, true);
            }catch (UnableToFindLicenceSign e1){
                player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getReason()));
                e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
                CorePlugin.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
            } catch (LoadVesselException e) {
                player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
            }
        }
        return false;
    }

    private void onVesselRotate(LivePlayer player, MovementContext context, Vessel vessel, SyncBlockPosition position, boolean left){
        context.getBar().ifPresent(bar -> {
            bar.deregister(player);
            vessel.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> bar.register((LivePlayer) e));

        });
        context.setMovement(ShipsPlugin.getPlugin().getConfig().getDefaultMovement());
        context.setClicked(position);
        Consumer<Throwable> exception = (exc) -> {
            ShipsSign.LOCKED_SIGNS.remove(position);
            if(exc instanceof MoveException){
                MoveException e = (MoveException)exc;
                sendErrorMessage(player, e.getMovement(), e.getMovement().getValue().orElse(null));
            }else{
                vessel.getEntities().forEach(e -> e.setGravity(true));
            }
        };
        if(left) {
            vessel.rotateLeftAround(vessel.getPosition(), context, exception);
        }else{
            vessel.rotateRightAround(vessel.getPosition(), context, exception);
        }
    }

    private <T> void sendErrorMessage(CommandViewer viewer, FailedMovement<T> movement, Object value){
        movement.sendMessage(viewer, (T)value);
    }
}
