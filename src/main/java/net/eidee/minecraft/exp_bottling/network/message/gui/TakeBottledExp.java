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

package net.eidee.minecraft.exp_bottling.network.message.gui;

import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.ClickType;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class TakeBottledExp
    implements IMessage
{
    private int dragType;
    private int clickType;

    public TakeBottledExp()
    {
    }

    public TakeBottledExp( int dragType, int clickType )
    {
        this.dragType = dragType;
        this.clickType = clickType;
    }

    public TakeBottledExp( int dragType, ClickType clickType )
    {
        this( dragType, clickType == ClickType.PICKUP ? 0 : 1 );
    }

    public int getDragType()
    {
        return dragType;
    }

    public ClickType getClickType()
    {
        return clickType == 0 ? ClickType.PICKUP : ClickType.QUICK_MOVE;
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        dragType = buf.readInt();
        clickType = buf.readByte();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeInt( dragType );
        buf.writeByte( clickType );
    }
}
