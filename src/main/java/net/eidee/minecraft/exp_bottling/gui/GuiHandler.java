package net.eidee.minecraft.exp_bottling.gui;

import javax.annotation.Nullable;

import net.eidee.minecraft.exp_bottling.inventory.container.ExpBottlingMachineContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler
    implements IGuiHandler
{
    public static final int GUI_EXP_BOTTLING_MACHINE = 0;

    @Nullable
    @Override
    public Object getServerGuiElement( int ID, EntityPlayer player, World world, int x, int y, int z )
    {
        if ( ID == GUI_EXP_BOTTLING_MACHINE )
        {
            return new ExpBottlingMachineContainer( player.inventory, world, new BlockPos( x, y, z ) );
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement( int ID, EntityPlayer player, World world, int x, int y, int z )
    {
        if ( ID == GUI_EXP_BOTTLING_MACHINE )
        {
            ExpBottlingMachineContainer container;
            container = new ExpBottlingMachineContainer( player.inventory, world, new BlockPos( x, y, z ) );
            return new ExpBottlingMachineScreen( container, player.inventory );
        }
        return null;
    }
}
