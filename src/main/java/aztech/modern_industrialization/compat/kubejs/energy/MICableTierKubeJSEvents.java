package aztech.modern_industrialization.compat.kubejs.energy;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface MICableTierKubeJSEvents {
    EventGroup EVENT_GROUP = EventGroup.of("MICableTierEvents");

    EventHandler MODIFY = EVENT_GROUP.startup("modify", () -> ModifyCableTiersEventJS.class);
}
