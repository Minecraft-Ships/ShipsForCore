package org.ships.vessel.sign;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.schedule.unit.TimeUnit;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Else;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.NoLicencePresent;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.movement.autopilot.scheduler.EOTExecutor;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EOTSign implements ShipsSign {

    private final List<AText> SIGN = Arrays.asList(
            AText.ofPlain("[EOT]").withColour(NamedTextColours.YELLOW),
            AText.ofPlain("Ahead").withColour(NamedTextColours.GREEN),
            AText.ofPlain("{Stop}"));

    public Collection<Scheduler> getScheduler(Vessel vessel) {
        return TranslateCore.getScheduleManager().getSchedules().stream().filter(e -> {
            Consumer<Scheduler> consumer = e.getRunner();
            if (!(consumer instanceof EOTExecutor runner)) {
                return false;
            }
            return runner.getVessel().equals(vessel);
        }).collect(Collectors.toUnmodifiableSet());
    }

    public boolean isAhead(SignTileEntity entity) {
        return entity.getTextAt(1).isPresent() && entity.getTextAt(1).get().contains(AText.ofPlain("{"));
    }

    @Override
    public boolean isSign(List<? extends AText> lines) {
        return lines.size() >= 1 && lines.get(0).equalsIgnoreCase(this.SIGN.get(0));
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setText(this.SIGN);
        return stes;
    }

    @Override
    public boolean onPrimaryClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        return false;
    }

    private Consumer<Vessel> onLoad(LivePlayer player, LiveSignTileEntity stes) {
        return (vessel) -> {
            TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDelay(0)
                    .setDisplayName("Back to Synced")
                    .setRunner((thisSch) -> {
                        if (this.isAhead(stes)) {
                            stes.setTextAt(1, AText.ofPlain("Ahead"));
                            stes.setTextAt(2, AText.ofPlain("{Stop}"));

                            this
                                    .getScheduler(vessel)
                                    .parallelStream()
                                    .filter(sch -> sch.getRunner() instanceof EOTExecutor)
                                    .filter(sch -> ((EOTExecutor) sch.getRunner()).getSign().isPresent())
                                    .filter(sch -> ((EOTExecutor) sch.getRunner())
                                            .getSign()
                                            .get()
                                            .getPosition()
                                            .equals(stes.getPosition()))
                                    .forEach(sch -> {
                                        if (sch instanceof Scheduler.Native nati) {
                                            nati.cancel();
                                        }
                                    });
                            return;
                        }
                        stes.setTextAt(1, AText.ofPlain("{Ahead}").withColour(NamedTextColours.GREEN));
                        stes.setTextAt(2, AText.ofPlain("Stop"));

                        TranslateCore.getScheduleManager().schedule()
                                .setDisplayName(
                                        "EOT: " + Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown"))
                                .setRunner(new EOTExecutor(vessel, player))
                                .setIteration(ShipsPlugin.getPlugin().getConfig().getEOTDelay())
                                .setIterationUnit(ShipsPlugin.getPlugin().getConfig().getEOTDelayUnit())
                                .build(ShipsPlugin.getPlugin()).run();
                    }).setDelayUnit(TimeUnit.MINECRAFT_TICKS).build(ShipsPlugin.getPlugin()).run();
        };
    }

    private Consumer<LoadVesselException> onException(CommandViewer player) {
        return ex -> player.sendMessage(AText
                .ofPlain("Could not find connected ship (" + ex.getMessage() + ")")
                .withColour(NamedTextColours.RED));
    }

    @Override
    public boolean onSecondClick(@NotNull LivePlayer player, SyncBlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (opTile.isEmpty()) {
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity stes)) {
            return false;
        }
        new ShipsUpdateBlockLoader(position).loadOvertime(this.onLoad(player, stes), this.onException(player));
        return false;
    }

    @Override
    public String getId() {
        return "ships:eot_sign";
    }

    @Override
    public String getName() {
        return "EOT Sign";
    }
}
