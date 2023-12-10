package aztech.modern_industrialization.api.energy;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.models.MachineCasings;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public abstract class CableTierRegistry {
    public static ArrayList<CableTier> tierList;
    public static Map<Block, CableTier> hullToTier;

    public static CableTier defaultTier = null;

    static {
        tierList = new ArrayList<>();
        hullToTier = new HashMap<>();
        addTier(new CableTier("LV", "lv", 32, "Low Voltage", MIBlock.block("Basic Machine Hull", "basic_machine_hull")));
        addTier(new CableTier("MV", "mv", 32 * 4, "Medium Voltage", MIBlock.block("Advanced Machine Hull", "advanced_machine_hull")));
        addTier(new CableTier("HV", "hv", 32 * 4 * 8, "High Voltage", MIBlock.block("Turbo Machine Hull", "turbo_machine_hull")));
        addTier(new CableTier("EV", "ev", 32 * 4 * 8 * 8, "Extreme Voltage", MIBlock.block("Highly Advanced Machine Hull", "highly_advanced_machine_hull")));
        addTier(new CableTier("Superconductor", "superconductor", 128000000, "Superconductor", MIBlock.block("Quantum Machine Hull", "quantum_machine_hull", MIBlock.BlockDefinitionParams.defaultStone().resistance(6000f))).setAE2Compatible());
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

    public static void finishInitialization() {
        CableTierRegistry.tierList.sort(Comparator.naturalOrder());
    }

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
