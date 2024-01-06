package org.ships.vessel.sign;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.TranslateCore;
import org.core.entity.EntitySnapshot;
import org.core.entity.LiveEntity;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.source.Messageable;
import org.core.utils.BarUtils;
import org.core.utils.ComponentUtils;
import org.core.utils.Else;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.finder.VesselBlockFinder;
import org.ships.vessel.common.flag.AltitudeLockFlag;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class AltitudeSign implements ShipsSign {

    public static final List<Component> SIGN;

    static {
        Component first = Component.text("[Altitude]").color(NamedTextColor.YELLOW);
        Component second = Component.text("{Increase}");
        Component third = Component.text("decrease");
        Component fourth = Component.text(1);

        SIGN = Arrays.asList(first, second, third, fourth);
    }

    @Override
    public boolean isSign(List<? extends Component> lines) {
        return lines.size() >= 1 && ComponentUtils
                .toPlain(lines.get(0))
                .equalsIgnoreCase(ComponentUtils.toPlain(SIGN.get(0)));
    }

    @Override
    public void changeInto(@NotNull SignSide sign) throws IOException {
        sign.setLines(SIGN);
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
        Optional<SignSide> opSignSide = this.getSide(stes);
        if (opSignSide.isEmpty()) {
            return false;
        }
        SignSide side = opSignSide.get();
        boolean updateSpeed = player.isSneaking();
        if (updateSpeed) {
            int altitude = Integer.parseInt(side.getLineAt(3).map(ComponentUtils::toPlain).orElse("0"));
            VesselBlockFinder.findOvertime(position).thenAccept(entry -> {
                ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
                Optional<Vessel> opVessel = entry.getValue();
                if (opVessel.isPresent()) {
                    Vessel vessel = opVessel.get();
                    int newSpeed = altitude + 1;
                    if (newSpeed <= vessel.getAltitudeSpeed()) {
                        side.setLineAt(3, Component.text(newSpeed));
                    }
                    return;
                }
                player.sendMessage(Component.text("could not find the licence sign").color(NamedTextColor.RED));
                entry
                        .getKey()
                        .getSyncedPositionsRelativeToWorld()
                        .forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
                TranslateCore
                        .getScheduleManager()
                        .schedule()
                        .setDisplayName("Unable to find ships sign")
                        .setDelay(5)
                        .setDelayUnit(TimeUnit.SECONDS)
                        .setRunner((sch) -> entry
                                .getKey()
                                .getSyncedPositionsRelativeToWorld()
                                .forEach(bp -> bp.resetBlock(player)))
                        .buildDelayed(ShipsPlugin.getPlugin())
                        .run();

            });
            return true;
        }

        Optional<Component> opSecondLine = side.getLineAt(1);


        if (opSecondLine.isPresent() && ComponentUtils.toPlain(opSecondLine.get()).contains("{")) {
            side.setLineAt(1, Component.text("Increase"));
            side.setLineAt(2, Component.text("{decrease}"));
            return true;
        }
        side.setLineAt(1, Component.text("{Increase}"));
        side.setLineAt(2, Component.text("decrease"));
        return true;
    }

    @Override
    public boolean onSecondClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        Optional<LiveTileEntity> opTileEntity = position.getTileEntity();
        if (opTileEntity.isEmpty()) {
            return false;
        }
        SignTileEntity ste = (SignTileEntity) opTileEntity.get();
        Optional<SignSide> opSide = this.getSide(ste);
        if (opSide.isEmpty()) {
            return false;
        }
        SignSide side = opSide.get();
        Optional<String> opLine1 = side.getLineAt(1).map(ComponentUtils::toPlain);
        Optional<String> opLine3 = side.getLineAt(3).map(ComponentUtils::toPlain);
        if (!(opLine1.isPresent() && opLine3.isPresent())) {
            return false;
        }

        String line1 = opLine1.get();
        int altitude = Else.throwOr(NumberFormatException.class, () -> Integer.parseInt(opLine3.get()), 1);
        boolean updateSpeed = player.isSneaking();
        if (updateSpeed) {
            int newSpeed = altitude - 1;
            if (newSpeed >= 0) {
                side.setLineAt(3, Component.text(newSpeed));
            }
            return true;
        }
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        int blockLimit = config.getDefaultTrackSize();
        BossBar bossBar = null;
        if (config.isBossBarVisible()) {
            bossBar = BossBar.bossBar(Component.text("0 / " + blockLimit), 0, BossBar.Color.PURPLE,
                                      BossBar.Overlay.PROGRESS);
            player.showBossBar(bossBar);
        }
        final BossBar finalBar = bossBar;
        final int finalAltitude = altitude;
        ShipsPlugin.getPlugin().getLockedSignManager().lock(position);

        VesselBlockFinder.findOvertime(position, (currentStructure, block) -> {
            int foundBlocks = currentStructure.getOriginalRelativePositionsToCenter().size() + 1;
            int newTotal = Math.max(blockLimit, foundBlocks);
            if (finalBar != null) {
                finalBar.name(Component.text(foundBlocks + "/" + newTotal));

                float progress = newTotal / (float) foundBlocks;
                progress = progress / 100;
                finalBar.progress(progress);
            }
        }).thenAccept(entry -> {
            if (entry.getValue().isPresent()) {
                AltitudeSign.this.onVesselMove(player, position, finalBar, finalAltitude, line1,
                                               entry.getValue().get());
                return;
            }
            Collection<? extends SyncBlockPosition> foundStructure = entry.getKey().getSyncedPositionsRelativeToWorld();
            foundStructure.forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
            TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDelay(5)
                    .setDelayUnit(TimeUnit.SECONDS)
                    .setRunner((sched) -> foundStructure.forEach(bp -> bp.resetBlock(player)))
                    .buildDelayed(ShipsPlugin.getPlugin())
                    .run();
        });

        return true;
    }

    @Override
    public String getId() {
        return "ships:altitude_sign";
    }

    @Override
    public String getName() {
        return "Altitude Sign";
    }

    private void onVesselMove(Messageable player,
                              BlockPosition position,
                              @Nullable BossBar bossBar,
                              int altitude,
                              String line1,
                              Vessel vessel) {
        Optional<Boolean> opFlag = vessel.getValue(AltitudeLockFlag.class);
        if (opFlag.isPresent() && bossBar != null) {
            if (opFlag.get()) {
                BarUtils.getPlayers(bossBar).forEach(user -> user.hideBossBar(bossBar));
                player.sendMessage(Component.text("The altitude is locked on this ship"));
                ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
                return;
            }
        }
        MovementDetailsBuilder builder = new MovementDetailsBuilder();
        builder.setAdventureBossBar(bossBar);
        builder.setClickedBlock(position);
        if (bossBar != null) {
            vessel.getEntitiesOvertime(user -> user instanceof Audience).thenAccept(entities -> {
                for (LiveEntity entity : entities) {
                    ((Audience) entity).showBossBar(bossBar);
                }
            });
        }

        BiConsumer<MovementContext, Throwable> exception = (context, exc) -> {
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
            context
                    .getAdventureBossBar()
                    .ifPresent(bar -> BarUtils.getPlayers(bar).forEach(user -> user.hideBossBar(bar)));
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
            player.sendMessage(e.getErrorMessage());
        };

        builder.setException(exception);
        builder.setUpdatingStructure(true);
        if (line1.startsWith("{")) {
            vessel.moveTowards(0, altitude, 0, builder.build());
        } else {
            vessel.moveTowards(0, -altitude, 0, builder.build());
        }
    }
}
