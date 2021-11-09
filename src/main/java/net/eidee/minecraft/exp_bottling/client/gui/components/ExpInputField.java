/*
 * MIT License
 *
 * Copyright (c) 2021 EideeHi
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

package net.eidee.minecraft.exp_bottling.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.regex.Pattern;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class ExpInputField extends AbstractWidget {
  private static final Pattern PATTERN = Pattern.compile("^-?\\d+$");

  private final Font font;
  private final Listener listener;

  private String value = "";
  private int frame = 0;

  public ExpInputField(Font font, int x, int y, int width, int height, Listener listener) {
    super(x, y, width, height, TextComponent.EMPTY);
    this.font = font;
    this.listener = listener;
  }

  private int getTextColor() {
    if (this.getValue().startsWith("-")) {
      return this.isFocused() ? 0xE02020 : 0xE09090;
    } else {
      return this.isFocused() ? 0xFFFFFF : 0xA0A0A0;
    }
  }

  public String getValue() {
    return this.value;
  }

  public void setValue(String value) {
    if (value.isEmpty() || PATTERN.matcher(value).matches()) {
      String newValue = value;
      if (!newValue.isEmpty()) {
        if (Long.parseLong(newValue) > Integer.MAX_VALUE) {
          newValue = Integer.toString(Integer.MAX_VALUE);
        } else {
          newValue = Integer.toString(Integer.parseInt(newValue));
        }
      }

      if (!this.value.equals(newValue)) {
        this.value = newValue;
        this.listener.onValueChanged(this);
      }
    }
  }

  @Override
  public void setFocused(boolean focused) {
    super.setFocused(focused);
  }

  public void tick() {
    this.frame++;
  }

  @Override
  protected void onFocusedChanged(boolean focused) {
    super.onFocusedChanged(focused);
    if (focused) {
      this.frame = 0;
      this.setValue("");
    }
    this.listener.onFocusChanged(this);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (this.active && this.visible) {
      if (this.isValidClickButton(mouseButton)) {
        boolean flag = this.clicked(mouseX, mouseY);
        if (flag) {
          this.changeFocus(false);
          this.onClick(mouseX, mouseY);
          return true;
        }
      }
    }
    return false;
  }

  @ParametersAreNonnullByDefault
  @Override
  public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
    String text = this.getValue();
    int marginX = 0;
    if (this.isFocused()) {
      if (this.frame / 6 % 2 == 0) {
        text += "_";
      } else {
        marginX = this.font.width("_");
      }
    } else {
      marginX = this.font.width("_");
    }

    this.font.draw(
        stack,
        text,
        this.x + this.width - this.font.width(text) - marginX - 3,
        this.y + this.height - this.font.lineHeight - 3,
        this.getTextColor());
  }

  @Override
  public void updateNarration(NarrationElementOutput output) {
    output.add(
        NarratedElementType.TITLE,
        new TranslatableComponent("narration.edit_box", this.getValue()));
  }

  @ParametersAreNonnullByDefault
  public interface Listener {
    void onValueChanged(ExpInputField instance);

    void onFocusChanged(ExpInputField instance);
  }
}
