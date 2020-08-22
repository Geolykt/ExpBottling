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

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.exp_bottling.inventory.container.ExpBottlingMachineContainer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExpBottlingMachineBlock
    extends Block
{
    private static final ITextComponent CONTAINER_NAME;

    static
    {
        CONTAINER_NAME = new TranslationTextComponent( "container.exp_bottling.exp_bottling_machine" );
    }

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
        return getDefaultState().with( FACING, context.getPlacementHorizontalFacing().getOpposite() );
    }

    @Override
    public ActionResultType onBlockActivated( BlockState state,
                                              World worldIn,
                                              BlockPos pos,
                                              PlayerEntity player,
                                              Hand handIn,
                                              BlockRayTraceResult hit )
    {
        if ( worldIn.isRemote() )
        {
            return ActionResultType.SUCCESS;
        }
        player.openContainer( state.getContainer( worldIn, pos ) );
        return ActionResultType.CONSUME;
    }

    @Override
    public INamedContainerProvider getContainer( BlockState state, World worldIn, BlockPos pos )
    {
        return new SimpleNamedContainerProvider( ( id, playerInventory, playerEntity ) -> {
            return new ExpBottlingMachineContainer( id, playerInventory, IWorldPosCallable.of( worldIn, pos ) );
        }, CONTAINER_NAME );
    }
}
