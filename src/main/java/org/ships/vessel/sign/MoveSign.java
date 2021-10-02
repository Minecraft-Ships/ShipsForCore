package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.adventureText.AText;
import org.core.adventureText.format.NamedTextColours;
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
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.configuration.ShipsConfig;
import org.ships.movement.MovementContext;
import org.ships.movement.result.FailedMovement;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsOvertimeBlockFinder;
import org.ships.vessel.common.types.Vessel;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MoveSign implements ShipsSign {

    public final List<AText> SIGN = Arrays.asList(
            AText.ofPlain("[Move]").withColour(NamedTextColours.YELLOW),
            AText.ofPlain(""),
            AText.ofPlain("Speed"),
            AText.ofPlain(1 + "")
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
        return CorePlugin.buildText(TextColours.YELLOW + "[Move]");
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
        LiveSignTileEntity lste = (LiveSignTileEntity) lte;
        String name = lste.getTextAt(3).get().toPlain();
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
        new ShipsOvertimeBlockFinder(position).loadOvertime(vessel -> {
            onSignSpeedUpdate(player, vessel, lste, finalSpeed);
            ShipsSign.LOCKED_SIGNS.remove(position);
        }, (pss) -> {

            player.sendMessage(CorePlugin.buildText(TextColours.RED + "Could not find [Ships] sign"));
            ShipsSign.LOCKED_SIGNS.remove(position);
            Collection<SyncBlockPosition> positions = pss.getPositions();
            positions.forEach(bp -> bp.setBlock(BlockTypes.BEDROCK.getDefaultBlockDetails(), player));
            CorePlugin
                    .createSchedulerBuilder()
                    .setDelay(5)
                    .setDelayUnit(TimeUnit.SECONDS)
                    .setExecutor(() -> positions.forEach(bp -> bp.resetBlock(player)))
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
        LiveSignTileEntity lste = (LiveSignTileEntity) lte;
        SignUtil.onMovement(position, player, (context, vessel, throwableConsumer) -> {
            int speed = lste.getTextAt(3).map(text -> Integer.parseInt(text.toPlain())).orElse(1);
            ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
            int trackLimit = config.getDefaultTrackSize();
            onVesselMove(player, position, speed, context, vessel, throwableConsumer);
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

    private void onSignSpeedUpdate(LivePlayer player, Vessel ship, LiveSignTileEntity lste, int finalSpeed) {
        int originalSpeed = 2;
        Optional<Text> opSpeed = lste.getLine(3);
        if (opSpeed.isPresent()) {
            try {
                originalSpeed = Integer.parseInt(opSpeed.get().toPlain());
            } catch (NumberFormatException ignored) {

            }
        }
        int max = ship.getMaxSpeed();
        if (finalSpeed > max && originalSpeed < finalSpeed) {
            player.sendMessage(CorePlugin.buildText(TextColours.RED + "Speed error: Your speed cannot go higher"));
        } else if (finalSpeed < -max && originalSpeed > finalSpeed) {
            player.sendMessage(CorePlugin.buildText(TextColours.RED + "Speed error: Your speed cannot go lower"));
        } else {
            lste.setLine(3, CorePlugin.buildText("" + finalSpeed));
        }
    }

    private void onVesselMove(LivePlayer player, SyncBlockPosition position, int speed, MovementContext context, Vessel vessel, Consumer<Throwable> throwableConsumer) {
        if (speed > vessel.getMaxSpeed() || speed < -vessel.getMaxSpeed()) {
            ShipsSign.LOCKED_SIGNS.remove(position);
            player.sendMessage(CorePlugin.buildText(TextColours.RED + "Speed error: Your ship cannot move that fast"));
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            return;
        }
        Optional<DirectionalData> opDirectional = position.getBlockDetails().getDirectionalData();
        if (!opDirectional.isPresent()) {
            ShipsSign.LOCKED_SIGNS.remove(position);
            player.sendMessage(CorePlugin.buildText(TextColours.RED + "Unknown error: " + position.getBlockType().getId() + " is not directional"));
            context.getBar().ifPresent(ServerBossBar::deregisterPlayers);
            return;
        }
        Vector3<Integer> direction = opDirectional.get().getDirection().getOpposite().getAsVector().multiply(speed);
        BasicMovement movement = ShipsPlugin.getPlugin().getConfig().getDefaultMovement();
        context.setMovement(movement);
        context.setClicked(position);
        vessel.moveTowards(direction, context, throwableConsumer);
    }

    private <T> void sendErrorMessage(CommandViewer viewer, FailedMovement<T> movement, Object value) {
        movement.sendMessage(viewer, (T) value);
    }
}
