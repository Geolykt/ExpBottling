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

package net.eidee.minecraft.exp_bottling.block;

import static net.eidee.minecraft.exp_bottling.gui.GuiHandler.GUI_EXP_BOTTLING_MACHINE;

import java.util.Random;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.exp_bottling.ExpBottling;
import net.eidee.minecraft.exp_bottling.tileentity.ExpBottlingMachineTileEntity;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExpBottlingMachineBlock
    extends BlockContainer
{
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public ExpBottlingMachineBlock()
    {
        super( Material.IRON );
        setHardness( 3.0F );
        setResistance( 10.0F );
        setSoundType( SoundType.METAL );
        setDefaultState( blockState.getBaseState().withProperty( FACING, EnumFacing.NORTH ) );
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity( World worldIn, int meta )
    {
        return new ExpBottlingMachineTileEntity();
    }

    @Override
    public Item getItemDropped( IBlockState state, Random rand, int fortune )
    {
        return Item.getItemFromBlock( Blocks.EXP_BOTTLING_MACHINE );
    }

    @Override
    public boolean onBlockActivated( World worldIn,
                                     BlockPos pos,
                                     IBlockState state,
                                     EntityPlayer playerIn,
                                     EnumHand hand,
                                     EnumFacing facing,
                                     float hitX,
                                     float hitY,
                                     float hitZ )
    {
        if ( !worldIn.isRemote )
        {
            TileEntity tileEntity = worldIn.getTileEntity( pos );
            if ( tileEntity instanceof ExpBottlingMachineTileEntity )
            {
                if ( playerIn instanceof EntityPlayerMP )
                {
                    playerIn.openGui( ExpBottling.INSTANCE,
                                      GUI_EXP_BOTTLING_MACHINE,
                                      worldIn,
                                      pos.getX(),
                                      pos.getY(),
                                      pos.getZ() );
                }
            }
        }
        return true;
    }

    @Override
    public IBlockState getStateForPlacement( World world,
                                             BlockPos pos,
                                             EnumFacing facing,
                                             float hitX,
                                             float hitY,
                                             float hitZ,
                                             int meta,
                                             EntityLivingBase placer,
                                             EnumHand hand )
    {
        return getDefaultState().withProperty( FACING, placer.getHorizontalFacing().getOpposite() );
    }

    @Override
    public void onBlockPlacedBy( World worldIn,
                                 BlockPos pos,
                                 IBlockState state,
                                 EntityLivingBase placer,
                                 ItemStack stack )
    {
        worldIn.setBlockState( pos, state.withProperty( FACING, placer.getHorizontalFacing().getOpposite() ), 2 );
    }

    @Override
    public ItemStack getPickBlock( IBlockState state,
                                   RayTraceResult target,
                                   World world,
                                   BlockPos pos,
                                   EntityPlayer player )
    {
        return new ItemStack( Blocks.EXP_BOTTLING_MACHINE );
    }

    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType( IBlockState state )
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public IBlockState getStateFromMeta( int meta )
    {
        EnumFacing enumfacing = EnumFacing.getFront( meta );

        if ( enumfacing.getAxis() == EnumFacing.Axis.Y )
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty( FACING, enumfacing );
    }

    @Override
    public int getMetaFromState( IBlockState state )
    {
        return state.getValue( FACING ).getIndex();
    }

    @Override
    public IBlockState withRotation( IBlockState state, Rotation rot )
    {
        return state.withProperty( FACING, rot.rotate( state.getValue( FACING ) ) );
    }

    @Override
    public IBlockState withMirror( IBlockState state, Mirror mirrorIn )
    {
        return state.withRotation( mirrorIn.toRotation( state.getValue( FACING ) ) );
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer( this, FACING );
    }
}
