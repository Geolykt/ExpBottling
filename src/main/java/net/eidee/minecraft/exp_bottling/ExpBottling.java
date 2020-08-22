/*
 * MIT License
 *
 * Copyright (c) 2019 EideeHi
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

package net.eidee.minecraft.exp_bottling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.eidee.minecraft.exp_bottling.init.BlockInitializer;
import net.eidee.minecraft.exp_bottling.init.NetworkInitializer;
import net.eidee.minecraft.exp_bottling.init.ScreenInitializer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod( ExpBottling.MOD_ID )
public class ExpBottling
{
    private static final Logger logger;

    public static final String MOD_ID = "exp_bottling";

    static
    {
        logger = LogManager.getLogger( MOD_ID );
    }

    public ExpBottling()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener( this::setup );
        FMLJavaModLoadingContext.get().getModEventBus().addListener( this::clientSetup );
    }

    public static Logger logger()
    {
        return logger;
    }

    private void setup( FMLCommonSetupEvent event )
    {
        NetworkInitializer.registerMessage();
    }

    private void clientSetup( FMLClientSetupEvent event )
    {
        ScreenInitializer.registerScreen();
        BlockInitializer.registerRenderType();
    }
}
