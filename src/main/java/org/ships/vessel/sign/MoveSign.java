package org.ships.vessel.sign;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.unit.TimeUnit;
import org.core.source.viewer.CommandViewer;
import org.core.vector.type.Vector3;
import org.core.world.boss.ServerBossBar;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.movement.MovementContext;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsOvertimeBlockFinder;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class MoveSign implements ShipsSign {

    public List<AText> getSignText() {
        return Arrays.asList(
                AText.ofPlain("[Move]").withColour(NamedTextColours.YELLOW),
                AText.ofPlain(""),
                AText.ofPlain("Speed"),
                AText.ofPlain(ShipsPlugin.getPlugin().getConfig().getDefaultMoveSpeed() + "")
        );
    }

    @Override
    public boolean isSign(List<? extends AText> lines) {
        return lines.size() >= 1 && lines.get(0).equalsIgnoreCase(this.getSignText().get(0));
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setText(this.getSignText());
        return stes;
    }

    @Override
    public boolean onPrimaryClick(@NotNull LivePlayer player, SyncBlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (opTile.isEmpty()) {
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity lste)) {
            return false;
        }
        String defaultSpeed = ShipsPlugin
                .getPlugin()
                .getConfig()
                .getDefaultMoveSpeed() + "";
        String name =
                lste
                        .getTextAt(3)
                        .map(AText::toPlain)
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
        ShipsSign.LOCKED_SIGNS.add(position);
        new ShipsOvertimeBlockFinder(position).loadOvertime(vessel -> {
            this.onSignSpeedUpdate(player, vessel, lste, finalSpeed);
            ShipsSign.LOCKED_SIGNS.remove(position);
        }, (pss) -> {
            player.sendMessage(AText.ofPlain("Could not find [Ships] sign").withColour(NamedTextColours.RED));
            ShipsSign.LOCKED_SIGNS.remove(position);
            Collection<SyncBlockPosition> positions = pss
                    .getPositions((Function<? super SyncBlockPosition, ? extends SyncBlockPosition>) s -> s);
            positions.forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
            TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDelay(5)
                    .setDelayUnit(TimeUnit.SECONDS)
                    .setRunner((sch) -> positions.forEach(bp -> bp.resetBlock(player)))
                    .setDisplayName("Remove bedrock")
                    .build(ShipsPlugin.getPlugin()).run();
        });
        return true;
    }

    @Override
    public boolean onSecondClick(@NotNull LivePlayer player, SyncBlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if (!opTile.isPresent()) {
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if (!(lte instanceof LiveSignTileEntity)) {
            return false;
        }
        SignTileEntity lste = (SignTileEntity) lte;
        SignUtil.onMovement(position, player, (context, vessel, throwableConsumer) -> {
            int speed = lste.getTextAt(3).map(text -> Integer.parseInt(text.toPlain())).orElse(1);
            this.onVesselMove(player, position, speed, context, vessel, throwableConsumer);
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

    private void onSignSpeedUpdate(CommandViewer player, Vessel ship, SignTileEntity lste, int finalSpeed) {
        int originalSpeed = ShipsPlugin.getPlugin().getConfig().getDefaultMoveSpeed();
        Optional<AText> opSpeed = lste.getTextAt(3);
        if (opSpeed.isPresent()) {
            try {
                originalSpeed = Integer.parseInt(opSpeed.get().toPlain());
            } catch (NumberFormatException ignored) {

            }
        }
        int max = ship.getMaxSpeed();
        if (finalSpeed > max && originalSpeed < finalSpeed) {
            player.sendMessage(AText.ofPlain("Speed error: Your speed cannot go higher").withColour(NamedTextColours.RED));
        } else if (finalSpeed < -max && originalSpeed > finalSpeed) {
            player.sendMessage(AText.ofPlain("Speed error: Your speed cannot go lower").withColour(NamedTextColours.RED));
        } else {
            lste.setTextAt(3, AText.ofPlain(String.valueOf(finalSpeed)));
        }
    }

    private void onVesselMove(CommandViewer player, SyncBlockPosition position, int speed, MovementContext context, Vessel vessel, Consumer<Throwable> throwableConsumer) {
        if (speed > vessel.getMaxSpeed() || speed < -vessel.getMaxSpeed()) {
            ShipsSign.LOCKED_SIGNS.remove(position);
            player.sendMessage(AText.ofPlain("Speed error: Your ship cannot move that fast").withColour(NamedTextColours.RED));
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            return;
        }
        Optional<DirectionalData> opDirectional = position.getBlockDetails().getDirectionalData();
        if (!opDirectional.isPresent()) {
            ShipsSign.LOCKED_SIGNS.remove(position);
            player.sendMessage(AText.ofPlain("Unknown error: " + position.getBlockType().getId() + " is not " +
                    "directional").withColour(NamedTextColours.RED));
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            return;
        }
        Vector3<Integer> direction = opDirectional.get().getDirection().getOpposite().getAsVector().multiply(speed);
        BasicMovement movement = ShipsPlugin.getPlugin().getConfig().getDefaultMovement();
        context.setMovement(movement);
        context.setClicked(position);
        vessel.moveTowards(direction, context, throwableConsumer);
    }
}
