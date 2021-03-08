package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.source.viewer.CommandViewer;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.movement.MovementContext;
import org.ships.movement.result.FailedMovement;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.CrewStoredVessel;
import org.ships.vessel.common.loader.ShipsBlockFinder;
import org.ships.vessel.common.loader.ShipsOvertimeUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;

public class MoveSign implements ShipsSign {

    @Override
    public boolean isSign(SignTileEntity entity) {
        Optional<Text> opValue = entity.getLine(0);
        return opValue.isPresent() && opValue.get().equals(getFirstLine());
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setLine(0, CorePlugin.buildText(TextColours.YELLOW + "[Move]"));
        stes.setLine(1, CorePlugin.buildText("{Engine}"));
        return stes;
    }

    @Override
    @Deprecated
    public Text getFirstLine() {
        return CorePlugin.buildText(TextColours.YELLOW + "[Move]");
    }

    @Override
    public boolean onPrimaryClick(LivePlayer player, SyncBlockPosition position) {
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
        ShipsSign.LOCKED_SIGNS.add(position);
        if (ShipsPlugin.getPlugin().getConfig().isStructureAutoUpdating()) {
            LicenceSign licenceSign = ShipsPlugin.getPlugin().get(LicenceSign.class).get();
            new ShipsOvertimeUpdateBlockLoader(position) {
                @Override
                protected void onStructureUpdate(Vessel vessel) {
                    onSignSpeedUpdate(vessel, lste, finalSpeed);
                    ShipsSign.LOCKED_SIGNS.remove(position);
                }

                @Override
                protected boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                    return true;
                }

                @Override
                protected void onExceptionThrown(LoadVesselException e) {
                    ShipsSign.LOCKED_SIGNS.remove(position);
                    if (e instanceof UnableToFindLicenceSign) {
                        UnableToFindLicenceSign e1 = (UnableToFindLicenceSign) e;
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getReason()));
                        e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
                        CorePlugin.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setDisplayName("Unable to find ships sign").setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
                    } else {
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
                    }
                }
            }.loadOvertime();
        } else {
            try {
                Vessel vessel = new ShipsBlockFinder(position).load();
                onSignSpeedUpdate(vessel, lste, finalSpeed);
                ShipsSign.LOCKED_SIGNS.remove(position);
            } catch (UnableToFindLicenceSign e1) {
                ShipsSign.LOCKED_SIGNS.remove(position);
                player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getReason()));
                e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
                CorePlugin.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
            } catch (LoadVesselException e) {
                ShipsSign.LOCKED_SIGNS.remove(position);
                player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
            }
        }
        return true;
    }

    @Override
    public boolean onSecondClick(LivePlayer player, SyncBlockPosition position) {
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
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        int trackLimit = config.getDefaultTrackSize();
        int speed = Integer.parseInt(name);
        MovementContext context = new MovementContext();
        context.setPostMovement(e -> ShipsSign.LOCKED_SIGNS.remove(position));
        if (config.isBossBarVisible()) {
            ServerBossBar bar = CorePlugin.createBossBar();
            bar.register(player);
            bar.setMessage(CorePlugin.buildText("0 / " + trackLimit));
            context.setBar(bar);
        }
        ShipsSign.LOCKED_SIGNS.add(position);
        if (config.isStructureAutoUpdating()) {
            new ShipsOvertimeUpdateBlockLoader(position) {
                @Override
                protected void onStructureUpdate(Vessel vessel) {
                    onVesselMove(player, position, speed, context, vessel);
                }

                @Override
                protected boolean onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                    int foundBlocks = currentStructure.getPositions().size() + 1;
                    context.getBar().ifPresent(bar -> {
                        bar.setMessage(CorePlugin.buildText(foundBlocks + " / " + trackLimit));
                        try {
                            bar.setValue(foundBlocks, trackLimit);
                        } catch (IllegalArgumentException ignore) {

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
                        CorePlugin.createSchedulerBuilder().setDisplayName("UnableToFindLicenceSign reverse").setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
                    } else {
                        player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
                    }
                }
            }.loadOvertime();
        } else {
            try {
                Vessel vessel = new ShipsBlockFinder(position).load();
                onVesselMove(player, position, speed, context, vessel);
            } catch (UnableToFindLicenceSign e1) {
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                ShipsSign.LOCKED_SIGNS.remove(position);
                player.sendMessage(CorePlugin.buildText(TextColours.RED + e1.getReason()));
                e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.get().getDefaultBlockDetails(), player));
                CorePlugin.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
            } catch (LoadVesselException e) {
                ShipsSign.LOCKED_SIGNS.remove(position);
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                player.sendMessage(CorePlugin.buildText(TextColours.RED + e.getMessage()));
            }
        }
        return true;
    }

    @Override
    public String getId() {
        return "ships:move_sign";
    }

    @Override
    public String getName() {
        return "Move sign";
    }

    private void onSignSpeedUpdate(Vessel ship, LiveSignTileEntity lste, int finalSpeed) {
        int max = ship.getMaxSpeed();
        if(finalSpeed > max){
            lste.setLine(3, CorePlugin.buildText("" + finalSpeed));
        }else if (finalSpeed < -max) {
            lste.setLine(3, CorePlugin.buildText("" + finalSpeed));
        }else {
            lste.setLine(3, CorePlugin.buildText("" + finalSpeed));
        }
    }

    private void onVesselMove(LivePlayer player, SyncBlockPosition position, int speed, MovementContext context, Vessel vessel) {
        Optional<DirectionalData> opDirectional = position.getBlockDetails().getDirectionalData();
        if (!opDirectional.isPresent()) {
            ShipsSign.LOCKED_SIGNS.remove(position);
            player.sendMessage(CorePlugin.buildText(TextColours.RED + "Unknown error: " + position.getBlockType().getId() + " is not directional"));
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            return;
        }
        if (vessel instanceof CrewStoredVessel) {
            CrewStoredVessel stored = (CrewStoredVessel) vessel;
            if (!(stored.getPermission(player.getUniqueId()).canMove() || player.hasPermission(Permissions.getMovePermission(stored.getType())) || player.hasPermission(Permissions.getOtherMovePermission(stored.getType())))) {
                player.sendMessage(CorePlugin.buildText(TextColours.RED + "Missing permission"));
                ShipsSign.LOCKED_SIGNS.remove(position);
                context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
                return;
            }
        }
        Vector3<Integer> direction = opDirectional.get().getDirection().getOpposite().getAsVector().multiply(speed);
        BasicMovement movement = ShipsPlugin.getPlugin().getConfig().getDefaultMovement();
        context.setMovement(movement);
        context.setClicked(position);
        vessel.moveTowards(direction, context, exc -> {
            ShipsSign.LOCKED_SIGNS.remove(position);
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            if (exc instanceof MoveException) {
                MoveException e = (MoveException) exc;
                sendErrorMessage(player, e.getMovement(), e.getMovement().getValue().orElse(null));
            } else {
                exc.printStackTrace();
            }
            context.getEntities().keySet().forEach(s -> {
                if (s instanceof EntitySnapshot.NoneDestructibleSnapshot) {
                    ((EntitySnapshot.NoneDestructibleSnapshot<? extends LiveEntity>) s).getEntity().setGravity(true);
                }
            });

        });
    }

    private <T> void sendErrorMessage(CommandViewer viewer, FailedMovement<T> movement, Object value) {
        movement.sendMessage(viewer, (T) value);
    }
}
