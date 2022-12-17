package org.ships.config.messages.adapter.specific;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Set;

public class NamedBlockNameAdapter implements MessageAdapter<String> {
    @Override
    public String adapterText() {
        return "Block Name";
    }

    @Override
    public Set<String> examples() {
        return Set.of("Cannot find %Block Name%");
    }

    @Override
    public AText process(@NotNull String obj) {
        return AText.ofPlain(obj);
    }
}
