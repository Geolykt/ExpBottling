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
import net.eidee.minecraft.exp_bottling.world.inventory.ExpBottlingMachineMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ExpBottlingMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MenuTypeInitializer {
  @SubscribeEvent
  public static void registerContainerType(RegistryEvent.Register<MenuType<?>> event) {
    IForgeRegistry<MenuType<?>> registry = event.getRegistry();
    MenuType<?> menuType;

    menuType =
        new MenuType<>(ExpBottlingMachineMenu::new)
            .setRegistryName(Identifies.EXP_BOTTLING_MACHINE);
    registry.register(menuType);
  }
}
