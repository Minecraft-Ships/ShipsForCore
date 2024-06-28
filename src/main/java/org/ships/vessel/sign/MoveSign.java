package org.ships.vessel.sign;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.TranslateCore;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.source.Messageable;
import org.core.utils.ComponentUtils;
import org.core.utils.Else;
import org.core.vector.type.Vector3;
import org.core.world.direction.Direction;
import org.core.world.direction.SixteenFacingDirection;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.movement.instruction.details.MovementDetailsBuilder;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.finder.VesselBlockFinder;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MoveSign implements ShipsSign {

    public List<Component> getSignText() {
        return Arrays.asList(Component.text("[Move]").color(NamedTextColor.YELLOW), Component.empty(),
                             Component.text("Speed"),
                             Component.text(ShipsPlugin.getPlugin().getConfig().getDefaultMoveSpeed()));
    }

    @Override
    public boolean isSign(List<? extends Component> lines) {
        return !lines.isEmpty() && ComponentUtils
                .toPlain(lines.get(0))
                .equalsIgnoreCase(ComponentUtils.toPlain(this.getSignText().get(0)));
    }

    @Override
    public void changeInto(@NotNull SignSide sign) throws IOException {
        sign.setLines(this.getSignText());
    }

    @Override
    public boolean onPrimaryClick(@NotNull LivePlayer player, @NotNull SyncBlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (opTile.isEmpty()) {
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity)) {
            return false;
        }
        LiveSignTileEntity lste = (LiveSignTileEntity) lte;
        String defaultSpeed = ShipsPlugin.getPlugin().getConfig().getDefaultMoveSpeed() + "";
        String name = this
                .getSide(lste)
                .flatMap(side -> side.getLineAt(3))
                .map(ComponentUtils::toPlain)
                .orElse(defaultSpeed);
        if (name.isEmpty()) {
            name = defaultSpeed;
        }
        int speed = Integer.parseInt(name);
        if (player.isSneaking()) {
            speed--;
        } else {
            speed++;
        }
        final int finalSpeed = speed;
        ShipsPlugin.getPlugin().getLockedSignManager().lock(position);

        VesselBlockFinder.findOvertime(position).thenAccept(entry -> {
            if (entry.getValue().isPresent()) {
                this.onSignSpeedUpdate(player, entry.getValue().get(), lste, finalSpeed);
                ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
                return;
            }
            player.sendMessage(Component.text("Could not find [Ships] sign").color(NamedTextColor.RED));
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
            Collection<SyncBlockPosition> positions = entry
                    .getKey()
                    .getSyncPositionsRelativeToPosition(entry.getKey().getPosition())
                    .collect(Collectors.toList());
            positions.forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
            TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDelay(5)
                    .setDelayUnit(TimeUnit.SECONDS)
                    .setRunner((sch) -> positions.forEach(bp -> bp.resetBlock(player)))
                    .setDisplayName("Remove bedrock")
                    .buildDelayed(ShipsPlugin.getPlugin())
                    .run();
        });
        return true;
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
        SignTileEntity lste = (SignTileEntity) lte;
        SignUtil.onMovement(position, player, (details, vessel) -> {
            int speed = this
                    .getSide(lste)
                    .flatMap(side -> side.getLineAt(3))
                    .map(text -> Integer.parseInt(ComponentUtils.toPlain(text)))
                    .orElse(1);
            this.onVesselMove(player, position, speed, details.toBuilder(), vessel);
        });
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

    private void onSignSpeedUpdate(Messageable player, Vessel ship, SignTileEntity lste, int finalSpeed) {
        int originalSpeed = this
                .getSide(lste)
                .flatMap(side -> side.getLineAt(3))
                .stream()
                .flatMapToInt(com -> Else.throwOr(NumberFormatException.class, () -> {
                    int value = Integer.parseInt(ComponentUtils.toPlain(com));
                    return IntStream.of(value);
                }, IntStream.empty()))
                .findAny()
                .orElseGet(() -> ShipsPlugin.getPlugin().getConfig().getDefaultMoveSpeed());
        int max = ship.getMaxSpeed();
        if (finalSpeed > max && originalSpeed < finalSpeed) {
            player.sendMessage(Component.text("Speed error: Your speed cannot go higher").color(NamedTextColor.RED));
            return;
        }
        if (finalSpeed < -max && originalSpeed > finalSpeed) {
            player.sendMessage(Component.text("Speed error: Your speed cannot go lower").color(NamedTextColor.RED));
            return;
        }
        this.getSide(lste).ifPresent(sign -> sign.setLineAt(3, Component.text(finalSpeed)));

    }

    private void onVesselMove(Messageable player,
                              BlockPosition position,
                              int speed,
                              MovementDetailsBuilder builder,
                              Vessel vessel) {
        if (speed > vessel.getMaxSpeed() || speed < -vessel.getMaxSpeed()) {
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
            player.sendMessage(
                    Component.text("Speed error: Your ship cannot move that fast").color(NamedTextColor.RED));
            if (builder.getAdventureBossBar() != null) {
                position
                        .getWorld()
                        .getLiveEntities()
                        .filter(p -> p instanceof LivePlayer)
                        .map(p -> (LivePlayer) p)
                        .forEach(p -> p.hideBossBar(builder.getAdventureBossBar()));
            }
            return;
        }
        Optional<DirectionalData> opDirectional = position.getBlockDetails().getDirectionalData();
        if (opDirectional.isEmpty()) {
            ShipsPlugin.getPlugin().getLockedSignManager().unlock(position);
            player.sendMessage(Component
                                       .text("Unknown error: " + position.getBlockType().getId() + " is not "
                                                     + "directional")
                                       .color(NamedTextColor.RED));
            if (builder.getAdventureBossBar() != null) {
                position
                        .getWorld()
                        .getLiveEntities()
                        .filter(p -> p instanceof LivePlayer)
                        .map(p -> (LivePlayer) p)
                        .forEach(p -> p.hideBossBar(builder.getAdventureBossBar()));
            }
            return;
        }
        Direction originalDirection = opDirectional.get().getDirection();
        if (originalDirection instanceof SixteenFacingDirection) {
            SixteenFacingDirection sixteenFacingDir = (SixteenFacingDirection) originalDirection;
            originalDirection = sixteenFacingDir.normal();
        }
        Vector3<Integer> direction = originalDirection.getOpposite().getAsVector().multiply(speed);
        builder.setClickedBlock(position);
        vessel.moveTowards(direction, builder.build());
    }
}
