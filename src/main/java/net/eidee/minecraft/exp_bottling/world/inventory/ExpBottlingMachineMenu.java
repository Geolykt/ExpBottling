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

package net.eidee.minecraft.exp_bottling.world.inventory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.exp_bottling.util.ExpUtil;
import net.eidee.minecraft.exp_bottling.world.item.BottledExpItem;
import net.eidee.minecraft.exp_bottling.world.item.Items;
import net.eidee.minecraft.exp_bottling.world.level.block.Blocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExpBottlingMachineMenu extends AbstractContainerMenu {
  private final Container emptyBottleSlot;
  private final ResultContainer resultSlot;
  private final Player player;
  private final ContainerLevelAccess access;
  private int exp;

  public ExpBottlingMachineMenu(int id, Inventory inventory, ContainerLevelAccess access) {
    super(MenuTypes.EXP_BOTTLING_MACHINE, id);
    this.emptyBottleSlot =
        new SimpleContainer(1) {
          @Override
          public void setChanged() {
            ExpBottlingMachineMenu.this.slotsChanged(this);
          }
        };
    this.resultSlot =
        new ResultContainer() {
          @Override
          public void setChanged() {
            ExpBottlingMachineMenu.this.slotsChanged(this);
          }
        };
    this.player = inventory.player;
    this.access = access;

    this.addSlot(
        new Slot(this.emptyBottleSlot, 0, 17, 37) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return stack.getItem() == net.minecraft.world.item.Items.GLASS_BOTTLE;
          }
        });

    this.addSlot(
        new Slot(this.resultSlot, 1, 17, 78) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return false;
          }

          @Override
          public boolean mayPickup(Player player) {
            if (!player.isCreative()) {
              int exp = BottledExpItem.getTagExp(this.getItem());
              return exp > 0 && exp <= ExpUtil.getCurrentExp(player);
            }
            return true;
          }

          @Override
          public void onTake(Player player, ItemStack stack) {
            if (!player.isCreative()) {
              int exp = BottledExpItem.getTagExp(stack);
              if (exp > 0) {
                player.giveExperiencePoints(-exp);
              }
              ItemStack emptyBottle = ExpBottlingMachineMenu.this.emptyBottleSlot.getItem(0).copy();
              if (!emptyBottle.isEmpty()) {
                emptyBottle.shrink(1);
                ExpBottlingMachineMenu.this.emptyBottleSlot.setItem(0, emptyBottle);
              }
            }
          }
        });

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlot(new Slot(inventory, j + i * 9 + 9, 38 + j * 18, 122 + i * 18));
      }
    }

    for (int i = 0; i < 9; ++i) {
      this.addSlot(new Slot(inventory, i, 38 + i * 18, 180));
    }
  }

  public ExpBottlingMachineMenu(int id, Inventory inventory) {
    this(id, inventory, ContainerLevelAccess.NULL);
  }

  private void createResult() {
    boolean canResultCreate = this.exp > 0 && !this.emptyBottleSlot.getItem(0).isEmpty();
    if (canResultCreate) {
      canResultCreate = ExpUtil.getCurrentExp(this.player) >= this.exp;
      if (canResultCreate) {
        ItemStack result = new ItemStack(Items.BOTTLED_EXP);
        BottledExpItem.setTagExp(result, this.exp);
        this.resultSlot.setItem(0, result);
      }
    }
    if (!canResultCreate) {
      this.resultSlot.setItem(0, ItemStack.EMPTY);
    }
    this.broadcastChanges();
  }

  public void setExpToBeBottled(int exp) {
    this.exp = Math.max(exp, 0);
    this.createResult();
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
    return slot.getSlotIndex() == 0;
  }

  @Override
  public boolean stillValid(Player player) {
    return stillValid(this.access, player, Blocks.EXP_BOTTLING_MACHINE);
  }

  @Override
  public void slotsChanged(Container container) {
    super.slotsChanged(container);
    this.createResult();
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    this.access.execute((level, pos) -> this.clearContainer(player, this.emptyBottleSlot));
  }

  @Override
  public ItemStack quickMoveStack(Player player, int slotIndex) {
    ItemStack stack = ItemStack.EMPTY;
    Slot slot = this.slots.get(slotIndex);
    if (slot.hasItem()) {
      ItemStack stackInSlot = slot.getItem();
      stack = stackInSlot.copy();
      if (slotIndex == 1) {
        if (!this.moveItemStackTo(stackInSlot, 2, 38, true)) {
          return ItemStack.EMPTY;
        }
        slot.onQuickCraft(stackInSlot, stack);
      } else if (slotIndex == 0) {
        if (!this.moveItemStackTo(stackInSlot, 2, 38, true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
        return ItemStack.EMPTY;
      }
      if (stackInSlot.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
      if (stackInSlot.getCount() == stack.getCount()) {
        return ItemStack.EMPTY;
      }
      slot.onTake(player, stack);
    }
    return stack;
  }
}
