package net.eidee.minecraft.exp_bottling.init;

import net.eidee.minecraft.exp_bottling.network.Networks;
import net.eidee.minecraft.exp_bottling.registry.GuiHandlerRegistry;
import net.eidee.minecraft.exp_bottling.registry.MessageRegistry;
import net.eidee.minecraft.exp_bottling.registry.TileEntityRegistry;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy
{
    public void preInit( FMLPreInitializationEvent event )
    {
        Networks.init();
        MessageRegistry.register();
        TileEntityRegistry.register();
        GuiHandlerRegistry.register();
    }

    public void init( FMLInitializationEvent event )
    {
    }
}
