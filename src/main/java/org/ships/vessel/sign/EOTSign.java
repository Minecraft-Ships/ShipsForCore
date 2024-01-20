package org.ships.vessel.sign;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.TranslateCore;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.schedule.unit.TimeUnit;
import org.core.source.Messageable;
import org.core.utils.ComponentUtils;
import org.core.utils.Else;
import org.core.vector.type.Vector3;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.NoLicencePresent;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.movement.autopilot.scheduler.EOTExecutor;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.EotFlag;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.loader.ShipsUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EOTSign implements ShipsSign {

    private final List<Component> SIGN = Arrays.asList(Component.text("[EOT]").color(NamedTextColor.YELLOW),
                                                       Component.text("Ahead").color(NamedTextColor.GREEN),
                                                       Component.text("{Stop}"));

    public Collection<Scheduler> getScheduler(Vessel vessel) {
        return TranslateCore.getScheduleManager().getSchedules().stream().filter(e -> {
            Consumer<Scheduler> consumer = e.getRunner();
            if (!(consumer instanceof EOTExecutor)) {
                return false;
            }
            EOTExecutor runner = (EOTExecutor) consumer;
            return runner.getVessel().equals(vessel);
        }).collect(Collectors.toUnmodifiableSet());
    }

    public boolean isAhead(SignTileEntity entity) {
        Optional<SignSide> opSide = this.getSide(entity);
        if (opSide.isEmpty()) {
            return false;
        }
        SignSide side = opSide.get();
        return side.getLineAt(1).map(ComponentUtils::toPlain).map(line -> line.contains("{")).orElse(false);
    }

    @Override
    public boolean isSign(List<? extends Component> lines) {
        return !lines.isEmpty() && ComponentUtils
                .toPlain(lines.get(0))
                .equalsIgnoreCase(ComponentUtils.toPlain(this.SIGN.get(0)));
    }

    @Override
    public void changeInto(@NotNull SignSide sign) throws IOException {
        sign.setLines(this.SIGN);
    }

    @Override
    public boolean onPrimaryClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        return false;
    }

    @Override
    public boolean onSecondClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (opTile.isEmpty()) {
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity)) {
            return false;
        }
        LiveSignTileEntity stes = (LiveSignTileEntity) lte;

        new ShipsUpdateBlockLoader(position).loadOvertime(this.onLoad(player, stes), this.onException(player));
        return true;
    }

    private Consumer<Vessel> onLoad(LivePlayer player, LiveSignTileEntity stes) {
        return (vessel) -> {
            Vector3<Integer> relative = stes.getPosition().getPosition().minus(vessel.getPosition().getPosition());
            VesselFlag<Vector3<Integer>> flag = new EotFlag(player.getUniqueId(), relative);
            vessel.set(flag);

            TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDelay(0)
                    .setDisplayName("Back to Synced")
                    .setRunner((thisSch) -> {
                        Optional<SignSide> opSide = this.getSide(stes);
                        if (opSide.isEmpty()) {
                            return;
                        }
                        SignSide side = opSide.get();
                        if (this.isAhead(stes)) {
                            side.setLineAt(1, Component.text("Ahead").color(NamedTextColor.GREEN));
                            side.setLineAt(2, Component.text("{Stop}"));
                            vessel.set(new EotFlag.Builder().buildEmpty());

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
                                    .forEach(Scheduler::cancel);
                            return;
                        }
                        side.setLineAt(1, Component.text("{Ahead}").color(NamedTextColor.GREEN));
                        side.setLineAt(2, Component.text("Stop"));

                        TranslateCore
                                .getScheduleManager()
                                .schedule()
                                .setDisplayName(
                                        "EOT: " + Else.throwOr(NoLicencePresent.class, vessel::getName, "Unknown"))
                                .setRunner(new EOTExecutor(vessel, player))
                                .setDelay(ShipsPlugin.getPlugin().getConfig().getEOTDelay())
                                .setDelayUnit(ShipsPlugin.getPlugin().getConfig().getEOTDelayUnit())
                                .buildDelayed(ShipsPlugin.getPlugin())
                                .run();
                    })
                    .setDelayUnit(TimeUnit.MINECRAFT_TICKS)
                    .buildDelayed(ShipsPlugin.getPlugin())
                    .run();
        };
    }

    private Consumer<LoadVesselException> onException(Messageable player) {
        return ex -> player.sendMessage(
                Component.text("Could not find connected ship (" + ex.getMessage() + ")").color(NamedTextColor.RED));
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
