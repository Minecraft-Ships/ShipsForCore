package org.ships.config.node;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;

public class ObjectDedicatedNode<V, N extends ConfigurationNode.KnownParser.SingleKnown<V>>
        implements DedicatedNode<V, V, N> {

    private final N node;
    private final String name;

    public ObjectDedicatedNode(N node, String name) {
        this.name = name;
        this.node = node;
    }

    @Override
    public N getNode() {
        return this.node;
    }

    @Override
    public String getKeyName() {
        return this.name;
    }

    @Override
    public void apply(ConfigurationStream stream, V value) {
        stream.set(this.node, value);
    }
}
