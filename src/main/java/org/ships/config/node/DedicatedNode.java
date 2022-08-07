package org.ships.config.node;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;

public interface DedicatedNode<A, V, N extends ConfigurationNode.KnownParser<?, V>> {

    N getNode();

    String getKeyName();

    void apply(ConfigurationStream stream, A value);
}
