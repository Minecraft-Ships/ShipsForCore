package org.ships.config.messages.adapter;

import org.core.adventureText.AText;

import java.util.Set;

public interface MessageAdapter<T> {

    String adapterText();

    Set<String> examples();

    AText process(AText message, T obj);

    default String adapterTextFormat() {
        return "%" + adapterText() + "%";
    }

}
