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

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SkullBlock;

public class PlayerHeadIcon implements Widget {
  private final Player player;
  private final SkullModelBase model;
  private final int x;
  private final int y;
  private final int width;
  private final int height;

  private GameProfile gameProfile;

  public PlayerHeadIcon(Player player, int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.player = player;
    this.model =
        new SkullModel(
            Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_HEAD));
  }

  @Override
  public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
    GameProfile currentGameProfile = this.player.getGameProfile();
    if (this.gameProfile == null || !this.gameProfile.equals(currentGameProfile)) {
      this.gameProfile = currentGameProfile;
    }

    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.enableBlend();
    RenderSystem.blendFunc(
        GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

    PoseStack stack1 = RenderSystem.getModelViewStack();
    stack1.pushPose();
    stack1.translate(this.x, this.y, 100.0F);
    stack1.translate((-this.width / 2.0), this.height, 0.0D);
    stack1.scale(1.0F, -1.0F, 1.0F);
    stack1.scale((this.width * 2.0F), (this.height * 2.0F), 32.0F);
    RenderSystem.applyModelViewMatrix();

    PoseStack stack2 = new PoseStack();
    MultiBufferSource.BufferSource bufferSource =
        Minecraft.getInstance().renderBuffers().bufferSource();
    Lighting.setupForFlatItems();

    stack2.pushPose();
    SkullBlockRenderer.renderSkull(
        null,
        180.0F,
        0.0F,
        stack2,
        bufferSource,
        15728880,
        this.model,
        SkullBlockRenderer.getRenderType(SkullBlock.Types.PLAYER, this.gameProfile));
    stack2.popPose();

    bufferSource.endBatch();
    RenderSystem.enableDepthTest();
    Lighting.setupFor3DItems();

    stack1.popPose();
    RenderSystem.applyModelViewMatrix();
  }
}
