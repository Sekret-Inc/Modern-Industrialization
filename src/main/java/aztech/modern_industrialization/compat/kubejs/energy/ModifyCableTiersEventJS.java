package aztech.modern_industrialization.compat.kubejs.energy;

import aztech.modern_industrialization.api.energy.CableTierRegistry;
import dev.latvian.mods.kubejs.event.EventJS;

import java.util.ArrayList;
import java.util.Map;

public class ModifyCableTiersEventJS extends EventJS {
    public void register(String tierEnglishNameShort, String tierId, long eu, String tierEnglishFullname, String casingEnglishName, String casingId, Map<String, String> options) {
        CableTierRegistry.unprocessedTierList.add(
            new CableTierRegistry.CableTierEntry(tierEnglishNameShort, tierId, eu, tierEnglishFullname, casingEnglishName, casingId, options)
        );
    }

    public ArrayList<CableTierRegistry.CableTierEntry> getEntries() {
        return CableTierRegistry.unprocessedTierList;
    }

    public void modifyEntry(CableTierRegistry.CableTierEntry entry) {
        for (int i = 0; i < CableTierRegistry.unprocessedTierList.size(); ++i) {
            var tier = CableTierRegistry.unprocessedTierList.get(i);
            if (tier.id.equals(entry.id)) {
                CableTierRegistry.unprocessedTierList.set(i, entry);
                return;
            }
        }

        throw new IllegalArgumentException("Cannot modify entry of cable tier with id " + entry.id + ", because it isn't registered");
    }
}
