/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.compat.kubejs.energy;

import aztech.modern_industrialization.api.energy.CableTierRegistry;
import dev.latvian.mods.kubejs.event.EventJS;
import java.util.ArrayList;
import java.util.Map;

public class ModifyCableTiersEventJS extends EventJS {
    public void register(String tierEnglishNameShort, String tierId, long eu, String tierEnglishFullname, String casingEnglishName, String casingId,
            Map<String, String> options) {
        CableTierRegistry.unprocessedTierList.add(
                new CableTierRegistry.CableTierEntry(tierEnglishNameShort, tierId, eu, tierEnglishFullname, casingEnglishName, casingId, options));
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
