package org.ships.commands.argument.ship.structure;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.RemainingArgument;
import org.core.command.argument.arguments.simple.StringArgument;
import org.core.command.argument.context.CommandContext;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.viewer.CommandViewer;
import org.core.utils.Bounds;
import org.core.world.structure.Structure;
import org.core.world.structure.StructureBuilder;
import org.ships.commands.argument.arguments.ShipIdArgument;
import org.ships.permissions.Permissions;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShipStructureSaveCommand implements ArgumentCommand {

    ShipIdArgument<? extends Vessel> SHIP = new ShipIdArgument<>("id");
    RemainingArgument<String> NAME = new RemainingArgument<>(new StringArgument("name"));

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(new ExactArgument("ship"), this.SHIP, new ExactArgument("structure"),
                             new ExactArgument("save"), this.NAME);
    }

    @Override
    public String getDescription() {
        return "Saves the structure of the ship as a .structure file";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIP_STRUCTURE_SAVE);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        Vessel vessel = commandContext.getArgument(this, this.SHIP);
        String name = String.join(" ", commandContext.getArgument(this, this.NAME));
        String id = name.toLowerCase().replace(" ", "_");

        File file = new File(ShipsPlugin.getPlugin().getConfigFolder(),
                             "Structure/" + ShipsPlugin.getPlugin().getPluginId() + "/" + id + ".structure");
        if (file.exists()) {
            if (commandContext.getSource() instanceof CommandViewer viewer) {
                viewer.sendMessage(AText.ofPlain("Cannot replace another structure file"));
            }
            return false;
        }

        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            if (commandContext.getSource() instanceof CommandViewer viewer) {
                viewer.sendMessage(AText.ofPlain("Error when creating file: " + e.getMessage()));
            }
            e.printStackTrace();
            return false;
        }

        Bounds<Integer> bounds = vessel.getStructure().getBounds();
        bounds.addX(1);
        bounds.addY(1);
        bounds.addZ(1);

        if (bounds.getIntMax().equals(bounds.getIntMin())) {
            if (commandContext.getSource() instanceof CommandViewer viewer) {
                viewer.sendMessage(AText.ofPlain(
                        "Size of ship is invalid. Manually updating the ship structure should fix this by sneaking and clicking the [ships] sign"));
            }
            return false;
        }

        StructureBuilder structureBuilder = new StructureBuilder()
                .setId(id)
                .setPlugin(ShipsPlugin.getPlugin())
                .setName(name)
                .setBounds(bounds)
                .setWorld(vessel.getPosition().getWorld());
        Structure structure = TranslateCore.getPlatform().register(structureBuilder);
        try {
            structure.serialize(file);
            if (commandContext.getSource() instanceof CommandViewer viewer) {
                viewer.sendMessage(AText.ofPlain("Saved"));
            }
        } catch (IOException e) {
            if (commandContext.getSource() instanceof CommandViewer viewer) {
                viewer.sendMessage(AText.ofPlain("Error saving: " + e.getMessage()));
            }
            e.printStackTrace();
        }
        return true;
    }
}
