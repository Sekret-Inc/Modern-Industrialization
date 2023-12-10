package aztech.modern_industrialization.api.energy;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.definition.BlockDefinition;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public abstract class CableTierRegistry {

    public static class CableTierEntry {
        public String englishName;
        public String id;
        public long eu;
        public String englishFullName;
        public String hullEnglishName;
        public String hullId;
        @Nullable
        public Map<String, String> options;
        public CableTierEntry(String englishName, String id, long eu, String englishFullName, String hullEnglishName, String hullId, @Nullable Map<String, String> options) {
            this.englishName = englishName;
            this.id = id;
            this.eu = eu;
            this.englishFullName = englishFullName;
            this.hullEnglishName = hullEnglishName;
            this.hullId = hullId;
            this.options = options;
        }
    }

    public static ArrayList<CableTierEntry> unprocessedTierList;
    public static ArrayList<CableTier> tierList;
    public static Map<Block, CableTier> hullToTier;
    public static CableTier defaultTier = null;

    static {
        unprocessedTierList = new ArrayList<>();
        hullToTier = new HashMap<>();
        unprocessedTierList.add(new CableTierEntry("LV", "lv", 32, "Low Voltage", "Basic Machine Hull", "basic_machine_hull", null));
        unprocessedTierList.add(new CableTierEntry("MV", "mv", 32 * 4, "Medium Voltage", "Advanced Machine Hull", "advanced_machine_hull", null));
        unprocessedTierList.add(new CableTierEntry("HV", "hv", 32 * 4 * 8, "High Voltage", "Turbo Machine Hull", "turbo_machine_hull", null));
        unprocessedTierList.add(new CableTierEntry("EV", "ev", 32 * 4 * 8 * 8, "Extreme Voltage", "Highly Advanced Machine Hull", "highly_advanced_machine_hull", null));
        var quantum = new HashMap<String, String>();
        quantum.put("ae2", "true");
        quantum.put("superResistant", "true");
        unprocessedTierList.add(new CableTierEntry("Superconductor", "superconductor", 128000000, "Superconductor", "Quantum Machine Hull", "quantum_machine_hull", quantum));
    }

    public static void addTier(CableTier tier) {
        if (getByName(tier.name) != null) {
            throw new IllegalArgumentException("Added twice energy tier: " + tier.name);
        }
        if (tier.name.equals("lv")) {
            defaultTier = tier;
        }
        tierList.add(tier);
        hullToTier.put(tier.machineHull, tier);
    }

    public static void init() {
    }

    private static boolean parseBoolOption(CableTierEntry tier, Map.Entry<String, String> entry) {
        var value = entry.getValue().toLowerCase();
        if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        } else {
            throw new IllegalArgumentException("Cable tier " + tier.id + " has property " + entry.getKey() + " with invalid value: " + entry.getValue());
        }
    }

    public static void finishInitialization() {
        tierList = new ArrayList<>();
        for (var tier : unprocessedTierList) {
            boolean isAE2Compatible = false;
            boolean isSuperResistant = false;
            if (tier.options != null) {
                for (var entry : tier.options.entrySet()) {
                    switch (entry.getKey()) {
                        case "ae2":
                            isAE2Compatible = parseBoolOption(tier, entry);
                            break;
                        case "superResistant":
                            isSuperResistant = parseBoolOption(tier, entry);
                            break;
                        default:
                            throw new IllegalArgumentException("Cable tier " + tier.id + " has invalid additional property " + entry.getKey());
                    }
                }
            }

            BlockDefinition<Block> block;
            if (isSuperResistant) {
                block = MIBlock.block(tier.hullEnglishName, tier.hullId, MIBlock.BlockDefinitionParams.defaultStone().resistance(6000f));
            } else {
                block = MIBlock.block(tier.hullEnglishName, tier.hullId);
            }

            CableTier cableTier = new CableTier(tier.englishName, tier.id, tier.eu, tier.englishFullName, block);
            if (isAE2Compatible) {
                cableTier.setAE2Compatible();
            }
            addTier(cableTier);
        }
        unprocessedTierList = null;
        CableTierRegistry.tierList.sort(Comparator.naturalOrder());
    }

    // TODO(Despacito696969): Should this be removed?
    public static void removeTier(String name) {
        int result = -1;
        for (int i = 0; i < tierList.size(); ++i) {
            if (tierList.get(i).name.equals(name)) {
                result = i;
                break;
            }
        }
        if (result == -1) {
            throw new IllegalArgumentException("Cable Tier: " + name + " not found!");
        }

        if (tierList.get(result).name.equals("lv")) {
            defaultTier = null;
        }

        tierList.remove(result);
    }

    @Nullable
    public static CableTier getByName(String tierName) {
        for (var tier : tierList) {
            if (tier.name.equals(tierName)) {
                return tier;
            }
        }
        return null;
    }

    public static CableTier getByNameOrThrow(String tierName) {
        var result = getByName(tierName);
        if (result == null) {
            throw new IllegalArgumentException("Couldn't find cable tier: " + tierName);
        }
        return result;
    }
}
