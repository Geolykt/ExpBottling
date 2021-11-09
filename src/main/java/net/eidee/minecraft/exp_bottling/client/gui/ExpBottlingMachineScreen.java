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

package net.eidee.minecraft.exp_bottling.client.gui;

import static net.eidee.minecraft.exp_bottling.ExpBottlingMod.MOD_ID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.exp_bottling.client.gui.components.ExpInputField;
import net.eidee.minecraft.exp_bottling.client.gui.components.PlayerHeadIcon;
import net.eidee.minecraft.exp_bottling.network.Networks;
import net.eidee.minecraft.exp_bottling.network.message.SetExpToBeBottled;
import net.eidee.minecraft.exp_bottling.util.ExpUtil;
import net.eidee.minecraft.exp_bottling.world.inventory.ExpBottlingMachineMenu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ExpBottlingMachineScreen extends AbstractContainerScreen<ExpBottlingMachineMenu>
    implements ContainerListener, ExpInputField.Listener {
  private static final ResourceLocation BACKGROUND;

  static {
    BACKGROUND = new ResourceLocation(MOD_ID, "textures/gui/container/exp_bottling_machine.png");
  }

  private final Player player;
  private final BiMap<NumpadLogic, Button> logicButtonBiMap;

  private ExpInputField expToBeBottled;
  private ExpInputField playerExpAfterBottling;
  private boolean isInputFieldDirty;
  private boolean ignoreClickEvent;
  private int prevExpValue;

  public ExpBottlingMachineScreen(
      ExpBottlingMachineMenu menu, Inventory inventory, Component component) {
    super(menu, inventory, component);
    this.player = inventory.player;
    this.logicButtonBiMap = HashBiMap.create();
    this.imageWidth = 236;
    this.imageHeight = 204;
  }

  private void buttonHandle(Button button) {
    this.stimulateInput(this.logicButtonBiMap.inverse().get(button));
  }

  private void stimulateInput(NumpadLogic logic) {
    if (!this.expToBeBottled.isFocused() && !this.playerExpAfterBottling.isFocused()) {
      return;
    }

    ExpInputField active;
    ExpInputField inactive;
    if (this.expToBeBottled.isFocused()) {
      active = this.expToBeBottled;
      inactive = this.playerExpAfterBottling;
    } else {
      active = this.playerExpAfterBottling;
      inactive = this.expToBeBottled;
    }

    active.setValue(logic.handleInput(active.getValue()));

    if (!active.getValue().isEmpty() && !inactive.getValue().isEmpty()) {
      inactive.setValue("");
    }
  }

  private void setExpToBeBottled() {
    String value = this.expToBeBottled.getValue();
    int exp = value.isEmpty() ? 0 : Integer.parseInt(value);

    if (exp != this.prevExpValue) {
      this.prevExpValue = exp;
      this.getMenu().setExpToBeBottled(exp);
      Networks.getChannel().sendToServer(new SetExpToBeBottled(exp));
    }
  }

  private void adjustValueOfInactiveInput() {
    int currentExp = ExpUtil.getCurrentExp(this.player);
    if (this.expToBeBottled.isFocused()) {
      String value = this.expToBeBottled.getValue();
      if (!value.isEmpty()) {
        int postBottlingExp = Integer.parseInt(value);
        int expToBeBottled = currentExp - postBottlingExp;
        this.playerExpAfterBottling.setValue(String.valueOf(expToBeBottled));
      } else if (!this.playerExpAfterBottling.getValue().isEmpty()) {
        this.playerExpAfterBottling.setValue("");
      }
    } else if (this.playerExpAfterBottling.isFocused()) {
      String value = this.playerExpAfterBottling.getValue();
      if (!value.isEmpty()) {
        int expToBeBottled = Integer.parseInt(value);
        int postBottlingExp = currentExp - expToBeBottled;
        this.expToBeBottled.setValue(String.valueOf(postBottlingExp));
      } else if (!this.expToBeBottled.getValue().isEmpty()) {
        this.expToBeBottled.setValue("");
      }
    }
  }

  @Override
  protected void init() {
    super.init();

    this.titleLabelX = (this.imageWidth - this.font.width(this.getTitle())) / 2;
    this.inventoryLabelX = 38;
    this.inventoryLabelY = this.imageHeight - 94;

    this.logicButtonBiMap.clear();
    this.isInputFieldDirty = false;

    this.addRenderableOnly(new PlayerHeadIcon(this.player, this.leftPos + 142, this.topPos + 27, 16, 16));

    this.expToBeBottled =
        this.addRenderableWidget(
            new ExpInputField(this.font, this.leftPos + 48, this.topPos + 47, 90, 16, this));
    this.playerExpAfterBottling =
        this.addRenderableWidget(
            new ExpInputField(this.font, this.leftPos + 48, this.topPos + 78, 90, 16, this));

    for (NumpadLogic logic : NumpadLogic.values()) {
      int i = logic.getSortOrder();
      int x = this.leftPos + ((i % 3) * 21);
      int y = this.topPos + ((i / 3) * 21);
      Button button =
          this.addRenderableWidget(
              new Button(162 + x, 18 + y, 20, 20, logic.getComponent(), this::buttonHandle));
      this.logicButtonBiMap.put(logic, button);
    }

    this.getMenu().addSlotListener(this);
  }

  @Override
  protected void containerTick() {
    this.expToBeBottled.tick();
    this.playerExpAfterBottling.tick();

    this.adjustValueOfInactiveInput();

    if (this.isInputFieldDirty) {
      this.isInputFieldDirty = false;
      this.setExpToBeBottled();
    }
  }

  @Override
  public void removed() {
    super.removed();
    this.getMenu().removeSlotListener(this);
  }

  @Override
  public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
    if (this.isInputFieldDirty) {
      this.adjustValueOfInactiveInput();
    }

    this.renderBackground(stack);
    this.renderBg(stack, delta, mouseX, mouseY);
    super.render(stack, mouseX, mouseY, delta);
    this.renderTooltip(stack, mouseX, mouseY);
  }

  @Override
  protected void renderBg(PoseStack stack, float delta, int mouseX, int mouseY) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, BACKGROUND);

    this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
  }

  @Override
  protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
    super.renderLabels(stack, mouseX, mouseY);

    int marginX = this.font.width("_");
    String exp = Integer.toString(ExpUtil.getCurrentExp(this.player));
    this.font.draw(
        stack,
        exp,
        48 + 90 - this.font.width(exp) - marginX - 3,
        27 + 16 - this.font.lineHeight - 3,
        0xFFFFFF);
  }

  @Override
  public void onValueChanged(ExpInputField instance) {
    this.isInputFieldDirty = true;
  }

  @Override
  public void onFocusChanged(ExpInputField instance) {
    if (instance.isFocused()) {
      if (instance == this.expToBeBottled) {
        this.playerExpAfterBottling.setFocused(false);
      } else {
        this.expToBeBottled.setFocused(false);
      }
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    if (!this.ignoreClickEvent) {
      if (this.getSlotUnderMouse() == null && this.children().stream().noneMatch(child -> child.isMouseOver(mouseX, mouseY))) {
        this.expToBeBottled.setFocused(false);
        this.playerExpAfterBottling.setFocused(false);
      }
    } else {
      this.ignoreClickEvent = false;
    }
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (this.expToBeBottled.isFocused() || this.playerExpAfterBottling.isFocused()) {
      switch (InputConstants.getKey(keyCode, scanCode).getName()) {
        case "key.keyboard.0", "key.keyboard.keypad.0" -> this.stimulateInput(NumpadLogic.NUMBER_0);
        case "key.keyboard.1", "key.keyboard.keypad.1" -> this.stimulateInput(NumpadLogic.NUMBER_1);
        case "key.keyboard.2", "key.keyboard.keypad.2" -> this.stimulateInput(NumpadLogic.NUMBER_2);
        case "key.keyboard.3", "key.keyboard.keypad.3" -> this.stimulateInput(NumpadLogic.NUMBER_3);
        case "key.keyboard.4", "key.keyboard.keypad.4" -> this.stimulateInput(NumpadLogic.NUMBER_4);
        case "key.keyboard.5", "key.keyboard.keypad.5" -> this.stimulateInput(NumpadLogic.NUMBER_5);
        case "key.keyboard.6", "key.keyboard.keypad.6" -> this.stimulateInput(NumpadLogic.NUMBER_6);
        case "key.keyboard.7", "key.keyboard.keypad.7" -> this.stimulateInput(NumpadLogic.NUMBER_7);
        case "key.keyboard.8", "key.keyboard.keypad.8" -> this.stimulateInput(NumpadLogic.NUMBER_8);
        case "key.keyboard.9", "key.keyboard.keypad.9" -> this.stimulateInput(NumpadLogic.NUMBER_9);
        case "key.keyboard.backspace" -> this.stimulateInput(NumpadLogic.BACKSPACE);
        case "key.keyboard.l" -> this.stimulateInput(NumpadLogic.LEVEL);
        default -> super.keyPressed(keyCode, scanCode, modifiers);
      }
    } else {
      super.keyPressed(keyCode, scanCode, modifiers);
    }

    return true;
  }

  @Override
  public void slotChanged(AbstractContainerMenu menu, int index, ItemStack stack) {
    if (index == 0 || index == 1) {
      if (!stack.isEmpty()) {
        if (!this.expToBeBottled.isFocused() && !this.playerExpAfterBottling.isFocused()) {
          this.expToBeBottled.setFocused(this.expToBeBottled.getValue().isEmpty());
          this.ignoreClickEvent = true;
          return;
        }
      }
      this.adjustValueOfInactiveInput();
    }
  }

  @Override
  public void dataChanged(AbstractContainerMenu menu, int index, int value) {}

  private enum NumpadLogic {
    NUMBER_1(0, "gui.exp_bottling.exp_bottling_machine.button.1") {
      @Override
      public String handleInput(String input) {
        return Objects.equals(input, "0") ? "1" : input + "1";
      }
    },
    NUMBER_2(1, "gui.exp_bottling.exp_bottling_machine.button.2") {
      @Override
      public String handleInput(String input) {
        return Objects.equals(input, "0") ? "2" : input + "2";
      }
    },
    NUMBER_3(2, "gui.exp_bottling.exp_bottling_machine.button.3") {
      @Override
      public String handleInput(String input) {
        return Objects.equals(input, "0") ? "3" : input + "3";
      }
    },
    NUMBER_4(3, "gui.exp_bottling.exp_bottling_machine.button.4") {
      @Override
      public String handleInput(String input) {
        return Objects.equals(input, "0") ? "4" : input + "4";
      }
    },
    NUMBER_5(4, "gui.exp_bottling.exp_bottling_machine.button.5") {
      @Override
      public String handleInput(String input) {
        return Objects.equals(input, "0") ? "5" : input + "5";
      }
    },
    NUMBER_6(5, "gui.exp_bottling.exp_bottling_machine.button.6") {
      @Override
      public String handleInput(String input) {
        return Objects.equals(input, "0") ? "6" : input + "6";
      }
    },
    NUMBER_7(6, "gui.exp_bottling.exp_bottling_machine.button.7") {
      @Override
      public String handleInput(String input) {
        return Objects.equals(input, "0") ? "7" : input + "7";
      }
    },
    NUMBER_8(7, "gui.exp_bottling.exp_bottling_machine.button.8") {
      @Override
      public String handleInput(String input) {
        return Objects.equals(input, "0") ? "8" : input + "8";
      }
    },
    NUMBER_9(8, "gui.exp_bottling.exp_bottling_machine.button.9") {
      @Override
      public String handleInput(String input) {
        return Objects.equals(input, "0") ? "9" : input + "9";
      }
    },
    NUMBER_0(10, "gui.exp_bottling.exp_bottling_machine.button.0") {
      @Override
      public String handleInput(String input) {
        return Objects.equals(input, "0") ? input : input + "0";
      }
    },
    LEVEL(9, "gui.exp_bottling.exp_bottling_machine.button.lv") {
      @Override
      public String handleInput(String input) {
        if (!input.isEmpty()) {
          int level = Mth.clamp(Integer.parseInt(input), 0, 21863);
          if (level > 0) {
            int exp = ExpUtil.getExpToReachLevel(level, 0.0F);
            return Integer.toString(Math.max(exp, 0));
          }
        }
        return "";
      }
    },
    BACKSPACE(11, "gui.exp_bottling.exp_bottling_machine.button.bs") {
      @Override
      public String handleInput(String input) {
        int length = input.length();
        return length > 0 ? input.substring(0, length - 1) : "";
      }
    };

    private final int sortOrder;
    private final Component text;

    NumpadLogic(int sortOrder, String text) {
      this.sortOrder = sortOrder;
      this.text = new TranslatableComponent(text);
    }

    public int getSortOrder() {
      return this.sortOrder;
    }

    public Component getComponent() {
      return this.text;
    }

    public abstract String handleInput(String input);
  }
}
