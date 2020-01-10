package org.ships.config;

import org.core.configuration.ConfigurationFile;
import org.ships.config.node.DedicatedNode;

import java.util.Optional;
import java.util.Set;

public interface Config {

    interface CommandConfigurable extends Config{

        Set<DedicatedNode<?>> getNodes();

        default Optional<?> getValue(String simpleName){
            Optional<DedicatedNode<?>> opNode = getNodes().stream().filter(n -> n.getSimpleName().equalsIgnoreCase(simpleName)).findAny();
            if(!opNode.isPresent()){
                return Optional.empty();
            }
            return opNode.get().getValue(this.getFile());
        }

        default Optional<DedicatedNode<?>> get(String simpleName){
            return getNodes().stream().filter(n -> n.getSimpleName().equalsIgnoreCase(simpleName)).findAny();
        }
    }

    ConfigurationFile getFile();
    void recreateFile();
}
