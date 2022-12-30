package org.ships.config.messages.error;

import org.core.inventory.item.ItemType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.config.messages.messages.error.ErrorNotEnoughFuelMessage;
import org.ships.config.messages.messages.error.data.FuelRequirementMessageData;
import org.ships.mock.common.MockShipsMain;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.requirement.FuelRequirement;
import org.ships.vessel.common.types.typical.airship.Airship;
import org.translate.common.MockItemType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NotEnoughFuelErrorMessage {

    private static final ErrorNotEnoughFuelMessage target = AdventureMessageConfig.ERROR_NOT_ENOUGH_FUEL;

    private MockedStatic<ShipsPlugin> staticPlugin;

    @BeforeEach
    public void setup() {
        staticPlugin = MockShipsMain.mockStatic();
    }

    @AfterEach
    public void shutdown() {
        staticPlugin.close();
    }

    @Test
    public void testValid() {
        //setup
        String itemId = "test:item";
        String itemName = "Item";

        Airship mockedVessel = Mockito.mock(Airship.class);

        ItemType mockedItem = MockItemType.createItem(itemId, itemName);

        FuelRequirement fuelRequirement = new FuelRequirement(null, FuelSlot.BOTTOM, 4,
                                                              Collections.singleton(mockedItem));
        Mockito.when(mockedVessel.getFuelRequirement()).thenReturn(fuelRequirement);
        Mockito.when(mockedVessel.getRequirements()).thenReturn(Collections.singleton(fuelRequirement));
        Mockito.when(mockedVessel.getRequirement(FuelRequirement.class)).thenReturn(Optional.of(fuelRequirement));

        //act
        String result = target.process(new FuelRequirementMessageData(mockedVessel, List.of(mockedItem), 1)).toPlain();

        //assert
        Assertions.assertTrue(result.contains(itemName),
                              "Could not find the itemname of '" + itemName + "' in message: " + result);

        Assertions.assertEquals(result, "Not enough fuel, you need 3 more of either Item");
    }

}
