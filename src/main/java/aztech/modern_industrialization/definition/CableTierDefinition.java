package aztech.modern_industrialization.definition;

import aztech.modern_industrialization.ModernIndustrialization;

public class CableTierDefinition extends Definition {
    String englishFullName;
    String cableTierId;
    String englishShortName;
    public CableTierDefinition(String englishFullName, String englishShortName, String cableTierId) {
        super(englishFullName, cableTierId);
        this.englishFullName = englishFullName;
        this.englishShortName = englishShortName;
        this.cableTierId = cableTierId;
    }

    @Override
    public String getTranslationKey() {
        return "text." + ModernIndustrialization.MOD_ID + ".CableTier" + englishShortName;
    }
}
