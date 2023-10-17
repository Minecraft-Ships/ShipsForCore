package org.ships.commands.argument.create;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.CommandArgumentResult;
import org.core.command.argument.ParseCommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.OptionalArgument;
import org.core.command.argument.arguments.position.WorldArgument;
import org.core.command.argument.arguments.simple.StringArgument;
import org.core.command.argument.arguments.simple.number.IntegerArgument;
import org.core.command.argument.context.CommandArgumentContext;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.utils.Bounds;
import org.core.vector.type.Vector3;
import org.core.world.WorldExtent;
import org.core.world.position.Positionable;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.structure.Structure;
import org.core.world.structure.StructurePlacementBuilder;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.commands.argument.arguments.structure.ShipsStructureArgument;
import org.ships.exceptions.load.LoadVesselException;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.finder.IdVesselFinder;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CreateShipCommand implements ArgumentCommand {

    private static final OptionalArgument<Integer> X = new OptionalArgument<>(new IntegerArgument("x"), new Parse<>(
            pos -> pos.getX().intValue()));
    private static final OptionalArgument<Integer> Y = new OptionalArgument<>(new IntegerArgument("y"), new Parse<>(
            pos -> pos.getY().intValue()));
    private static final OptionalArgument<Integer> Z = new OptionalArgument<>(new IntegerArgument("z"), new Parse<>(
            pos -> pos.getZ().intValue()));
    private static final OptionalArgument<WorldExtent> WORLD = new OptionalArgument<>(new WorldArgument("world"),
                                                                                      new Parse<>(Position::getWorld));
    private static final ShipIdentifiableArgument<ShipType<?>> SHIP_TYPE = new ShipIdentifiableArgument<>("ship_type",
                                                                                                          (Class<ShipType<?>>) (Object) ShipType.class);
    private static final ShipsStructureArgument STRUCTURE = new ShipsStructureArgument("structure");
    private static final StringArgument NAME = new StringArgument("name");

    private record Parse<T>(Function<? super Position<? extends Number>, ? extends T> function)
            implements ParseCommandArgument<T> {

        @Override
        public CommandArgumentResult<T> parse(CommandContext context, CommandArgumentContext<T> argument)
                throws IOException {
            if (!(context.getSource() instanceof Positionable)) {
                throw new IOException("Player only command assumption for x, y, z and world");
            }
            Positionable<? extends Number> positionable = (Positionable<? extends Number>) context.getSource();
            Position<? extends Number> position = positionable.getPosition();
            return CommandArgumentResult.from(argument, this.function.apply(position));
        }

    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument("create"), X, Y, Z, WORLD, STRUCTURE, SHIP_TYPE, NAME);
    }

    @Override
    public String getDescription() {
        return "Creates a pre-defined ship";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.empty();
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Integer x = commandContext.getArgument(this, X);
        Integer y = commandContext.getArgument(this, Y);
        Integer z = commandContext.getArgument(this, Z);
        WorldExtent world = commandContext.getArgument(this, WORLD);
        ShipType<?> shipType = commandContext.getArgument(this, SHIP_TYPE);
        Structure structure = commandContext.getArgument(this, STRUCTURE);
        String name = commandContext.getArgument(this, NAME);
        if (x == null) {
            return false;
        }
        if (y == null) {
            return false;
        }
        if (z == null) {
            return false;
        }
        if (world == null) {
            return false;
        }
        if (name.contains(":")) {
            name = name.replaceAll(":", "");
        }

        String fullId = shipType.getId() + ":" + name.toLowerCase().replaceAll(" ", "_");
        try {
            IdVesselFinder.load(fullId);
            commandContext.getSource().sendMessage(Component.text("Ship name already taken"));
            return false;
        } catch (LoadVesselException e) {
        }

        SyncBlockPosition start = world.getPosition(x, y, z);
        structure.place(new StructurePlacementBuilder().setPosition(start));
        commandContext.getSource().sendMessage(Component.text("placing"));
        Map.Entry<LiveSignTileEntity, Boolean> signTileEntity;
        try {
            signTileEntity = this.findLicence(structure, start);
        } catch (IllegalStateException e) {
            commandContext.getSource().sendMessage(Component.text("Invalid structure file"));

            return false;
        }
        List<Component> lines = Arrays.asList(Component.text("[Ships]").color(NamedTextColor.YELLOW),
                                              Component.text(shipType.getDisplayName()).color(NamedTextColor.BLUE),
                                              Component.text(name).color(NamedTextColor.GREEN));
        SignSide side = signTileEntity.getKey().getSide(signTileEntity.getValue());
        side.setLines(lines);
        Vessel vessel = shipType.createNewVessel(side, signTileEntity.getKey().getPosition());
        ShipsPlugin.getPlugin().registerVessel(vessel);
        vessel.save();
        commandContext.getSource().sendMessage(Component.text("Created ship"));
        vessel.setLoading(false);
        return true;
    }

    private Map.Entry<LiveSignTileEntity, Boolean> findLicence(Structure structure, Position<Integer> start) {
        Bounds<Integer> bounds = new Bounds<>(start.getPosition(), start.getPosition().plus(structure.getSize()));
        Vector3<Integer> minInt = bounds.getIntMin();
        Vector3<Integer> maxInt = bounds.getIntMax();
        for (int x = minInt.getX(); x < maxInt.getX(); x++) {
            for (int y = minInt.getY(); y < maxInt.getY(); y++) {
                for (int z = minInt.getZ(); z < maxInt.getZ(); z++) {
                    SyncBlockPosition pos = start.getWorld().getPosition(x, y, z);
                    Optional<LiveTileEntity> opTile = pos.getTileEntity();
                    if (opTile.isEmpty()) {
                        continue;
                    }
                    if (!(opTile.get() instanceof LiveSignTileEntity ste)) {
                        continue;
                    }
                    LicenceSign licence = ShipsPlugin
                            .getPlugin()
                            .get(LicenceSign.class)
                            .orElseThrow(() -> new RuntimeException("Could not find licence sign in register."));
                    if (licence.isSign(ste)) {
                        return Map.entry(ste, licence.getSide(ste).map(SignSide::isFront).orElse(false));
                    }
                }
            }
        }
        throw new IllegalStateException("Cannot find licence sign");
    }
}
