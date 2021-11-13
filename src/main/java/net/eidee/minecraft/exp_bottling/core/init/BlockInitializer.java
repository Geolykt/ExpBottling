/*
 * MIT License
 *
 * Copyright (c) 2020 EideeHi
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

package net.eidee.minecraft.exp_bottling.core.init;

import net.eidee.minecraft.exp_bottling.ExpBottlingMod;
import net.eidee.minecraft.exp_bottling.core.constants.Identifies;
import net.eidee.minecraft.exp_bottling.world.level.block.Blocks;
import net.eidee.minecraft.exp_bottling.world.level.block.ExpBottlingMachineBlock;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

/** This class is block initializer. */
@Mod.EventBusSubscriber(modid = ExpBottlingMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockInitializer {

  /**
   * This method is block initializer.
   *
   * @param event block register event
   */
  @SubscribeEvent
  public static void registerBlock(RegistryEvent.Register<Block> event) {
    IForgeRegistry<Block> registry = event.getRegistry();

    Block block;
    Properties properties;

    properties =
        Properties.of(Material.METAL).requiresCorrectToolForDrops().strength(3.0F).noOcclusion();
    block =
        new ExpBottlingMachineBlock(properties).setRegistryName(Identifies.EXP_BOTTLING_MACHINE);
    registry.register(block);
  }

  /**
   * This method is block item initializer.
   *
   * @param event item register event
   */
  @SubscribeEvent
  public static void registerItem(RegistryEvent.Register<Item> event) {
    IForgeRegistry<Item> registry = event.getRegistry();

    Item item;
    Item.Properties prop;

    prop = new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS);
    item =
        new BlockItem(Blocks.EXP_BOTTLING_MACHINE, prop)
            .setRegistryName(Identifies.EXP_BOTTLING_MACHINE);
    registry.register(item);
  }

  @OnlyIn(Dist.CLIENT)
  public static void registerRenderType() {
    ItemBlockRenderTypes.setRenderLayer(Blocks.EXP_BOTTLING_MACHINE, RenderType.cutout());
  }
}
