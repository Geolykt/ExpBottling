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

package net.eidee.minecraft.exp_bottling.network.message;

import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import net.eidee.minecraft.exp_bottling.world.inventory.ExpBottlingMachineMenu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record SetExpToBeBottled(int exp) {

  public static void encode(SetExpToBeBottled message, FriendlyByteBuf buffer) {
    buffer.writeInt(message.exp);
  }

  public static SetExpToBeBottled decode(FriendlyByteBuf buffer) {
    return new SetExpToBeBottled(buffer.readInt());
  }

  public static void handle(SetExpToBeBottled message, Supplier<NetworkEvent.Context> supplier) {
    NetworkEvent.Context ctx = supplier.get();
    ctx.enqueueWork(
        () -> {
          ServerPlayer player = ctx.getSender();
          if (player != null && player.containerMenu instanceof ExpBottlingMachineMenu menu) {
            menu.setExpToBeBottled(message.exp);
          }
        });
    ctx.setPacketHandled(true);
  }
}
