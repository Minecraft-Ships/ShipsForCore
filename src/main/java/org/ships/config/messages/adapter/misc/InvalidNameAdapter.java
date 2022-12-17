package org.ships.config.messages.adapter.misc;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collections;
import java.util.Set;

public class InvalidNameAdapter implements MessageAdapter<String> {
    @Override
    public String adapterText() {
        return "Invalid Name";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("Invalid");
    }

    @Override
    public AText process(@NotNull String obj) {
        return AText.ofPlain(obj);
    }
}
