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

package net.eidee.minecraft.exp_bottling.world.level.block;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.exp_bottling.world.inventory.ExpBottlingMachineMenu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExpBottlingMachineBlock extends HorizontalDirectionalBlock {
  private static final Component CONTAINER_TITLE =
      new TranslatableComponent("container.exp_bottling.exp_bottling_machine");

  public ExpBottlingMachineBlock(Properties properties) {
    super(properties);
  }

  @Nullable
  @Override
  public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
    return new SimpleMenuProvider(
        (id, inventory, player) ->
            new ExpBottlingMachineMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
        CONTAINER_TITLE);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
    return Shapes.block();
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public boolean isPathfindable(
      BlockState state, BlockGetter getter, BlockPos pos, PathComputationType type) {
    return false;
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext ctx) {
    return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (level.isClientSide) {
      return InteractionResult.SUCCESS;
    }
    player.openMenu(state.getMenuProvider(level, pos));
    // player.awardStat(Stats.INTERACT_WITH_ANVIL);
    return InteractionResult.CONSUME;
  }
}
