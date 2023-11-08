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
package aztech.modern_industrialization.api.energy;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

public class CableTier implements Comparable<CableTier> {

    public final String englishName;
    public final String name;
    public final long eu;
    public final MachineCasing machineCasing;
    public final String machineHullPath;
    public final Block machineHull;
    public boolean isAE2Compatible = false;

    public final String translationKey;
    public final Component englishNameComponent;

    // TODO(Despacito696969): Move turbines and hatches here
    public CableTier(String englishName, String name, long eu, MIText englishNameComponent, BlockDefinition<Block> machineHullDefinintion) {
        this.englishName = englishName;
        this.name = name;
        this.eu = eu;
        this.translationKey = "text.modern_industrialization.cable_tier_" + name;
        this.englishNameComponent = englishNameComponent.text();
        this.machineCasing = MachineCasings.create(name);
        this.machineHull = machineHullDefinintion.asBlock();
        this.machineHullPath = machineHullDefinintion.path();
    }

    public CableTier setAE2Compatible() {
        isAE2Compatible = true;
        return this;
    }

    /**
     * @return The total EU/t transferred by this tier of network. The same number
     *         is also the internal storage of every node.
     */
    public long getMaxTransfer() {
        return eu * 8;
    }

    public long getEu() {
        return eu;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(CableTier other) {
        return Long.compare(eu, other.eu);
    }
}
