package org.ships.mock.common.config;

import org.core.config.ConfigurationStream;
import org.mockito.Mockito;
import org.ships.config.messages.AdventureMessageConfig;

import java.io.File;

public class MockAdventureMessageConfig {

    public static AdventureMessageConfig mockConfig(){
        ConfigurationStream.ConfigurationFile file = mockFile();

        AdventureMessageConfig mockedConfig = Mockito.mock(AdventureMessageConfig.class);
        Mockito.when(mockedConfig.getFile()).thenReturn(file);
        return mockedConfig;
    }

    public static ConfigurationStream.ConfigurationFile mockFile(){
        ConfigurationStream.ConfigurationFile mockedFile = Mockito.mock(ConfigurationStream.ConfigurationFile.class);
        Mockito.when(mockedFile.getFile()).thenReturn(new File(""));
        return mockedFile;
    }

}
