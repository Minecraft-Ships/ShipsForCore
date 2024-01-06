package org.ships.config.messages.adapter.category;

class AbstractAdapterCategory<T> implements AdapterCategory<T> {

    private final Class<?> adapterType;
    private final String name;
    private final String id;

    public AbstractAdapterCategory(Class<?> adapterType, String name) {
        this(adapterType, name, name.replaceAll(" ", "_").toLowerCase());
    }

    public AbstractAdapterCategory(Class<?> adapterType, String name, String id) {
        this.adapterType = adapterType;
        this.name = name;
        this.id = id;
    }


    @Override
    public Class<?> adapterType() {
        return this.adapterType;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
