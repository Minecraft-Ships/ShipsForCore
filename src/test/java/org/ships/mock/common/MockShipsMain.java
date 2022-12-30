package org.ships.mock.common;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.mock.common.config.MockAdventureMessageConfig;
import org.ships.plugin.ShipsPlugin;

public class MockShipsMain {

    public static MockedStatic<ShipsPlugin> mockStatic() {
        ShipsPlugin mockedPlugin = mockPlugin();


        MockedStatic<ShipsPlugin> mockedStatic = Mockito.mockStatic(ShipsPlugin.class);
        mockedStatic.when(ShipsPlugin::getPlugin).thenReturn(mockedPlugin);

        return mockedStatic;
    }

    public static ShipsPlugin mockPlugin() {
        AdventureMessageConfig mockConfig = MockAdventureMessageConfig.mockConfig();

        ShipsPlugin mockedPlugin = Mockito.mock(ShipsPlugin.class);
        Mockito.when(mockedPlugin.getAdventureMessageConfig()).thenReturn(mockConfig);
        return mockedPlugin;
    }

}
