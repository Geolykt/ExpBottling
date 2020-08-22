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

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.exp_bottling.block.Blocks;
import net.eidee.minecraft.exp_bottling.item.BottledExpItem;
import net.eidee.minecraft.exp_bottling.item.Items;
import net.eidee.minecraft.exp_bottling.util.ExperienceUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExpBottlingMachineContainer
    extends Container
{
    private final IInventory inputInventory;
    private final IInventory outputInventory;
    private final PlayerInventory playerInventory;
    private final IWorldPosCallable worldPosCallable;
    private int expValue;

    public ExpBottlingMachineContainer( int id, PlayerInventory playerInventory )
    {
        this( id, playerInventory, IWorldPosCallable.DUMMY );
    }

    public ExpBottlingMachineContainer( int id, PlayerInventory playerInventory, IWorldPosCallable worldPosCallable )
    {
        super( ContainerTypes.EXP_BOTTLING_MACHINE, id );
        this.inputInventory = new Inventory( 1 )
        {
            @Override
            public void markDirty()
            {
                super.markDirty();
                onCraftMatrixChanged( this );
            }
        };
        this.outputInventory = new CraftResultInventory();
        this.playerInventory = playerInventory;
        this.worldPosCallable = worldPosCallable;
        this.inputInventory.openInventory( playerInventory.player );

        addSlot( new Slot( this.inputInventory, 0, 17, 37 )
        {
            @Override
            public boolean isItemValid( ItemStack stack )
            {
                return stack.getItem() == net.minecraft.item.Items.GLASS_BOTTLE;
            }
        } );

        addSlot( new Slot( this.outputInventory, 0, 17, 78 )
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
            public ItemStack onTake( PlayerEntity thePlayer, ItemStack stack )
            {
                onCraftMatrixChanged( outputInventory );
                return super.onTake( thePlayer, stack );
            }
        } );

        for ( int i = 0; i < 3; ++i )
        {
            for ( int j = 0; j < 9; ++j )
            {
                addSlot( new Slot( playerInventory, j + i * 9 + 9, 38 + j * 18, 122 + i * 18 ) );
            }
        }

        for ( int i = 0; i < 9; ++i )
        {
            addSlot( new Slot( playerInventory, i, 38 + i * 18, 180 ) );
        }
    }

    public void setBottlingExp( int expValue )
    {
        this.expValue = Math.max( expValue, 0 );
        onCraftMatrixChanged( this.inputInventory );
    }

    @Override
    public boolean canInteractWith( PlayerEntity playerIn )
    {
        return isWithinUsableDistance( worldPosCallable, playerIn, Blocks.EXP_BOTTLING_MACHINE );
    }

    @Override
    public void onContainerClosed( PlayerEntity playerIn )
    {
        super.onContainerClosed( playerIn );
        worldPosCallable.consume( ( world, blockPos ) -> {
            clearContainer( playerIn, world, this.inputInventory );
        } );
    }

    @Override
    public ItemStack transferStackInSlot( PlayerEntity playerIn, int index )
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
            PlayerEntity player = playerInventory.player;
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
            outputInventory.clear();
        }
        detectAndSendChanges();
    }

    @Override
    public ItemStack slotClick( int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player )
    {
        if ( slotId == 1 )
        {
            ItemStack item = ItemStack.EMPTY;

            if ( !inputInventory.getStackInSlot( 0 ).isEmpty() )
            {
                if ( clickTypeIn == ClickType.QUICK_MOVE )
                {
                    item = transferStackInSlot( player, slotId );
                }
                else if ( clickTypeIn == ClickType.PICKUP )
                {
                    ItemStack before = playerInventory.getItemStack().copy();
                    ItemStack result = super.slotClick( slotId, dragType, clickTypeIn, player );
                    ItemStack after = playerInventory.getItemStack();
                    if ( !ItemStack.areItemStacksEqual( before, after ) )
                    {
                        item = result;
                    }
                }
            }

            if ( !item.isEmpty() )
            {
                int playerExp = ExperienceUtil.getPlayerExp( player );
                int tagExp = BottledExpItem.getTagExperience( item );
                if ( tagExp > 0 && playerExp >= tagExp )
                {
                    if ( !player.world.isRemote() )
                    {
                        ExperienceUtil.removeExpFromPlayer( player, tagExp );
                    }
                    inputInventory.decrStackSize( 0, 1 );
                }
            }

            detectAndSendChanges();
            return item;
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
