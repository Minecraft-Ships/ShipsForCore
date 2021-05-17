package org.ships.vessel.converts;

import org.core.utils.Identifiable;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.common.assits.IdentifiableShip;

import java.io.File;
import java.io.IOException;

public interface ShipsConverter<T extends IdentifiableShip> extends Identifiable {

    @NotNull File getFolder();

    @NotNull T convert(@NotNull File file) throws IOException;
}
