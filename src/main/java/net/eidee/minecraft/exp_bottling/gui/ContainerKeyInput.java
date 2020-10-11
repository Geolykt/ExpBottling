/*
 * MIT License
 *
 * Copyright (c) 2020 EideeHi
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
package net.eidee.minecraft.exp_bottling.gui;

import org.lwjgl.glfw.GLFW;

import net.eidee.minecraft.exp_bottling.gui.ExpBottlingMachineScreen.ButtonLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardKeyPressedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(value = Dist.CLIENT)
public class ContainerKeyInput 
{
    @SubscribeEvent(receiveCanceled = false)
    public static void keyPress(KeyboardKeyPressedEvent e)
    {
        if (e.getGui() instanceof ExpBottlingMachineScreen) 
        {
            if (e.getResult() != Result.DENY) 
            {
                ExpBottlingMachineScreen screen = (ExpBottlingMachineScreen) e.getGui();
                if (screen.inputTime + 10 > System.currentTimeMillis())
                {
                    return;
                }
                
                boolean performed = true;
                switch (e.getKeyCode())
                {
                case GLFW.GLFW_KEY_0:
                case GLFW.GLFW_KEY_KP_0:
                    screen.stimulateInput(ButtonLogic.NUMBER_0);
                    break;
                case GLFW.GLFW_KEY_1:
                case GLFW.GLFW_KEY_KP_1:
                    screen.stimulateInput(ButtonLogic.NUMBER_1);
                    break;
                case GLFW.GLFW_KEY_2:
                case GLFW.GLFW_KEY_KP_2:
                    screen.stimulateInput(ButtonLogic.NUMBER_2);
                    break;
                case GLFW.GLFW_KEY_3:
                case GLFW.GLFW_KEY_KP_3:
                    screen.stimulateInput(ButtonLogic.NUMBER_3);
                    break;
                case GLFW.GLFW_KEY_4:
                case GLFW.GLFW_KEY_KP_4:
                    screen.stimulateInput(ButtonLogic.NUMBER_4);
                    break;
                case GLFW.GLFW_KEY_5:
                case GLFW.GLFW_KEY_KP_5:
                    screen.stimulateInput(ButtonLogic.NUMBER_5);
                    break;
                case GLFW.GLFW_KEY_6:
                case GLFW.GLFW_KEY_KP_6:
                    screen.stimulateInput(ButtonLogic.NUMBER_6);
                    break;
                case GLFW.GLFW_KEY_7:
                case GLFW.GLFW_KEY_KP_7:
                    screen.stimulateInput(ButtonLogic.NUMBER_7);
                    break;
                case GLFW.GLFW_KEY_8:
                case GLFW.GLFW_KEY_KP_8:
                    screen.stimulateInput(ButtonLogic.NUMBER_8);
                    break;
                case GLFW.GLFW_KEY_9:
                case GLFW.GLFW_KEY_KP_9:
                    screen.stimulateInput(ButtonLogic.NUMBER_9);
                    break;
                case GLFW.GLFW_KEY_BACKSPACE:
                    screen.stimulateInput(ButtonLogic.BACKSPACE);
                    break;
                default:
                    performed = false;
                    break;
                }
                if (performed) 
                {
                    screen.inputTime = System.currentTimeMillis();
                }
            }
        }
    }
}
