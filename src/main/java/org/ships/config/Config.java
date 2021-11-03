package org.ships.config;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.ships.config.node.DedicatedNode;

import java.util.Optional;
import java.util.Set;

public interface Config {

    interface KnownNodes extends Config {

        <A, V, N extends ConfigurationNode.KnownParser<?, V>> Set<DedicatedNode<A, V, N>> getNodes();

        default Optional<DedicatedNode<Object, Object, ConfigurationNode.KnownParser<String, Object>>> getNode(String key){
            return (Optional<DedicatedNode<Object, Object, ConfigurationNode.KnownParser<String, Object>>>)(Object) this.getNodes().parallelStream().filter(n -> n.getKeyName().equalsIgnoreCase(key)).findAny();
        }

    }

    ConfigurationStream.ConfigurationFile getFile();
    void recreateFile();
}
