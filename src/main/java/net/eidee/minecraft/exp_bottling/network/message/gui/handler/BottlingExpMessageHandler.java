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

package net.eidee.minecraft.exp_bottling.network.message.gui.handler;

import java.util.function.Supplier;

import net.eidee.minecraft.exp_bottling.inventory.container.ExpBottlingMachineContainer;
import net.eidee.minecraft.exp_bottling.network.message.gui.SetBottlingExp;
import net.eidee.minecraft.exp_bottling.network.message.gui.TakeBottledExp;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

public class BottlingExpMessageHandler
{
    public static void setBottlingExp( SetBottlingExp message, Supplier< NetworkEvent.Context > ctx )
    {
        NetworkEvent.Context _ctx = ctx.get();
        _ctx.enqueueWork( () -> {
            ServerPlayerEntity player = _ctx.getSender();
            if ( player != null && player.openContainer instanceof ExpBottlingMachineContainer )
            {
                ( ( ExpBottlingMachineContainer )player.openContainer ).setBottlingExp( message.getExpValue() );
            }
        } );
        _ctx.setPacketHandled( true );
    }

    public static void takeBottledExp( TakeBottledExp message, Supplier< NetworkEvent.Context > ctx )
    {
        NetworkEvent.Context _ctx = ctx.get();
        _ctx.enqueueWork( () -> {
            ServerPlayerEntity player = _ctx.getSender();
            if ( player != null && player.openContainer instanceof ExpBottlingMachineContainer )
            {
                ExpBottlingMachineContainer openContainer = ( ExpBottlingMachineContainer )player.openContainer;
                openContainer.takeBottledExp( message.getDragType(), message.getClickType(), player );
            }
        } );
        _ctx.setPacketHandled( true );
    }
}
