package org.ships.vessel.sign;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.EntitySnapshot;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Else;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.blockfinder.OvertimeBlockFinderUpdate;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.AltitudeLockFlag;
import org.ships.vessel.common.loader.ShipsOvertimeUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class AltitudeSign implements ShipsSign {

    public static final List<AText> SIGN = Arrays.asList(
            AText.ofPlain("[Altitude]").withColour(NamedTextColours.YELLOW), AText.ofPlain("{Increase}"),
            AText.ofPlain("decrease"), AText.ofPlain("1"));

    @Override
    public boolean isSign(List<? extends AText> lines) {
        return lines.size() >= 1 && lines.get(0).equalsIgnoreCase(SIGN.get(0));
    }

    @Override
    public SignTileEntitySnapshot changeInto(@NotNull SignTileEntity sign) {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setText(SIGN);
        return stes;
    }

    @Override
    public boolean onPrimaryClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (opTile.isEmpty()) {
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity stes)) {
            return false;
        }
        boolean updateSpeed = player.isSneaking();
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();

        if (updateSpeed) {
            int altitude = Integer.parseInt(stes.getTextAt(3).map(AText::toPlain).orElse("0"));
            new ShipsOvertimeUpdateBlockLoader(position, config.isStructureAutoUpdating()) {
                @Override
                protected void onStructureUpdate(Vessel vessel) {
                    int newSpeed = altitude + 1;
                    if (newSpeed <= vessel.getAltitudeSpeed()) {
                        stes.setTextAt(3, AText.ofPlain(newSpeed + ""));
                    }
                    ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
                }

                @Override
                protected OvertimeBlockFinderUpdate.BlockFindControl onBlockFind(PositionableShipsStructure currentStructure,
                                                                                 BlockPosition block) {
                    return OvertimeBlockFinderUpdate.BlockFindControl.USE;
                }

                @Override
                protected void onExceptionThrown(LoadVesselException e) {
                    ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
                    if (!(e instanceof UnableToFindLicenceSign e1)) {
                        player.sendMessage(AText.ofPlain(e.getMessage()).withColour(NamedTextColours.RED));
                        return;
                    }
                    player.sendMessage(AText.ofPlain(e1.getReason()).withColour(NamedTextColours.RED));
                    e1
                            .getFoundStructure()
                            .getSyncedPositions()
                            .forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
                    TranslateCore
                            .getScheduleManager()
                            .schedule()
                            .setDisplayName("Unable to find ships sign")
                            .setDelay(5)
                            .setDelayUnit(TimeUnit.SECONDS)
                            .setRunner((sch) -> e1
                                    .getFoundStructure()
                                    .getSyncedPositions()
                                    .forEach(bp -> bp.resetBlock(player)))
                            .build(ShipsPlugin.getPlugin())
                            .run();
                }
            }.loadOvertime();
            return false;
        }

        if (stes.getTextAt(1).isPresent() && stes.getTextAt(1).get().toPlain().contains("{")) {
            stes.setTextAt(1, AText.ofPlain("Increase"));
            stes.setTextAt(2, AText.ofPlain("{decrease}"));
            return false;
        }
        stes.setTextAt(1, AText.ofPlain("{Increase}"));
        stes.setTextAt(2, AText.ofPlain("decrease"));
        return false;
    }

    @Override
    public boolean onSecondClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        Optional<LiveTileEntity> opTileEntity = position.getTileEntity();
        if (opTileEntity.isEmpty()) {
            return false;
        }
        SignTileEntity ste = (SignTileEntity) opTileEntity.get();
        Optional<String> opLine1 = ste.getTextAt(1).map(AText::toPlain);
        Optional<String> opLine3 = ste.getTextAt(3).map(AText::toPlain);
        if (!(opLine1.isPresent() && opLine3.isPresent())) {
            return false;
        }

        String line1 = opLine1.get();
        int altitude = Else.throwOr(NumberFormatException.class, () -> Integer.parseInt(opLine3.get()), 1);
        boolean updateSpeed = player.isSneaking();
        if (updateSpeed) {
            int newSpeed = altitude - 1;
            if (newSpeed >= 0) {
                ste.setTextAt(3, AText.ofPlain(newSpeed + ""));
            }
            return false;
        }
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        int blockLimit = config.getDefaultTrackSize();
        ServerBossBar bossBar = null;
        if (config.isBossBarVisible()) {
            bossBar = TranslateCore.createBossBar().setTitle(AText.ofPlain("0 / " + blockLimit)).register(player);
        }
        final ServerBossBar finalBar = bossBar;
        final int finalAltitude = altitude;
        ShipsPlugin.getPlugin().getLockedSignManager().lock(position);
        new ShipsOvertimeUpdateBlockLoader(position, config.isStructureAutoUpdating()) {
            @Override
            protected void onStructureUpdate(Vessel vessel) {
                AltitudeSign.this.onVesselMove(player, position, finalBar, finalAltitude, line1, vessel);
            }

            @Override
            protected OvertimeBlockFinderUpdate.BlockFindControl onBlockFind(PositionableShipsStructure currentStructure,
                                                                             BlockPosition block) {
                int foundBlocks = currentStructure.getRelativePositions().size() + 1;
                if (finalBar != null) {
                    finalBar.setTitle(AText.ofPlain(foundBlocks + " / " + blockLimit));
                    try {
                        finalBar.setValue(foundBlocks, blockLimit);
                    } catch (IllegalArgumentException ignore) {

                    }
                }
                return OvertimeBlockFinderUpdate.BlockFindControl.USE;
            }

            @Override
            protected void onExceptionThrown(LoadVesselException e) {
                ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
                if (finalBar != null) {
                    finalBar.deregisterPlayers();
                }
                if (e instanceof UnableToFindLicenceSign e1) {
                    player.sendMessage(AText.ofPlain(e1.getReason()).withColour(NamedTextColours.RED));
                    e1
                            .getFoundStructure()
                            .getSyncedPositions()
                            .forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
                    TranslateCore
                            .getScheduleManager()
                            .schedule()
                            .setDisplayName("Unable to find ships sign")
                            .setDelay(5)
                            .setDelayUnit(TimeUnit.SECONDS)
                            .setRunner((scheduler) -> e1
                                    .getFoundStructure()
                                    .getSyncedPositions()
                                    .forEach(bp -> bp.resetBlock(player)))
                            .build(ShipsPlugin.getPlugin())
                            .run();
                } else {
                    player.sendMessage(AText.ofPlain(e.getMessage()).withColour(NamedTextColours.RED));
                }
            }
        }.loadOvertime();

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

    private void onVesselMove(CommandViewer player,
                              BlockPosition position,
                              ServerBossBar bossBar,
                              int altitude,
                              String line1,
                              Vessel vessel) {
        Optional<Boolean> opFlag = vessel.getValue(AltitudeLockFlag.class);
        if (opFlag.isPresent() && bossBar != null) {
            if (opFlag.get()) {
                bossBar.deregisterPlayers();
                player.sendMessage(AText.ofPlain("The altitude is locked on this ship"));
                ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
                return;
            }
        }
        MovementDetailsBuilder builder = new MovementDetailsBuilder();
        builder.setBossBar(bossBar);
        builder.setClickedBlock(position);
        vessel.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> {
            if (bossBar == null) {
                return;
            }
            bossBar.register((LivePlayer) e);
        });

        BiConsumer<MovementContext, Throwable> exception = (context, exc) -> {
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
            context.getBossBar().ifPresent(ServerBossBar::deregisterPlayers);
            context
                    .getEntities()
                    .keySet()
                    .stream()
                    .filter(e -> e instanceof EntitySnapshot.NoneDestructibleSnapshot)
                    .map(e -> (EntitySnapshot.NoneDestructibleSnapshot<?>) e)
                    .forEach(e -> e.setGravity(true));
            if (!(exc instanceof MoveException e)) {
                return;
            }
            player.sendMessage(e.getErrorMessageText());
        };

        builder.setException(exception);
        if (line1.startsWith("{")) {
            vessel.moveTowards(0, altitude, 0, builder.build());
        } else {
            vessel.moveTowards(0, -altitude, 0, builder.build());
        }
    }
}
