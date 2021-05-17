package org.ships.config.messages.adapter;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class SpeedAdapter implements MessageAdapter {
    @Override
    public String adapterText() {
        return "Speed";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(new Random().nextInt(99) + "");
    }
}
