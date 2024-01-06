package org.ships.config.messages.adapter.category;

import org.core.utils.Identifiable;
import org.ships.config.messages.adapter.MessageAdapter;

public interface AdapterCategory<T> extends Identifiable {

    Class<?> adapterType();

    default boolean canAccept(MessageAdapter<?> adapter) {
        var categories = adapter.categories();
        if(categories == null){
            throw new RuntimeException("MessageAdapter categories is null: " + adapter.getClass().getName());
        }
        return categories.contains(this);
    }

    default MessageAdapter<T> onAccept(MessageAdapter<?> adapter) {
        return (MessageAdapter<T>) adapter;
    }

    default <M> AdapterCategory<M> map(Class<?> t) {
        return new AbstractAdapterCategory<>(t, this.getName(), this.getId());
    }

}
