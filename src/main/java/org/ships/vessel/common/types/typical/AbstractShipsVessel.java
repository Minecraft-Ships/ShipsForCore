package org.ships.vessel.common.types.typical;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.config.ConfigurationStream;
import org.core.vector.type.Vector3;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.block.details.data.DirectionalData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.ExactPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.event.vessel.VesselStructureUpdate;
import org.ships.exceptions.NoLicencePresent;
import org.ships.permissions.vessel.CrewPermission;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.IdentifiableShip;
import org.ships.vessel.common.assits.TeleportToVessel;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.assits.shiptype.SizedShipType;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.loader.shipsvessel.ShipsFileLoader;
import org.ships.vessel.common.requirement.Requirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPositionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractShipsVessel implements ShipsVessel {

    protected final @NotNull Map<UUID, CrewPermission> crewsPermission = new HashMap<>();
    protected final @NotNull Set<VesselFlag<?>> flags = new HashSet<>(Collections.singletonList(new MovingFlag()));
    protected final @NotNull CrewPermission defaultPermission = CrewPermission.DEFAULT;
    protected final @NotNull Map<String, Vector3<Double>> teleportPositions = new HashMap<>();
    protected @NotNull PositionableShipsStructure positionableShipsStructure;
    protected @NotNull File file;
    protected @NotNull ShipType<? extends AbstractShipsVessel> type;
    protected @Nullable Integer maxSpeed;
    protected @Nullable Integer altitudeSpeed;
    protected boolean isLoading = true;
    protected String cachedName;

    public AbstractShipsVessel(@NotNull LiveTileEntity licence, @NotNull ShipType<? extends AbstractShipsVessel> type)
            throws NoLicencePresent {
        this.positionableShipsStructure = new AbstractPositionableShipsStructure(licence.getPosition());
        this.file = new File(ShipsPlugin.getPlugin().getConfigFolder(),
                             "VesselData/" + this.getType().getId().replaceAll(":", ".") + "/" + this.getName() + "."
                                     + TranslateCore.getPlatform().getConfigFormat().getFileType()[0]);
        this.init(type);
    }

    public AbstractShipsVessel(@NotNull SignTileEntity ste,
                               @NotNull SyncBlockPosition position,
                               @NotNull ShipType<? extends AbstractShipsVessel> type) {
        this.positionableShipsStructure = new AbstractPositionableShipsStructure(position);
        this.file = new File(ShipsPlugin.getPlugin().getConfigFolder(), "VesselData/" + ShipsPlugin
                .getPlugin()
                .getAllShipTypes()
                .stream()
                .filter(t -> ste
                        .getTextAt(1)
                        .orElseThrow(() -> new IllegalStateException("Could not get line 1 of sign"))
                        .toPlain()
                        .equalsIgnoreCase(t.getDisplayName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find the shiptype"))
                .getId()
                .replaceAll(":", ".") + "/" + ste
                .getTextAt(2)
                .orElseThrow(() -> new IllegalArgumentException("Could not get name of ship"))
                .toPlain() + "." + TranslateCore.getPlatform().getConfigFormat().getFileType()[0]);
        this.init(type);
    }

    private void init(ShipType<? extends AbstractShipsVessel> type) {
        ConfigurationStream.ConfigurationFile configuration = TranslateCore.createConfigurationFile(this.file,
                                                                                                    TranslateCore
                                                                                                            .getPlatform()
                                                                                                            .getConfigFormat());
        this.file = configuration.getFile();
        this.type = type;

    }

    protected void initRequirements() {
        if (!(this instanceof VesselRequirement requirement)) {
            //this if check will be removed when the OPShip is removed
            return;
        }
        if (!(this.type instanceof AbstractShipType<?> shipsType)) {
            //requirements may become standard
            return;
        }
        shipsType.getDefaultRequirements().stream().map(Requirement::createChild).forEach(requirement::setRequirement);

    }

    public Optional<Integer> getMaxSize() {
        ShipType<?> type = this.getType();
        if (!(type instanceof SizedShipType<?> sizedType)) {
            return Optional.empty();
        }
        return sizedType.getMaxSize();
    }

    @Override
    public @NotNull String getName() throws NoLicencePresent {
        this.cachedName = this
                .getSign()
                .getTextAt(2)
                .map(AText::toPlain)
                .orElseThrow(() -> new IllegalStateException("Could not find name of ship"));
        return this.cachedName;
    }

    @Override
    public Optional<String> getCachedName() {
        return Optional.ofNullable(this.cachedName);
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
        return this.getFlags().stream().filter(clazz::isInstance).map(v -> (T) v).findAny();
    }

    @Override
    public <T> @NotNull Vessel set(@NotNull Class<? extends VesselFlag<T>> flag, T value) {
        Optional<VesselFlag<?>> opFlag = this.getFlags().stream().filter(flag::isInstance).findFirst();
        if (opFlag.isEmpty()) {
            Optional<? extends VesselFlag<T>> opNewFlag = ShipsPlugin.getPlugin().get(flag);
            if (opNewFlag.isEmpty()) {
                AText error = AText.ofPlain(
                        "Class of " + flag.getName() + " is not registered in ShipsPlugin. " + "Failed to set for ");
                try {
                    TranslateCore.getConsole().sendMessage(error.append(AText.ofPlain(this.getId())));
                } catch (NoLicencePresent noLicencePresent) {
                    TranslateCore.getConsole().sendMessage(error.append(AText.ofPlain("unknown")));
                }
                return this;
            }
            VesselFlag<T> vFlag = opNewFlag.get();
            vFlag.setValue(value);
            this.flags.add(vFlag);
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
        collect.forEach(this.flags::remove);
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
        if (opDirectionalData.isEmpty()) {
            return this;
        }
        Direction direction = opDirectionalData.get().getDirection();

        double x = tel.getX() - position.getX();
        double y = tel.getY() - position.getY();
        double z = tel.getZ() - position.getZ();

        Vector3<Double> vector = this.flip(direction, x, y, z);

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
        if (opDirectionalData.isEmpty()) {
            return new HashMap<>();
        }
        Direction direction = opDirectionalData.get().getDirection();
        return this.teleportPositions
                .entrySet()
                .stream()
                .map((entry) -> {
                    double x = entry.getValue().getX();
                    double y = entry.getValue().getY();
                    double z = entry.getValue().getZ();

                    Vector3<Double> vec = this.flip(direction, x, y, z);
                    return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(),
                                                                  position.getRelative(vec.getX(), vec.getY(),
                                                                                       vec.getZ()));
                })
                .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey,
                                          AbstractMap.SimpleImmutableEntry::getValue));
    }

    @Override
    public @NotNull ShipType<? extends AbstractShipsVessel> getType() {
        return this.type;
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
    public @NotNull PositionableShipsStructure getStructure() {
        return this.positionableShipsStructure;
    }

    @Override
    public void setStructure(@NotNull PositionableShipsStructure pss) {
        if (TranslateCore.getPlatform().callEvent(new VesselStructureUpdate(pss, this)).isCancelled()) {
            return;
        }
        this.positionableShipsStructure = pss;
    }

    @Override
    public boolean isMaxSpeedSpecified() {
        return this.maxSpeed != null;
    }

    @Override
    public int getMaxSpeed() {
        if (this.maxSpeed == null) {
            return this.getType().getDefaultMaxSpeed();
        }
        return this.maxSpeed;
    }

    @Override
    public int getAltitudeSpeed() {
        if (this.altitudeSpeed == null) {
            return this.getType().getDefaultAltitudeSpeed();
        }
        return this.altitudeSpeed;
    }

    @Override
    public @NotNull Vessel setMaxSpeed(@Nullable Integer speed) {
        if (speed != null && speed < 0) {
            throw new IndexOutOfBoundsException("Speed cannot be less then 0");
        }
        this.maxSpeed = speed;
        return this;
    }

    @Override
    public boolean isAltitudeSpeedSpecified() {
        return this.altitudeSpeed != null;
    }

    @Override
    public @NotNull Vessel setAltitudeSpeed(@Nullable Integer speed) {
        if (speed != null && speed < 0) {
            throw new IndexOutOfBoundsException("Speed cannot be less then 0");
        }
        this.altitudeSpeed = speed;
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
