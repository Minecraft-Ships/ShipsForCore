package org.ships.config.parsers.identify;

import org.ships.algorthum.blockfinder.BasicBlockFinder;

public class StringToBlockFinder extends StringToIdentifiable<BasicBlockFinder> {

    public StringToBlockFinder() {
        super(BasicBlockFinder.class);
    }
}
