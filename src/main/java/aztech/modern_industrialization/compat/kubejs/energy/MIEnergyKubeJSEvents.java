package aztech.modern_industrialization.compat.kubejs.energy;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface MIEnergyKubeJSEvents {
    EventGroup EVENT_GROUP = EventGroup.of("MIEnergyTierEvents");

    EventHandler MODIFY = EVENT_GROUP.startup("modify", () -> ModifyEnergyTierEventJS.class);
}
