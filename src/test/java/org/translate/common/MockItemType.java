package org.translate.common;

import org.core.inventory.item.ItemType;
import org.mockito.Mockito;

public class MockItemType {

    public static ItemType createItem() {
        return createItem("test:item", "Item");
    }

    public static ItemType createItem(String id, String name) {
        ItemType mockedItem = Mockito.mock(ItemType.class);
        Mockito.when(mockedItem.getName()).thenReturn(name);
        Mockito.when(mockedItem.getId()).thenReturn(id);
        return mockedItem;
    }

}
