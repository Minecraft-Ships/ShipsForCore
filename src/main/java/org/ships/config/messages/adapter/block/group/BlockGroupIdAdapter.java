package org.ships.config.messages.adapter.block.group;

import net.kyori.adventure.text.Component;

public class BlockGroupIdAdapter extends BlockGroupAdapter {

    public BlockGroupIdAdapter() {
        this("Block Group Id");
    }

    public BlockGroupIdAdapter(String name) {
        super(name, type -> Component.text(type.getId()));
    }
}
