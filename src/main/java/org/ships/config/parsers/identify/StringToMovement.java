package org.ships.config.parsers.identify;

import org.ships.algorthum.movement.BasicMovement;
import org.ships.algorthum.movement.Movements;

public class StringToMovement extends StringToIdentifiable<BasicMovement> {

    public StringToMovement() {
        super(() -> Movements.getMovements().stream());
    }
}
