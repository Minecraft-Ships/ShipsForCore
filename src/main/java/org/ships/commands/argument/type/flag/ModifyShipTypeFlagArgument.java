package org.ships.commands.argument.type.flag;

import org.core.command.argument.ArgumentCommand;
import org.core.command.argument.CommandArgument;
import org.core.command.argument.arguments.operation.ExactArgument;
import org.core.command.argument.arguments.operation.StringParserArgument;
import org.core.command.argument.context.CommandContext;
import org.core.config.parser.StringParser;
import org.core.exceptions.NotEnoughArguments;
import org.core.permission.Permission;
import org.core.source.command.CommandSource;
import org.core.source.viewer.CommandViewer;
import org.ships.commands.argument.arguments.identifiable.ShipIdentifiableArgument;
import org.ships.commands.argument.arguments.identifiable.shiptype.flag.ShipTypeFlagArgument;
import org.ships.permissions.Permissions;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ModifyShipTypeFlagArgument implements ArgumentCommand {

    private final ExactArgument VESSEL_TYPE_KEY = new ExactArgument("vessel type key", false, "shiptype");
    private final ExactArgument FLAG_KEY = new ExactArgument("flag key", false, "flag");
    private final ExactArgument MODIFY_KEY = new ExactArgument("modify");
    private final ShipIdentifiableArgument<ShipType> VESSEL_TYPE = new ShipIdentifiableArgument<>("shiptype", ShipType.class, (c, a, v) -> !v.getFlags().isEmpty());
    private final ShipTypeFlagArgument VESSEL_TYPE_FLAG = new ShipTypeFlagArgument("flag", (c, a) -> c.getArgument(this, VESSEL_TYPE));
    private final StringParserArgument<Object> FLAG_PARSER = new StringParserArgument<>("flagValue", (c, a) -> (StringParser<Object>) c.getArgument(this, VESSEL_TYPE_FLAG).getParser(), (a, p) -> "Could not understand the value you entered");

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Arrays.asList(VESSEL_TYPE_KEY, FLAG_KEY, MODIFY_KEY, VESSEL_TYPE, VESSEL_TYPE_FLAG, FLAG_PARSER);
    }

    @Override
    public String getDescription() {
        return "modify a vessel type flag";
    }

    @Override
    public Optional<Permission> getPermissionNode() {
        return Optional.of(Permissions.CMD_SHIPTYPE_MODIFY_FLAG);
    }

    @Override
    public boolean hasPermission(CommandSource source) {
        if(!(source instanceof CommandViewer)){
            return false;
        }
        return ArgumentCommand.super.hasPermission(source);
    }

    @Override
    public boolean run(CommandContext commandContext, String... args) throws NotEnoughArguments {
        VesselFlag<?> flag = commandContext.getArgument(this, VESSEL_TYPE_FLAG);
        Object parsedValue = commandContext.getArgument(this, FLAG_PARSER);
        setFlagValue(flag, parsedValue);
        return true;
    }

    private <T> void setFlagValue(VesselFlag<T> flag, Object value){
        flag.setValue((T)value);
    }
}
