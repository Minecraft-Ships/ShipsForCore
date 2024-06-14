package org.ships.vessel.converts;

import org.jetbrains.annotations.UnmodifiableView;
import org.ships.vessel.converts.vessel.shipsfive.Ships5VesselConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedTransferQueue;

public class ShipsConverters {

    private static final Collection<ShipsConverter<?>> registered = new LinkedTransferQueue<>();

    public static Ships5VesselConverter SHIPS_FIVE_CONVERTER = register(new Ships5VesselConverter());

    public static <C extends ShipsConverter<?>> C register(C converter){
        registered.add(converter);
        return converter;
    }

    @UnmodifiableView
    public static Collection<ShipsConverter<?>> converters(){
        return Collections.unmodifiableCollection(registered);
    }

}
