package org.ships.config.node;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class RawDedicatedNode <V, N extends ConfigurationNode.KnownParser<?, V>> implements DedicatedNode<V, V, N> {

    private final N node;
    private final String keyName;
    private final BiConsumer<ConfigurationStream, Map.Entry<N, V>> consumer;

    public RawDedicatedNode(N node, String keyName, BiConsumer<ConfigurationStream, Map.Entry<N, V>> consumer){
        this.node = node;
        this.keyName = keyName;
        this.consumer = consumer;
    }

    @Override
    public N getNode() {
        return this.node;
    }

    @Override
    public String getKeyName() {
        return this.keyName;
    }

    @Override
    public void apply(ConfigurationStream stream, V value) {
        this.consumer.accept(stream, new AbstractMap.SimpleImmutableEntry<>(getNode(), value));
    }
}
