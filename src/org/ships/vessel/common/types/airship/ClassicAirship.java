package org.ships.vessel.common.types.airship;

import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.vessel.common.assits.ClassicShip;

public class ClassicAirship extends Airship implements ClassicShip {

    protected ConfigurationNode configClassicSpecialBlockPercent = new ConfigurationNode("");

    public ClassicAirship(LiveSignTileEntity licence) {
        super(licence);
    }

    public ClassicAirship(SignTileEntity ste, BlockPosition position) {
        super(ste, position);
    }

    @Override
    public ClassicShip deserializeClassicExtra(ConfigurationFile file) {
        this.specialBlockPercent = file.parseDouble(this.configClassicSpecialBlockPercent).get().floatValue();
        return this;
    }
}
