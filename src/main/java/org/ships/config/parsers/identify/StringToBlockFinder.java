package org.ships.config.parsers.identify;

import org.ships.algorthum.blockfinder.BasicBlockFinder;
import org.ships.algorthum.blockfinder.BlockFinders;

public class StringToBlockFinder extends StringToIdentifiable<BasicBlockFinder> {

    public StringToBlockFinder() {
        super(() -> BlockFinders.getBlockFinders().stream());
    }
}
