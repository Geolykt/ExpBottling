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

package net.eidee.minecraft.exp_bottling.inventory.container;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.exp_bottling.block.Blocks;
import net.eidee.minecraft.exp_bottling.item.BottledExpItem;
import net.eidee.minecraft.exp_bottling.item.Items;
import net.eidee.minecraft.exp_bottling.util.ExperienceUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExpBottlingMachineContainer
    extends Container
{
    private final IInventory inputInventory;
    private final IInventory outputInventory;
    private final InventoryPlayer playerInventory;
    private final World world;
    private final BlockPos pos;
    private int expValue;

    public ExpBottlingMachineContainer( InventoryPlayer playerInventory, World world, BlockPos pos )
    {
        this.inputInventory = new InventoryBasic( "ExpBottling", false, 1 )
        {
            @Override
            public void markDirty()
            {
                super.markDirty();
                onCraftMatrixChanged( this );
            }
        };
        this.outputInventory = new InventoryCraftResult();
        this.playerInventory = playerInventory;
        this.world = world;
        this.pos = pos;
        this.inputInventory.openInventory( playerInventory.player );

        addSlotToContainer( new Slot( this.inputInventory, 0, 17, 37 )
        {
            @Override
            public boolean isItemValid( ItemStack stack )
            {
                return stack.getItem() == net.minecraft.init.Items.GLASS_BOTTLE;
            }
        } );

        addSlotToContainer( new Slot( this.outputInventory, 0, 17, 78 )
        {
            @Override
            public boolean isItemValid( ItemStack stack )
            {
                return false;
            }

            @Override
            public int getSlotStackLimit()
            {
                return 1;
            }

            @Override
            public ItemStack onTake( EntityPlayer thePlayer, ItemStack stack )
            {
                onCraftMatrixChanged( outputInventory );
                return super.onTake( thePlayer, stack );
            }
        } );

        for ( int i = 0; i < 3; ++i )
        {
            for ( int j = 0; j < 9; ++j )
            {
                addSlotToContainer( new Slot( playerInventory, j + i * 9 + 9, 38 + j * 18, 122 + i * 18 ) );
            }
        }

        for ( int i = 0; i < 9; ++i )
        {
            addSlotToContainer( new Slot( playerInventory, i, 38 + i * 18, 180 ) );
        }
    }


    public void setBottlingExp( int expValue )
    {
        this.expValue = Math.max( expValue, 0 );
        onCraftMatrixChanged( this.inputInventory );
    }

    public boolean takeBottledExp( int dragType, ClickType clickTypeIn, EntityPlayer player )
    {
        if ( !inputInventory.getStackInSlot( 0 ).isEmpty() )
        {
            int playerExp = ExperienceUtil.getPlayerExp( player );
            ItemStack stackInSlot1 = outputInventory.getStackInSlot( 0 );
            ItemStack copy = stackInSlot1.copy();
            int tagExp = BottledExpItem.getTagExperience( stackInSlot1 );
            if ( tagExp > 0 && playerExp >= tagExp )
            {
                ItemStack slotClick = slotClick( 1, dragType, clickTypeIn, player );
                if ( slotClick.getItem() == copy.getItem() &&
                     Objects.equals( slotClick.getTagCompound(), copy.getTagCompound() ) )
                {
                    if ( !player.world.isRemote )
                    {
                        ExperienceUtil.removeExpFromPlayer( player, tagExp );
                    }
                    inputInventory.decrStackSize( 0, 1 );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canInteractWith( EntityPlayer playerIn )
    {
        if ( world.getBlockState( pos ).getBlock() != Blocks.EXP_BOTTLING_MACHINE )
        {
            return false;
        }
        else
        {
            return playerIn.getDistanceSq( ( double )pos.getX() + 0.5D,
                                           ( double )pos.getY() + 0.5D,
                                           ( double )pos.getZ() + 0.5D ) <= 64.0D;
        }
    }

    @Override
    public void onContainerClosed( EntityPlayer playerIn )
    {
        super.onContainerClosed( playerIn );
        if ( !world.isRemote )
        {
            clearContainer( playerIn, playerIn.world, inputInventory );
        }
    }

    @Override
    public ItemStack transferStackInSlot( EntityPlayer playerIn, int index )
    {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get( index );
        if ( slot != null && slot.getHasStack() )
        {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            if ( index == 0 || index == 1 )
            {
                if ( !mergeItemStack( stackInSlot, 2, 38, true ) )
                {
                    return ItemStack.EMPTY;
                }
            }
            else if ( !mergeItemStack( stackInSlot, 0, 1, false ) )
            {
                return ItemStack.EMPTY;
            }

            if ( stackInSlot.isEmpty() )
            {
                slot.putStack( ItemStack.EMPTY );
            }
            else
            {
                slot.onSlotChanged();
            }

            if ( stack.getCount() == stackInSlot.getCount() )
            {
                return ItemStack.EMPTY;
            }

            slot.onTake( playerIn, stack );
        }

        return stack;
    }

    @Override
    public void onCraftMatrixChanged( IInventory inventoryIn )
    {
        boolean flag = expValue > 0 && !inputInventory.getStackInSlot( 0 ).isEmpty();
        if ( flag )
        {
            EntityPlayer player = playerInventory.player;
            flag = ExperienceUtil.getPlayerExp( player ) >= expValue;
            if ( flag )
            {
                ItemStack stack = new ItemStack( Items.BOTTLED_EXP );
                BottledExpItem.setTagExperience( stack, expValue );
                outputInventory.setInventorySlotContents( 0, stack );
            }
        }

        if ( !flag )
        {
            outputInventory.setInventorySlotContents( 1, ItemStack.EMPTY );
        }
        detectAndSendChanges();
    }

    @Override
    public ItemStack slotClick( int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player )
    {
        if ( slotId == 1 )
        {
            if ( clickTypeIn == ClickType.QUICK_MOVE )
            {
                ItemStack item = transferStackInSlot( player, slotId );
                detectAndSendChanges();
                return item;
            }
            else if ( clickTypeIn != ClickType.PICKUP )
            {
                return ItemStack.EMPTY;
            }
        }
        return super.slotClick( slotId, dragType, clickTypeIn, player );
    }

    @Override
    public boolean canMergeSlot( ItemStack stack, Slot slotIn )
    {
        return slotIn.slotNumber == 0;
    }

    @Override
    public boolean canDragIntoSlot( Slot slotIn )
    {
        return slotIn.slotNumber == 0;
    }
}
