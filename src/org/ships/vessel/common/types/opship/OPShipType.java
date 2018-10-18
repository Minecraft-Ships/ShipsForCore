package org.ships.vessel.common.types.opship;

import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.configuration.parser.Parser;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipType;

public class OPShipType implements ShipType {

    protected ConfigurationFile file;
    protected ExpandedBlockList blockList = new ExpandedBlockList(getFile(), ShipsPlugin.getPlugin().getBlockList());

    private final String[] MAX_SPEED = {"Speed", "Max"};
    private final String[] ALTITUDE_SPEED = {"Speed", "Altitude"};

    @Override
    public String getDisplayName() {
        return "OPShip";
    }

    @Override
    public ExpandedBlockList getDefaultBlockList() {
        return this.blockList;
    }

    @Override
    public int getDefaultMaxSpeed() {
        return file.parse(new ConfigurationNode(this.file.getRootNode(), this.MAX_SPEED), Parser.STRING_TO_INTEGER).get();
    }

    @Override
    public int getDefaultAltitudeSpeed() {
        return file.parse(new ConfigurationNode(this.file.getRootNode(), this.ALTITUDE_SPEED), Parser.STRING_TO_INTEGER).get();
    }

    @Override
    public boolean canAutopilot() {
        return false;
    }

    @Override
    public ConfigurationFile getFile() {
        return null;
    }

    @Override
    public String getId() {
        return "ships:" + getDisplayName();
    }

    @Override
    public String getName() {
        return getDisplayName();
    }
}
