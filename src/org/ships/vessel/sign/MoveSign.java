package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.source.viewer.CommandViewer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.vector.types.Vector3Int;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.exceptions.MoveException;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.movement.result.FailedMovement;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.loader.ShipsOvertimeUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MoveSign implements ShipsSign {

    @Override
    public boolean isSign(SignTileEntity entity) {
        Optional<Text> opValue = entity.getLine(0);
        if (opValue.isPresent() && opValue.get().equals(getFirstLine())) {
            return true;
        }
        return false;
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setLine(0, CorePlugin.buildText(TextColours.YELLOW + "[Move]"));
        stes.setLine(1, CorePlugin.buildText("{Engine}"));
        return stes;
    }

    @Override
    public Text getFirstLine() {
        return CorePlugin.buildText(TextColours.YELLOW + "[Move]");
    }

    @Override
    public boolean onPrimaryClick(LivePlayer player, BlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (!opTile.isPresent()) {
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity)) {
            return false;
        }
        LiveSignTileEntity lste = (LiveSignTileEntity) lte;
        String name = lste.getLine(3).get().toPlain();
        if (name.length() == 0) {
            name = "1";
        }
        int speed = Integer.parseInt(name);
        if (player.isSneaking()) {
            speed--;
        } else {
            speed++;
        }
        final int finalSpeed = speed;
        new ShipsOvertimeUpdateBlockLoader(position) {
            @Override
            protected void onStructureUpdate(Vessel ship) {
                if (finalSpeed > ship.getMaxSpeed() || finalSpeed < -ship.getMaxSpeed()) {
                    return;
                }
                lste.setLine(3, CorePlugin.buildText("" + finalSpeed));
            }

            @Override
            protected boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                return true;
            }

            @Override
            protected void onExceptionThrown(LoadVesselException e) {
                if (e instanceof UnableToFindLicenceSign) {
                    UnableToFindLicenceSign e1 = (UnableToFindLicenceSign) e;
                    player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getReason()));
                    e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
                    CorePlugin.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
                } else {
                    player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
                }
            }
        }.loadOvertime();
        return false;
    }

    @Override
    public boolean onSecondClick(LivePlayer player, BlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (!opTile.isPresent()) {
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity)) {
            return false;
        }
        LiveSignTileEntity lste = (LiveSignTileEntity) lte;
        String name = lste.getLine(3).get().toPlain();
        if (name.length() == 0) {
            name = "1";
        }
        int speed = Integer.parseInt(name);
        ServerBossBar bar = CorePlugin.createBossBar();
        bar.register(player);
        bar.setMessage(CorePlugin.buildText("Finding structure: 0"));
        new ShipsOvertimeUpdateBlockLoader(position) {
            @Override
            protected void onStructureUpdate(Vessel vessel) {
                bar.deregister(player);
                vessel.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> bar.register((LivePlayer) e));
                Optional<DirectionalData> opDirectional = position.getBlockDetails().getDirectionalData();
                if (!opDirectional.isPresent()) {
                    player.sendMessage(CorePlugin.buildText(TextColours.RED + "Unknown error: " + position.getBlockType().getId() + " is not directional"));
                    return;
                }
                if (vessel instanceof CrewStoredVessel) {
                    CrewStoredVessel stored = (CrewStoredVessel) vessel;
                    if (!(stored.getPermission(player.getUniqueId()).canMove() || player.hasPermission(Permissions.getMovePermission(stored.getType())) || player.hasPermission(Permissions.getOtherMovePermission(stored.getType())))) {
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + "Missing permission"));
                        return;
                    }
                }
                Vector3Int direction = opDirectional.get().getDirection().getOpposite().getAsVector().multiply(speed);
                BasicMovement movement = ShipsPlugin.getPlugin().getConfig().getDefaultMovement();
                try {
                    vessel.moveTowards(direction, movement, bar);
                } catch (MoveException e) {
                    sendErrorMessage(player, e.getMovement(), e.getMovement().getValue().orElse(null));
                } catch (Throwable e2) {
                    vessel.getEntities().forEach(e -> e.setGravity(true));
                }
            }

            @Override
            protected boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                bar.setMessage(CorePlugin.buildText("Finding structure: " + (currentStructure.getPositions().size() + 1)));
                return true;
            }

            @Override
            protected void onExceptionThrown(LoadVesselException e) {
                bar.deregisterPlayers();
                if (e instanceof UnableToFindLicenceSign) {
                    UnableToFindLicenceSign e1 = (UnableToFindLicenceSign) e;
                    player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getReason()));
                    e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
                    CorePlugin.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
                } else {
                    player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
                }
            }
        }.loadOvertime();
        return false;
    }

    @Override
    public String getId() {
        return "ships:move_sign";
    }

    @Override
    public String getName() {
        return "Move sign";
    }

    private <T> void sendErrorMessage(CommandViewer viewer, FailedMovement<T> movement, Object value) {
        movement.sendMessage(viewer, (T) value);
    }
}
