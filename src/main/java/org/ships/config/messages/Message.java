package org.ships.config.messages;

import org.core.adventureText.AText;
import org.core.config.ConfigurationNode;
import org.core.config.parser.StringParser;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Set;
import java.util.stream.Collectors;

public interface Message {

    String[] getPath();

    AText getDefault();

    Set<MessageAdapter> getAdapters();

    default ConfigurationNode.KnownParser.SingleKnown<AText> getKnownPath() {
        return new ConfigurationNode.KnownParser.SingleKnown<>(StringParser.STRING_TO_TEXT, this.getPath());
    }

    default AText parse(AdventureMessageConfig config) {
        return config.getFile().parse(this.getKnownPath()).orElse(this.getDefault());
    }

    default Set<String> suggestAdapter(String peek) {
        String peekLower = peek.replaceAll("%", "").toLowerCase();
        return getAdapters().parallelStream().map(a -> a.adapterText().toLowerCase()).filter(a -> a.contains(peekLower)).collect(Collectors.toSet());
    }
}
