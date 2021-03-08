package org.ships.vessel.sign;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.text.Text;
import org.core.text.TextColours;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntitySnapshot;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.ships.movement.autopilot.scheduler.EOTExecutor;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.loader.ShipsUpdateBlockLoader;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EOTSign implements ShipsSign {

    private final Set<Scheduler> eot_scheduler = new HashSet<>();

    public Collection<Scheduler> getScheduler(Vessel vessel){
        return Collections.unmodifiableCollection(this.eot_scheduler.stream().filter(e -> {
            Runnable runnable = e.getExecutor();
            if(!(runnable instanceof EOTExecutor)){
                return false;
            }
            EOTExecutor exe = (EOTExecutor)runnable;
            return exe.getVessel().equals(vessel);
        }).collect(Collectors.toSet()));
    }

    public boolean isAhead(SignTileEntity entity){
        return entity.getLine(1).get().toPlain().startsWith("{");
    }

    @Override
    public boolean isSign(SignTileEntity entity) {
        Optional<Text> opText = entity.getLine(0);
        return opText.map(text -> text.toPlain().equals("[EOT]")).orElse(false);
    }

    @Override
    public SignTileEntitySnapshot changeInto(SignTileEntity sign) {
        SignTileEntitySnapshot stes = sign.getSnapshot();
        stes.setLine(0, CorePlugin.buildText(TextColours.YELLOW + "[EOT]"));
        stes.setLine(1, CorePlugin.buildText("Ahead"));
        stes.setLine(2, CorePlugin.buildText("{Stop}"));
        return stes;
    }

    @Override
    public Text getFirstLine() {
        return CorePlugin.buildText(TextColours.YELLOW + "[EOT]");
    }

    @Override
    public boolean onPrimaryClick(LivePlayer player, SyncBlockPosition position) {
        return false;
    }

    @Override
    public boolean onSecondClick(LivePlayer player, SyncBlockPosition position) {
        Optional<LiveTileEntity> opTile = position.getTileEntity();
        if(!opTile.isPresent()){
            return false;
        }
        LiveTileEntity lte = opTile.get();
        if(!(lte instanceof LiveSignTileEntity)){
            return false;
        }
        LiveSignTileEntity stes = (LiveSignTileEntity) lte;
        Vessel vessel;
        try {
            vessel = new ShipsUpdateBlockLoader(position).load();
        } catch (IOException e) {
            player.sendMessage(CorePlugin.buildText(TextColours.RED + "Could not find connected ship"));
            return false;
        }
        final Vessel vesselFinal = vessel;
        if(stes.getLine(1).get().toPlain().startsWith("{")) {
            stes.setLine(1, CorePlugin.buildText("Ahead"));
            stes.setLine(2, CorePlugin.buildText("{Stop}"));
            this.eot_scheduler.stream().filter(e -> {
                Runnable runnable = e.getExecutor();
                if(!(runnable instanceof EOTExecutor)){
                    return false;
                }
                EOTExecutor eotExecutor = (EOTExecutor) runnable;
                return vesselFinal.equals(eotExecutor.getVessel());
            }).forEach(Scheduler::cancel);
        }else{
            stes.setLine(1, CorePlugin.buildText("{Ahead}"));
            stes.setLine(2, CorePlugin.buildText("Stop"));
            Scheduler task = CorePlugin
                    .createSchedulerBuilder()
                    .setDisplayName("EOT: " + vessel.getName())
                    .setExecutor(new EOTExecutor(player, vessel))
                    .setIteration(ShipsPlugin.getPlugin().getConfig().getEOTDelay())
                    .setIterationUnit(ShipsPlugin.getPlugin().getConfig().getEOTDelayUnit())
                    .build(ShipsPlugin.getPlugin());
            task.run();
            this.eot_scheduler.add(task);
        }
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
