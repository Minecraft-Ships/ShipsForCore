package org.ships.config.messages.adapter.block.group;

import net.kyori.adventure.text.Component;

public class BlockGroupNameAdapter extends BlockGroupAdapter {

    public BlockGroupNameAdapter() {
        this("Block Group Name");
    }

    public BlockGroupNameAdapter(String name) {
        super(name, type -> Component.text(type.getId()));
    }
}
