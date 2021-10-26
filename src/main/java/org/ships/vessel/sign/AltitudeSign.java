package org.ships.vessel.sign;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.EntitySnapshot;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.source.viewer.CommandViewer;
import org.core.text.Text;
import org.core.text.TextColours;
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
import org.ships.exceptions.MoveException;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.exceptions.load.UnableToFindLicenceSign;
import org.ships.movement.MovementContext;
import org.ships.movement.result.FailedMovement;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.AltitudeLockFlag;
import org.ships.vessel.common.loader.ShipsBlockFinder;
import org.ships.vessel.common.loader.ShipsOvertimeUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class AltitudeSign implements ShipsSign {

    public static final List<AText> SIGN = Arrays.asList(
            AText.ofPlain("[Altitude]").withColour(NamedTextColours.YELLOW),
            AText.ofPlain("{Increase}"),
            AText.ofPlain("decrease"),
            AText.ofPlain("1")
    );

    @Override
    public boolean isSign(List<AText> lines) {
        return lines.size() >= 1 && lines.get(0).equalsIgnoreCase(SIGN.get(0));
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setText(SIGN);
        return stes;
    }

    @Override
    @Deprecated
    public Text getFirstLine() {
        return TranslateCore.buildText(TextColours.YELLOW + "[Altitude]");
    }

    @Override
    public boolean onPrimaryClick(@NotNull LivePlayer player, SyncBlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (!opTile.isPresent()) {
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity)) {
            return false;
        }
        LiveSignTileEntity stes = (LiveSignTileEntity) lte;
        boolean updateSpeed = player.isSneaking();
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();

        if (updateSpeed) {
            int altitude = Integer.parseInt(stes.getTextAt(3).map(AText::toPlain).orElse("0"));
            if (config.isStructureAutoUpdating()) {
                new ShipsOvertimeUpdateBlockLoader(position) {
                    @Override
                    protected void onStructureUpdate(Vessel vessel) {
                        int newSpeed = altitude + 1;
                        if (newSpeed <= vessel.getAltitudeSpeed()) {
                            stes.setTextAt(3, AText.ofPlain(newSpeed + ""));
                        }
                        ShipsSign.LOCKED_SIGNS.remove(position);
                    }

                    @Override
                    protected OvertimeBlockFinderUpdate.BlockFindControl onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                        return OvertimeBlockFinderUpdate.BlockFindControl.USE;
                    }

                    @Override
                    protected void onExceptionThrown(LoadVesselException e) {
                        ShipsSign.LOCKED_SIGNS.remove(position);
                        if (e instanceof UnableToFindLicenceSign) {
                            UnableToFindLicenceSign e1 = (UnableToFindLicenceSign) e;
                            player.sendMessage(AText.ofPlain(e1.getReason()).withColour(NamedTextColours.RED));
                            e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
                            TranslateCore.createSchedulerBuilder().setDisplayName("Unable to find ships sign").setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
                        } else {
                            player.sendMessage(AText.ofPlain(e.getMessage()).withColour(NamedTextColours.RED));
                        }
                    }
                }.loadOvertime();
            } else {
                try {
                    Vessel vessel = new ShipsBlockFinder(position).load();
                    int newSpeed = altitude + 1;
                    if (newSpeed <= vessel.getAltitudeSpeed()) {
                        stes.setTextAt(3, AText.ofPlain("" + newSpeed));
                    }
                    ShipsSign.LOCKED_SIGNS.remove(position);
                    return false;
                } catch (UnableToFindLicenceSign e1) {
                    ShipsSign.LOCKED_SIGNS.remove(position);
                    player.sendMessage(AText.ofPlain(e1.getReason()).withColour(NamedTextColours.RED));
                    e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
                    TranslateCore.createSchedulerBuilder().setDelay(5).setDelayUnit(TimeUnit.SECONDS).setDisplayName("Unable to find ships sign").setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
                } catch (LoadVesselException e) {
                    ShipsSign.LOCKED_SIGNS.remove(position);
                    player.sendMessage(AText.ofPlain(e.getMessage()).withColour(NamedTextColours.RED));
                }
            }
            return false;
        }

        if (stes.getTextAt(1).isPresent() && stes.getTextAt(1).get().toPlain().contains("{")) {
            stes.setTextAt(1, AText.ofPlain("Increase"));
            stes.setTextAt(2, AText.ofPlain("{decrease}"));
        } else {
            stes.setTextAt(1, AText.ofPlain("{Increase}"));
            stes.setTextAt(2, AText.ofPlain("decrease"));
        }
        return false;
    }

    @Override
    public boolean onSecondClick(@NotNull LivePlayer player, SyncBlockPosition position) {
        Optional<LiveTileEntity> opTileEntity = position.getTileEntity();
        if (!opTileEntity.isPresent()) {
            return false;
        }
        SignTileEntity ste = (SignTileEntity) opTileEntity.get();
        Optional<String> opLine1 = ste.getTextAt(1).map(AText::toPlain);
        Optional<String> opLine3 = ste.getTextAt(3).map(AText::toPlain);
        if (!(opLine1.isPresent() && opLine3.isPresent())) {
            return false;
        }

        String line1 = opLine1.get();
        int altitude = 1;
        try {
            altitude = Integer.parseInt(opLine3.get());
        } catch (NumberFormatException ignored) {

        }
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
        ServerBossBar bar = null;
        if (config.isBossBarVisible()) {
            bar = TranslateCore.createBossBar().setTitle(AText.ofPlain("0 / " + blockLimit)).register(player);
        }
        final ServerBossBar finalBar = bar;
        final int finalAltitude = altitude;
        ShipsSign.LOCKED_SIGNS.add(position);
        if (config.isStructureAutoUpdating()) {
            new ShipsOvertimeUpdateBlockLoader(position) {
                @Override
                protected void onStructureUpdate(Vessel vessel) {
                    onVesselMove(player, position, finalBar, finalAltitude, line1, vessel);
                }

                @Override
                protected OvertimeBlockFinderUpdate.BlockFindControl onBlockFind(PositionableShipsStructure currentStructure, BlockPosition block) {
                    int foundBlocks = currentStructure.getPositions().size() + 1;
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
                    ShipsSign.LOCKED_SIGNS.remove(position);
                    if (finalBar != null) {
                        finalBar.deregisterPlayers();
                    }
                    if (e instanceof UnableToFindLicenceSign) {
                        UnableToFindLicenceSign e1 = (UnableToFindLicenceSign) e;
                        player.sendMessage(AText.ofPlain(e1.getReason()).withColour(NamedTextColours.RED));
                        e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
                        TranslateCore.createSchedulerBuilder().setDisplayName("Unable to find ships sign").setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
                    } else {
                        player.sendMessage(AText.ofPlain(e.getMessage()).withColour(NamedTextColours.RED));
                    }
                }
            }.loadOvertime();
        } else {
            try {
                Vessel vessel = new ShipsBlockFinder(position).load();
                onVesselMove(player, position, finalBar, altitude, line1, vessel);
            } catch (UnableToFindLicenceSign e1) {
                ShipsSign.LOCKED_SIGNS.remove(position);
                player.sendMessage(AText.ofPlain(e1.getReason()).withColour(NamedTextColours.RED));
                e1.getFoundStructure().getPositions().forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
                TranslateCore.createSchedulerBuilder().setDisplayName("Unable to find ships sign").setDelay(5).setDelayUnit(TimeUnit.SECONDS).setExecutor(() -> e1.getFoundStructure().getPositions().forEach(bp -> bp.resetBlock(player))).build(ShipsPlugin.getPlugin()).run();
            } catch (LoadVesselException e) {
                ShipsSign.LOCKED_SIGNS.remove(position);
                player.sendMessage(AText.ofPlain(e.getMessage()).withColour(NamedTextColours.RED));
            }
        }
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

    private <T> void sendErrorMessage(CommandViewer viewer, FailedMovement<T> movement, Object value) {
        movement.sendMessage(viewer, (T) value);
    }

    private void onVesselMove(LivePlayer player, BlockPosition position, ServerBossBar bar, int altitude, String line1, Vessel vessel) {
        Optional<Boolean> opFlag = vessel.getValue(AltitudeLockFlag.class);
        if (opFlag.isPresent() && bar != null) {
            if (opFlag.get()) {
                bar.deregisterPlayers();
                player.sendMessage(AText.ofPlain("The altitude is locked on this ship"));
                ShipsSign.LOCKED_SIGNS.remove(position);
                return;
            }
        }
        MovementContext context = new MovementContext().setMovement(ShipsPlugin.getPlugin().getConfig().getDefaultMovement()).setPostMovement((e) -> ShipsSign.LOCKED_SIGNS.remove(position));
        context.setClicked(position);
        vessel.getEntities().stream().filter(e -> e instanceof LivePlayer).forEach(e -> {
            if (bar == null) {
                return;
            }
            bar.register((LivePlayer) e);
            context.setBar(bar);
        });

        Consumer<Throwable> exception = (exc) -> {
            ShipsSign.LOCKED_SIGNS.remove(position);
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            context
                    .getEntities()
                    .keySet()
                    .stream()
                    .filter(e -> e instanceof EntitySnapshot.NoneDestructibleSnapshot)
                    .map(e -> (EntitySnapshot.NoneDestructibleSnapshot<?>) e)
                    .forEach(e -> e.setGravity(true));
            if (!(exc instanceof MoveException)) {
                return;
            }
            MoveException e = (MoveException) exc;
            FailedMovement<?> movement = e.getMovement();
            sendErrorMessage(player, movement, movement.getValue().orElse(null));
        };
        if (line1.startsWith("{")) {
            vessel.moveTowards(0, altitude, 0, context, exception);
        } else {
            vessel.moveTowards(0, -altitude, 0, context, exception);
        }
    }
}
