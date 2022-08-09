package org.ships.vessel.sign;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.utils.Else;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.autopilot.scheduler.EOTExecutor;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;

import java.util.*;
import java.util.stream.Collectors;

public class EOTSign implements ShipsSign {

    private final Collection<Scheduler> eotScheduler = new HashSet<>();

    private final List<AText> SIGN = Arrays.asList(
            AText.ofPlain("[EOT]").withColour(NamedTextColours.YELLOW),
            AText.ofPlain("Ahead").withColour(NamedTextColours.GREEN),
            AText.ofPlain("Stop"));

    @Deprecated(forRemoval = true)
    public Collection<Scheduler> getScheduler(Vessel vessel) {
        return this.eotScheduler.stream().filter(e -> {
            Runnable runnable = e.getExecutor();
            if (!(runnable instanceof EOTExecutor exe)) {
                return false;
            }
            return exe.getVessel().equals(vessel);
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

    @Override
    public boolean onSecondClick(@NotNull LivePlayer player, SyncBlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (opTile.isEmpty()) {
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity)) {
            return false;
        }
        SignTileEntity stes = (SignTileEntity) lte;
        new ShipsUpdateBlockLoader(position).loadOvertime(vessel -> {
            if (stes.getTextAt(1).isPresent() && stes.getTextAt(1).get().toPlain().contains("{")) {
                stes.setText(this.SIGN);
                this.eotScheduler.stream().filter(e -> {
                    Runnable runnable = e.getExecutor();
                    if (!(runnable instanceof EOTExecutor eotExecutor)) {
                        return false;
                    }
                    return vessel.equals(eotExecutor.getVessel());
                }).forEach(Scheduler::cancel);
            } else {
                stes.setTextAt(1, AText.ofPlain("{Ahead}").withColour(NamedTextColours.GREEN));
                stes.setTextAt(2, AText.ofPlain("Stop"));
                Scheduler task = TranslateCore.getScheduleManager().schedule()
                        .setDisplayName("EOT: " + Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown"))
                        .setExecutor(new EOTExecutor(player, vessel))
                        .setIteration(ShipsPlugin.getPlugin().getConfig().getEOTDelay())
                        .setIterationUnit(ShipsPlugin.getPlugin().getConfig().getEOTDelayUnit())
                        .build(ShipsPlugin.getPlugin());
                task.run();
                this.eotScheduler.add(task);
            }
        }, ex -> player.sendMessage(AText
                .ofPlain("Could not find connected ship (" + ex.getMessage() + ")")
                .withColour(NamedTextColours.RED)));
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
