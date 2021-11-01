package org.ships.config.node;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;

import java.util.Collection;

public class CollectionDedicatedNode <V, K extends Collection<V>, N extends ConfigurationNode.KnownParser.CollectionKnown<V>> implements DedicatedNode<K, V, N> {

    private final N node;
    private final String name;

    public CollectionDedicatedNode(N node, String name){
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
    public void apply(ConfigurationStream stream, K value) {
        stream.set(this.node, value);
    }
}
