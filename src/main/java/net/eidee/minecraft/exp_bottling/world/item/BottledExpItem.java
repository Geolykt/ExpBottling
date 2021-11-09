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

package net.eidee.minecraft.exp_bottling.world.item;

import static net.eidee.minecraft.exp_bottling.ExpBottlingMod.MOD_ID;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BottledExpItem extends Item {
  private static final int DRINK_DURATION = 32;
  private static final String TAG_EXP = (MOD_ID + ":Exp");

  public BottledExpItem(Item.Properties properties) {
    super(properties);
  }

  public static void setTagExp(ItemStack stack, int expValue) {
    stack.getOrCreateTag().putInt(TAG_EXP, expValue);
  }

  public static int getTagExp(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag != null ? tag.getInt(TAG_EXP) : 0;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    Player player = entity instanceof Player ? (Player) entity : null;
    if (player instanceof ServerPlayer sp) {
      CriteriaTriggers.CONSUME_ITEM.trigger(sp, stack);
    }

    if (!level.isClientSide() && player != null) {
      int exp = getTagExp(stack);
      if (exp > 0) {
        player.giveExperiencePoints(exp);
      }
    }

    if (player != null) {
      player.awardStat(Stats.ITEM_USED.get(this));
      if (!player.getAbilities().instabuild) {
        stack.shrink(1);
      }
    }

    if (player == null || !player.getAbilities().instabuild) {
      if (stack.isEmpty()) {
        return new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE);
      }

      if (player != null) {
        player.getInventory().add(new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE));
      }
    }

    return stack;
  }

  @Override
  public int getUseDuration(ItemStack stack) {
    return DRINK_DURATION;
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    return UseAnim.DRINK;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    return ItemUtils.startUsingInstantly(level, player, hand);
  }

  @Override
  public void appendHoverText(
      ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn) {
    if (!stack.isEmpty()) {
      int exp = getTagExp(stack);
      if (exp > 0) {
        Component expComponent = new TextComponent(String.format("%,d", exp));
        tooltip.add(new TranslatableComponent("item.exp_bottling.bottled_exp.tooltip.0", expComponent));
      }
    }
  }

  @Override
  public boolean isFoil(ItemStack stack) {
    return true;
  }
}
