package aztech.modern_industrialization.compat.kubejs.energy;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.CableTierRegistry;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.level.block.Block;

public class ModifyEnergyTierEventJS extends EventJS {
    public void register(String tier_english_name, String tier_id, long eu, String tier_english_full_name, String casing_name, String casing_id) {
        CableTierRegistry.addTier(
                new CableTier(tier_english_name, tier_id, eu, tier_english_full_name, MIBlock.block(casing_name, casing_id))
        );
    }
}
