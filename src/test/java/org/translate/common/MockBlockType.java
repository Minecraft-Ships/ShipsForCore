package org.translate.common;

import org.core.world.position.block.BlockType;
import org.mockito.Mockito;

public class MockBlockType {

    public static BlockType createItem() {
        return createItem("test:item", "Item");
    }

    public static BlockType createItem(String id, String name) {
        BlockType mockedItem = Mockito.mock(BlockType.class);
        Mockito.when(mockedItem.getName()).thenReturn(name);
        Mockito.when(mockedItem.getId()).thenReturn(id);
        return mockedItem;
    }

}
