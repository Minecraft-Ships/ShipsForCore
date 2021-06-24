package org.ships.vessel.common.types.typical;

import org.core.CorePlugin;
import org.core.config.ConfigurationStream;
import org.core.vector.type.Vector3;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.ExactPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.event.vessel.VesselStructureUpdate;
import org.ships.exceptions.NoLicencePresent;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.assits.TeleportToVessel;
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

    protected @NotNull PositionableShipsStructure positionableShipsStructure;
    protected @NotNull Map<UUID, CrewPermission> crewsPermission = new HashMap<>();
    protected @NotNull Set<VesselFlag<?>> flags = new HashSet<>(Collections.singletonList(new MovingFlag()));
    protected @NotNull CrewPermission defaultPermission = CrewPermission.DEFAULT;
    protected @NotNull Map<String, Vector3<Double>> teleportPositions = new HashMap<>();
    protected @NotNull File file;
    protected @NotNull ExpandedBlockList blockList;
    protected @NotNull ShipType<? extends AbstractShipsVessel> type;
    protected int maxSpeed = 10;
    protected int altitudeSpeed = 2;
    protected @Nullable Integer maxSize;
    protected @Nullable Integer minSize;
    protected boolean isLoading = true;

    public AbstractShipsVessel(@NotNull LiveSignTileEntity licence, @NotNull ShipType<? extends AbstractShipsVessel> type) throws NoLicencePresent {
        this.positionableShipsStructure = new AbstractPosititionableShipsStructure(licence.getPosition());
        this.file = new File(ShipsPlugin.getPlugin().getShipsConigFolder(), "VesselData/" + getType().getId().replaceAll(":", ".") + "/" + getName() + "." + CorePlugin.getPlatform().getConfigFormat().getFileType()[0]);
        init(type);
    }

    public AbstractShipsVessel(@NotNull SignTileEntity ste, @NotNull SyncBlockPosition position, @NotNull ShipType<? extends AbstractShipsVessel> type) {
        this.positionableShipsStructure = new AbstractPosititionableShipsStructure(position);
        this.file = new File(
                ShipsPlugin.getPlugin().getShipsConigFolder(),
                "VesselData/" + ShipsPlugin
                        .getPlugin()
                        .getAll(ShipType.class)
                        .stream()
                        .filter(t -> ste.getTextAt(1)
                                .orElseThrow(() -> new IllegalStateException("Could not get line 1 of sign"))
                                .toPlain().equalsIgnoreCase(t.getDisplayName()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Could not find the shiptype"))
                        .getId()
                        .replaceAll(":", ".")
                        + "/"
                        + ste.getTextAt(2)
                        .orElseThrow(() -> new IllegalArgumentException("Could not get name of ship"))
                        .toPlain()
                        + "."
                        + CorePlugin
                        .getPlatform()
                        .getConfigFormat()
                        .getFileType()[0]);
        init(type);
    }

    private void init(ShipType<? extends AbstractShipsVessel> type) {
        ConfigurationStream.ConfigurationFile configuration = CorePlugin.createConfigurationFile(this.file, CorePlugin.getPlatform().getConfigFormat());
        this.blockList = new ExpandedBlockList(configuration, type.getDefaultBlockList());
        this.file = configuration.getFile();
        this.type = type;
        this.maxSpeed = this.type.getDefaultMaxSpeed();
        this.altitudeSpeed = this.type.getDefaultAltitudeSpeed();
    }

    @Override
    public boolean isLoading() {
        return this.isLoading;
    }

    @Override
    public void setLoading(boolean check) {
        this.isLoading = check;
    }

    @Override
    public @NotNull Collection<VesselFlag<?>> getFlags() {
        return Collections.unmodifiableCollection(this.flags);
    }

    @Override
    public <T extends VesselFlag<?>> @NotNull Optional<T> get(@NotNull Class<T> clazz) {
        return getFlags().stream().filter(clazz::isInstance).map(v -> (T) v).findAny();
    }

    @Override
    public <T> @NotNull Vessel set(@NotNull Class<? extends VesselFlag<T>> clazz, T value) {
        Optional<VesselFlag<?>> opFlag = getFlags().stream().filter(clazz::isInstance).findFirst();
        if (!opFlag.isPresent()) {
            Optional<? extends VesselFlag<T>> opNewFlag = ShipsPlugin.getPlugin().get(clazz);
            if (!opNewFlag.isPresent()) {
                try {
                    System.err.println("Class of " + clazz.getName() + " is not registered in ShipsPlugin. Failing to set for " + getId());
                } catch (NoLicencePresent noLicencePresent) {
                    System.err.println("Class of " + clazz.getName() + " is not registered in ShipsPlugin. Failing to set for Unknown");
                }
                return this;
            }
            VesselFlag<T> flag = opNewFlag.get();
            flag.setValue(value);
            this.flags.add(flag);
            return this;
        }
        ((VesselFlag<T>) opFlag.get()).setValue(value);
        return this;
    }

    @Override
    public @NotNull Vessel set(@NotNull VesselFlag<?> flag) {
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
    public Map<String, Vector3<Double>> getTeleportVectors() {
        return this.teleportPositions;
    }

    @Override
    public TeleportToVessel setTeleportVector(Vector3<Double> position, String id) {
        this.teleportPositions.put(id, position);
        return this;
    }

    @Override
    public TeleportToVessel setTeleportPosition(ExactPosition tel, String id) {
        ExactPosition position = this.getPosition().toExactPosition();
        Optional<DirectionalData> opDirectionalData = position.getBlockDetails().getDirectionalData();
        if (!opDirectionalData.isPresent()) {
            return this;
        }
        Direction direction = opDirectionalData.get().getDirection();

        double x = tel.getX() - position.getX();
        double y = tel.getY() - position.getY();
        double z = tel.getZ() - position.getZ();

        Vector3<Double> vector = flip(direction, x, y, z);

        if (this.teleportPositions.containsKey(id)) {
            this.teleportPositions.replace(id, vector);
            return this;
        }
        this.teleportPositions.put(id, vector);
        return this;
    }

    private Vector3<Double> flip(Direction direction, double x, double y, double z) {
        if (direction.equals(FourFacingDirection.SOUTH)) {
            x = -x;
            z = -z;
        }
        if (direction.equals(FourFacingDirection.EAST)) {
            double temp = x;
            x = -z;
            z = temp;
        }
        if (direction.equals(FourFacingDirection.WEST)) {
            double temp = x;
            x = z;
            z = -temp;
        }
        return Vector3.valueOf(x, y, z);
    }

    @Override
    public @NotNull Map<String, ExactPosition> getTeleportPositions() {
        ExactPosition position = this.getPosition().toExactPosition();
        Optional<DirectionalData> opDirectionalData = position.getBlockDetails().getDirectionalData();
        if (!opDirectionalData.isPresent()) {
            return new HashMap<>();
        }
        Direction direction = opDirectionalData.get().getDirection();
        return this
                .teleportPositions
                .entrySet()
                .stream()
                .map((entry) -> {
                    double x = entry.getValue().getX();
                    double y = entry.getValue().getY();
                    double z = entry.getValue().getZ();

                    Vector3<Double> vec = flip(direction, x, y, z);
                    return new AbstractMap.SimpleImmutableEntry<>(
                            entry.getKey(),
                            position.getRelative(vec.getX(), vec.getY(), vec.getZ()));
                })
                .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
    }

    @Override
    public @NotNull ShipType<? extends AbstractShipsVessel> getType() {
        return type;
    }

    @Override
    public @NotNull File getFile() {
        return this.file;
    }

    @Override
    public void save() {
        ShipsFileLoader fl = new ShipsFileLoader(this.getFile());
        fl.save(this);
    }

    @Override
    public @NotNull ExpandedBlockList getBlockList() {
        return this.blockList;
    }

    @Override
    public @NotNull PositionableShipsStructure getStructure() {
        return this.positionableShipsStructure;
    }

    @Override
    public void setStructure(@NotNull PositionableShipsStructure pss) {
        if (CorePlugin.getPlatform().callEvent(new VesselStructureUpdate(pss, this)).isCancelled()) {
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
    public @NotNull Vessel setMaxSpeed(int speed) {
        this.maxSpeed = speed;
        return this;
    }

    @Override
    public @NotNull Vessel setAltitudeSpeed(int speed) {
        this.altitudeSpeed = speed;
        return this;
    }

    @Override
    public Optional<Integer> getMaxSize() {
        return (this.maxSize == null) ? this.getType().getDefaultMaxSize() : Optional.of(this.maxSize);
    }

    @Override
    public int getMinSize() {
        return (this.minSize == null) ? this.getType().getDefaultMinSize() : this.minSize;
    }

    @Override
    public @NotNull Vessel setMaxSize(Integer size) {
        this.maxSize = size;
        return this;
    }

    @Override
    public @NotNull Vessel setMinSize(Integer size) {
        this.minSize = size;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IdentifiableShip)) {
            return false;
        }
        try {
            return this.getId().equals(((IdentifiableShip) obj).getId());
        } catch (NoLicencePresent e) {
            return false;
        }
    }

    @Override
    public Map<UUID, CrewPermission> getCrew() {
        return this.crewsPermission;
    }

    @Override
    public @NotNull CrewPermission getDefaultPermission() {
        return this.defaultPermission;
    }
}
