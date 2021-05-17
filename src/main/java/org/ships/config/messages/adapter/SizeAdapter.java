package org.ships.config.messages.adapter;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class SizeAdapter implements MessageAdapter {
    @Override
    public String adapterText() {
        return "Size";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(new Random().nextInt(99) + "");
    }
}
