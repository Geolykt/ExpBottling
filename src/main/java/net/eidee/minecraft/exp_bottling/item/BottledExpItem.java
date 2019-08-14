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

package net.eidee.minecraft.exp_bottling.item;

import static net.eidee.minecraft.exp_bottling.ExpBottling.MOD_ID;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.exp_bottling.util.ExperienceUtil;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottledExpItem
    extends Item
{
    private static final String TAG_EXPERIENCE = ( MOD_ID + ":Exp" );

    public static void setTagExperience( ItemStack stack, int expValue )
    {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if ( tagCompound == null )
        {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound( tagCompound );
        }
        tagCompound.setInteger( TAG_EXPERIENCE, expValue );
    }

    public static int getTagExperience( ItemStack stack )
    {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag != null )
        {
            return tag.getInteger( TAG_EXPERIENCE );
        }
        return 0;
    }

    @Override
    public ItemStack onItemUseFinish( ItemStack stack, World worldIn, EntityLivingBase entityLiving )
    {
        EntityPlayer entityplayer = entityLiving instanceof EntityPlayer ? ( EntityPlayer )entityLiving : null;

        if ( entityplayer == null || !entityplayer.capabilities.isCreativeMode )
        {
            stack.shrink( 1 );
        }

        if ( entityplayer instanceof EntityPlayerMP )
        {
            CriteriaTriggers.CONSUME_ITEM.trigger( ( EntityPlayerMP )entityplayer, stack );
        }

        if ( !worldIn.isRemote && entityplayer != null )
        {
            int exp = getTagExperience( stack );
            if ( exp > 0 )
            {
                ExperienceUtil.addExpToPlayer( entityplayer, exp );
            }
        }

        if ( entityplayer != null )
        {
            entityplayer.addStat( StatList.getObjectUseStats( this ) );
        }

        if ( entityplayer == null || !entityplayer.capabilities.isCreativeMode )
        {
            if ( stack.isEmpty() )
            {
                return new ItemStack( net.minecraft.init.Items.GLASS_BOTTLE );
            }

            if ( entityplayer != null )
            {
                entityplayer.inventory.addItemStackToInventory( new ItemStack( Items.GLASS_BOTTLE ) );
            }
        }

        return stack;
    }

    @Override
    public int getMaxItemUseDuration( ItemStack stack )
    {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction( ItemStack stack )
    {
        return EnumAction.DRINK;
    }

    @Override
    public ActionResult< ItemStack > onItemRightClick( World worldIn, EntityPlayer playerIn, EnumHand handIn )
    {
        playerIn.setActiveHand( handIn );
        return new ActionResult<>( EnumActionResult.SUCCESS, playerIn.getHeldItem( handIn ) );
    }

    @Override
    @SideOnly( Side.CLIENT )
    public void addInformation( ItemStack stack, @Nullable World worldIn, List< String > tooltip, ITooltipFlag flagIn )
    {
        if ( !stack.isEmpty() )
        {
            int exp = getTagExperience( stack );
            if ( exp > 0 )
            {
                tooltip.add( I18n.format( "item.exp_bottling.bottled_exp.tooltip.0", exp ) );
            }
        }
    }

    @Override
    @SideOnly( Side.CLIENT )
    public boolean hasEffect( ItemStack stack )
    {
        return true;
    }
}
