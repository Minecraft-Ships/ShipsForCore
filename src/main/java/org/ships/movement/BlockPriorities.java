package org.ships.movement;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedTransferQueue;

public final class BlockPriorities {

    private static final Collection<BlockPriority> registered = new LinkedTransferQueue<>();

    public static final BlockPriority ATTACHED = register(new PackagedBlockPriority("attached", 5));
    public static final BlockPriority DIRECTIONAL = register(new PackagedBlockPriority("directional", 10));
    public static final BlockPriority NORMAL = register(new PackagedBlockPriority("normal", 100));
    public static final BlockPriority AIR = register(new PackagedBlockPriority("air", 200));

    private BlockPriorities() {
        throw new RuntimeException("Do not create");
    }

    public static <T extends BlockPriority> T register(@NotNull T blockPriority) {
        registered.add(blockPriority);
        return blockPriority;
    }

    public static Collection<BlockPriority> priorities() {
        return Collections.unmodifiableCollection(registered);
    }

}
