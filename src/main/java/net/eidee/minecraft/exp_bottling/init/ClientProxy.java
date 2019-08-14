package net.eidee.minecraft.exp_bottling.init;

import net.eidee.minecraft.exp_bottling.registry.GuiHandlerRegistry;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy
    extends CommonProxy
{
    public void preInit( FMLPreInitializationEvent event )
    {
        super.preInit( event );
        GuiHandlerRegistry.register();
    }
}
