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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.exp_bottling.tileentity.ExpBottlingMachineTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExpBottlingMachineBlock
    extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ExpBottlingMachineBlock( Properties properties )
    {
        super( properties );
    }

    @Override
    public VoxelShape getCollisionShape( BlockState p_220071_1_,
                                         IBlockReader p_220071_2_,
                                         BlockPos p_220071_3_,
                                         ISelectionContext p_220071_4_ )
    {
        return VoxelShapes.fullCube();
    }

    @Override
    protected void fillStateContainer( StateContainer.Builder< Block, BlockState > builder )
    {
        builder.add( FACING );
    }

    @Override
    public BlockState getStateForPlacement( BlockItemUseContext context )
    {
        return getDefaultState().with( FACING,
                                       context.getPlacementHorizontalFacing()
                                              .getOpposite() );
    }

    @Override
    public boolean hasTileEntity( BlockState state )
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity( BlockState state, IBlockReader world )
    {
        return new ExpBottlingMachineTileEntity();
    }

    @Override
    public ActionResultType func_225533_a_( BlockState state,
                                            World world,
                                            BlockPos pos,
                                            PlayerEntity player,
                                            Hand hand,
                                            BlockRayTraceResult rayTraceResult )
    {
        if ( !world.isRemote() )
        {
            TileEntity tileEntity = world.getTileEntity( pos );
            if ( tileEntity instanceof ExpBottlingMachineTileEntity )
            {
                ExpBottlingMachineTileEntity bottlingMachine = ( ExpBottlingMachineTileEntity )tileEntity;
                if ( player instanceof ServerPlayerEntity )
                {
                    NetworkHooks.openGui( ( ServerPlayerEntity )player, bottlingMachine );
                }
            }
        }
        return ActionResultType.SUCCESS;
    }
}
