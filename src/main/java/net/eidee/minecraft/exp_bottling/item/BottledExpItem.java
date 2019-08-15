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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottledExpItem
    extends Item
{
    private static final String TAG_EXPERIENCE = ( MOD_ID + ":Exp" );

    public BottledExpItem( Properties properties )
    {
        super( properties );
    }

    public static void setTagExperience( ItemStack stack, int expValue )
    {
        stack.getOrCreateTag().putInt( TAG_EXPERIENCE, expValue );
    }

    public static int getTagExperience( ItemStack stack )
    {
        CompoundNBT tag = stack.getTag();
        if ( tag != null )
        {
            return tag.getInt( TAG_EXPERIENCE );
        }
        return 0;
    }

    @Override
    public ItemStack onItemUseFinish( ItemStack stack, World worldIn, LivingEntity entityLiving )
    {
        PlayerEntity player = entityLiving instanceof PlayerEntity ? ( PlayerEntity )entityLiving : null;
        if ( player == null || !player.abilities.isCreativeMode )
        {
            stack.shrink( 1 );
        }

        if ( player instanceof ServerPlayerEntity )
        {
            CriteriaTriggers.CONSUME_ITEM.trigger( ( ServerPlayerEntity )player, stack );
        }

        if ( !worldIn.isRemote && player != null )
        {
            int exp = getTagExperience( stack );
            if ( exp > 0 )
            {
                ExperienceUtil.addExpToPlayer( player, exp );
            }
        }

        if ( player != null )
        {
            player.addStat( Stats.ITEM_USED.get( this ) );
        }

        if ( player == null || !player.abilities.isCreativeMode )
        {
            if ( stack.isEmpty() )
            {
                return new ItemStack( net.minecraft.item.Items.GLASS_BOTTLE );
            }

            if ( player != null )
            {
                player.inventory.addItemStackToInventory( new ItemStack( net.minecraft.item.Items.GLASS_BOTTLE ) );
            }
        }

        return stack;
    }

    @Override
    public int getUseDuration( ItemStack stack )
    {
        return 32;
    }

    @Override
    public UseAction getUseAction( ItemStack stack )
    {
        return UseAction.DRINK;
    }

    @Override
    public ActionResult< ItemStack > onItemRightClick( World worldIn, PlayerEntity playerIn, Hand handIn )
    {
        playerIn.setActiveHand( handIn );
        return new ActionResult<>( ActionResultType.SUCCESS, playerIn.getHeldItem( handIn ) );
    }

    @Override
    public void addInformation( ItemStack stack,
                                @Nullable World worldIn,
                                List< ITextComponent > tooltip,
                                ITooltipFlag flagIn )
    {
        if ( !stack.isEmpty() )
        {
            int exp = getTagExperience( stack );
            if ( exp > 0 )
            {
                String msg = I18n.format( "item.exp_bottling.bottled_exp.tooltip.0", exp );
                tooltip.add( new StringTextComponent( msg ) );
            }
        }
    }

    @Override
    public boolean hasEffect( ItemStack stack )
    {
        return true;
    }
}
