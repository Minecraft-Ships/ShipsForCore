package org.ships.vessel.common.types.typical;

import org.core.CorePlugin;
import org.core.config.ConfigurationStream;
import org.core.utils.Identifable;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.event.vessel.VesselStructureUpdate;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.loader.shipsvessel.ShipsFileLoader;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractShipsVessel implements ShipsVessel {

    protected PositionableShipsStructure positionableShipsStructure;
    protected Map<UUID, CrewPermission> crewsPermission = new HashMap<>();
    protected Set<VesselFlag<?>> flags = new HashSet<>(Collections.singletonList(new MovingFlag()));
    protected CrewPermission defaultPermission = CrewPermission.DEFAULT;
    protected Map<String, SyncExactPosition> teleportPositions = new HashMap<>();
    protected File file;
    protected ExpandedBlockList blockList;
    protected ShipType<? extends AbstractShipsVessel> type;
    protected int maxSpeed = 10;
    protected int altitudeSpeed = 2;
    protected boolean isLoading = true;

    public AbstractShipsVessel(LiveSignTileEntity licence, ShipType<? extends AbstractShipsVessel> type){
        this.positionableShipsStructure = new AbstractPosititionableShipsStructure(licence.getPosition());
        this.file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/" + getType().getId().replaceAll(":", ".") + "/" + getName() + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]);
        init(type);
    }

    public AbstractShipsVessel(SignTileEntity ste, SyncBlockPosition position, ShipType<? extends AbstractShipsVessel> type){
        this.positionableShipsStructure = new AbstractPosititionableShipsStructure(position);
        this.file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/" + ShipsPlugin.getPlugin().getAll(ShipType.class).stream().filter(t -> ste.getLine(1).get().equalsPlain(t.getDisplayName(), true)).findFirst().get().getId().replaceAll(":", ".") + "/" + ste.getLine(2).get().toPlain() + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]);
        init(type);
    }

    private void init(ShipType<? extends AbstractShipsVessel> type){
        ConfigurationStream.ConfigurationFile configuration = CorePlugin.createConfigurationFile(this.file, CorePlugin.getPlatform().getConfigFormat());
        this.blockList = new ExpandedBlockList(configuration, type.getDefaultBlockList());
        this.file = configuration.getFile();
        this.type = type;
        this.maxSpeed = this.type.getDefaultMaxSpeed();
        this.altitudeSpeed = this.type.getDefaultAltitudeSpeed();
    }

    @Override
    public boolean isLoading(){
        return this.isLoading;
    }

    @Override
    public void setLoading(boolean check){
        this.isLoading = check;
    }

    @Override
    public Collection<VesselFlag<?>> getFlags(){
        return Collections.unmodifiableCollection(this.flags);
    }

    @Override
    public <T extends VesselFlag<?>> Optional<T> get(Class<T> clazz){
        return (Optional<T>) getFlags().stream().filter(clazz::isInstance).findAny();
    }

    @Override
    public <T> Vessel set(Class<? extends VesselFlag<T>> clazz, T value){
        Optional<VesselFlag<?>> opFlag = getFlags().stream().filter(clazz::isInstance).findFirst();
        if(!opFlag.isPresent()){
            Optional<? extends VesselFlag<T>> opNewFlag = ShipsPlugin.getPlugin().get(clazz);
            if(!opNewFlag.isPresent()){
                System.err.println("Class of " + clazz.getName() + " is not registered in ShipsPlugin. Failing to set for " + getId());
                return this;
            }
            VesselFlag<T> flag = opNewFlag.get();
            flag.setValue(value);
            this.flags.add(flag);
            return this;
        }
        ((VesselFlag<T>)opFlag.get()).setValue(value);
        return this;
    }

    @Override
    public Vessel set(VesselFlag<?> flag){
        Set<VesselFlag<?>> collect = this.flags.stream().filter(f -> {
            String fID = f.getId();
            String flagID = flag.getId();
            return fID.equals(flagID);
        }).collect(Collectors.toSet());
        collect.forEach(f -> this.flags.remove(f));
        this.flags.add(flag);
        return this;
    }

    @Override
    public Map<String, SyncExactPosition> getTeleportPositions(){
        return this.teleportPositions;
    }

    @Override
    public ShipType<? extends AbstractShipsVessel> getType(){
        return type;
    }

    @Override
    public File getFile(){
        return this.file;
    }

    @Override
    public void save(){
        ShipsFileLoader fl = new ShipsFileLoader(this.getFile());
        fl.save(this);
    }

    @Override
    public ExpandedBlockList getBlockList() {
        return this.blockList;
    }

    @Override
    public PositionableShipsStructure getStructure() {
        return this.positionableShipsStructure;
    }

    @Override
    public void setStructure(PositionableShipsStructure pss){
        if (CorePlugin.getPlatform().callEvent(new VesselStructureUpdate(pss, this)).isCancelled()){
            return;
        }
        this.positionableShipsStructure = pss;
    }

    @Override
    public int getMaxSpeed() {
        return this.maxSpeed;
    }

    @Override
    public int getAltitudeSpeed() {
        return this.altitudeSpeed;
    }

    @Override
    public Vessel setMaxSpeed(int speed) {
        this.maxSpeed = speed;
        return this;
    }

    @Override
    public Vessel setAltitudeSpeed(int speed) {
        this.altitudeSpeed = speed;
        return this;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof Identifable)){
            return false;
        }
        return ((Identifable) obj).getId().equals(this.getId());
    }

    @Override
    public Map<UUID, CrewPermission> getCrew() {
        return this.crewsPermission;
    }

    @Override
    public CrewPermission getDefaultPermission() {
        return this.defaultPermission;
    }
}
