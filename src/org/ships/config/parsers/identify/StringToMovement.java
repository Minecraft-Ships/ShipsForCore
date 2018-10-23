package org.ships.config.parsers.identify;

import org.ships.algorthum.movement.BasicMovement;

public class StringToMovement extends StringToIdentifiable<BasicMovement> {

    public StringToMovement() {
        super(BasicMovement.class);
    }
}
