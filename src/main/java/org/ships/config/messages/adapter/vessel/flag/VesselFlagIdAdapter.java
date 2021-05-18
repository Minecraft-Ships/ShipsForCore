package org.ships.config.messages.adapter.vessel.flag;

import org.core.adventureText.AText;
import org.core.utils.Else;
import org.core.utils.Identifiable;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.flag.VesselFlag;

import java.util.Collections;
import java.util.Set;

public class VesselFlagIdAdapter implements MessageAdapter<VesselFlag<?>> {
    @Override
    public String adapterText() {
        return "Vessel Key Id";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("ships.is_moving");
    }

    @Override
    public AText process(AText message, VesselFlag<?> obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(Else.canCast(obj, VesselFlag.Serializable.class, (v) -> v.getId() + ":" + v.serialize(), Identifiable::getId)));
    }
}
