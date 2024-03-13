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
package aztech.modern_industrialization.proxy;

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlockEntity;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelRenderer;
import aztech.modern_industrialization.blocks.storage.tank.AbstractTankBlockEntity;
import aztech.modern_industrialization.blocks.storage.tank.TankRenderer;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.machines.models.ForwardingCasingBakedModel;
import aztech.modern_industrialization.machines.models.MachineBakedModel;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.models.MachineRendering;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.UnsidedPacketHandler;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public @Nullable Player findUser(ItemStack mainHand) {
        if (Minecraft.getInstance().isSameThread()) {
            for (var player : Minecraft.getInstance().level.players()) {
                if (player.getMainHandItem() == mainHand) {
                    return player;
                }
            }
            return null;
        }
        return super.findUser(mainHand);
    }

    @Override
    public void delayNextBlockAttack(Player player) {
        if (player == Minecraft.getInstance().player) {
            // Add a 5 tick delay like vanilla.
            Minecraft.getInstance().gameMode.destroyDelay = 5;
        }
    }

    @Override
    public boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    @Override
    public List<Component> getFluidTooltip(FluidVariant variant) {
        return FluidVariantRendering.getTooltip(variant);
    }

    @Override
    public void registerUnsidedPacket(ResourceLocation identifier, UnsidedPacketHandler handler) {
        super.registerUnsidedPacket(identifier, handler);

        ClientPlayNetworking.registerGlobalReceiver(identifier, (mc, listener, buf, responseSender) -> {
            mc.execute(handler.handlePacket(mc.player, buf));
        });
    }

    @Override
    public void registerPartTankClient(Block tankBlock, Item tankItem, String materialName, String itemPath,
            BlockEntityType<AbstractTankBlockEntity> blockEntityType, int meanRgb) {
        BlockRenderLayerMap.INSTANCE.putBlock(tankBlock, RenderType.cutout());
        TankRenderer.register(blockEntityType, TextureHelper.getOverlayTextColor(meanRgb));
        BuiltinItemRendererRegistry.INSTANCE.register(tankItem, RenderHelper.BLOCK_AND_ENTITY_RENDERER);
    }

    @Override
    public void registerPartBarrelClient(Block barrelBlock, Item barrelItem, String materialName, String itemPath,
            BlockEntityType<BarrelBlockEntity> blockEntityType, int meanRgb) {
        BarrelRenderer.register(blockEntityType, TextureHelper.getOverlayTextColor(meanRgb));
        BuiltinItemRendererRegistry.INSTANCE.register(barrelItem, RenderHelper.BLOCK_AND_ENTITY_RENDERER);
    }

    @Override
    public MachineMenuCommon createClientMachineMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        return MachineMenuClient.create(syncId, playerInventory, buf);
    }

    @Override
    public BlockState getMachineCasingBlockState(BlockState state, BlockAndTintGetter renderView, BlockPos pos) {
        var be = renderView.getBlockEntity(pos); // Note: not safe to access fields!
        if (!MIConfig.getConfig().enableInterMachineConnectedTextures) {
            // Use the machine's own state, unless we are a hatch or a multiblock controller of course.
            if (!(be instanceof HatchBlockEntity) && !(be instanceof MultiblockMachineBlockEntity)) {
                return state;
            }
        }

        var attachmentView = (RenderAttachedBlockView) renderView;
        if (!(attachmentView.getBlockEntityRenderAttachment(pos) instanceof MachineModelClientData clientData)) {
            // Not a machine's data!
            return state;
        }
        var casing = clientData.casing;
        if (casing == null) {
            // No override, then pull the casing from the machine's baked model.
            var machineModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            while (true) {
                if (machineModel instanceof MachineBakedModel mbm) {
                    casing = mbm.getBaseCasing();
                    break;
                } else if (machineModel instanceof WrapperBakedModel wbm) {
                    machineModel = wbm.getWrappedModel();
                } else {
                    break;
                }
            }
            if (casing == null) {
                // Couldn't find casing... :(
                return state;
            }
        }

        // Pull the block state from the casing model if possible
        var casingModel = MachineRendering.getCasingModel(casing);
        while (true) {
            if (casingModel instanceof ForwardingCasingBakedModel fcbm) {
                return fcbm.getTargetState();
            } else if (casingModel instanceof WrapperBakedModel wbm) {
                casingModel = wbm.getWrappedModel();
            } else {
                break;
            }
        }
        // Couldn't find target state
        return state;
    }
}
