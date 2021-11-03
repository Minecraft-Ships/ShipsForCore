package org.ships.config.messages.adapter.config;

import org.core.adventureText.AText;
import org.ships.config.messages.adapter.MessageAdapter;

public interface ConfigAdapter extends MessageAdapter<Object> {

    AText process(AText message);

    @Override
    @Deprecated
    default AText process(AText message, Object obj) {
        return this.process(message);
    }
}
