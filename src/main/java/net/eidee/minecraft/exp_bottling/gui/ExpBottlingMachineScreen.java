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

package net.eidee.minecraft.exp_bottling.gui;

import static net.eidee.minecraft.exp_bottling.ExpBottling.MOD_ID;

import java.util.Map;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcp.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.exp_bottling.inventory.container.ExpBottlingMachineContainer;
import net.eidee.minecraft.exp_bottling.network.Networks;
import net.eidee.minecraft.exp_bottling.network.message.gui.SetBottlingExp;
import net.eidee.minecraft.exp_bottling.network.message.gui.TakeBottledExp;
import net.eidee.minecraft.exp_bottling.util.ExperienceUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.GenericHeadModel;
import net.minecraft.client.renderer.entity.model.HumanoidHeadModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.config.GuiButtonExt;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@OnlyIn( Dist.CLIENT )
public class ExpBottlingMachineScreen
    extends ContainerScreen< ExpBottlingMachineContainer >
    implements IContainerListener
{
    private static final ResourceLocation GUI_TEXTURE;

    static
    {
        GUI_TEXTURE = new ResourceLocation( MOD_ID, "textures/gui/container/exp_bottling_machine.png" );
    }

    private RenderType playerSkin;
    private GenericHeadModel head;
    private String inputString1;
    private String inputString2;
    private int blinkCount;
    private int activeInput;

    public ExpBottlingMachineScreen( ExpBottlingMachineContainer screenContainer,
                                     PlayerInventory inv,
                                     ITextComponent titleIn )
    {
        super( screenContainer, inv, titleIn );
        xSize = 236;
        ySize = 204;
    }

    private RenderType getPlayerSkin()
    {
        Minecraft minecraft = getMinecraft();
        ClientPlayerEntity player = Objects.requireNonNull( minecraft.player );
        SkinManager skinManager = minecraft.getSkinManager();
        MinecraftProfileTexture.Type skinType = MinecraftProfileTexture.Type.SKIN;
        GameProfile gameProfile = player.getGameProfile();

        Map< MinecraftProfileTexture.Type, MinecraftProfileTexture > skins;
        skins = skinManager.loadSkinFromCache( gameProfile );
        return skins.containsKey( skinType ) ? RenderType.func_228644_e_( skinManager.loadSkin( skins.get( skinType ), skinType ) )
                                             : RenderType.func_228640_c_( DefaultPlayerSkin.getDefaultSkin( player.getUniqueID() ) );
    }

    private void drawString( String text, int x, int y, int color )
    {
        font.drawString( text, x, y, color );
    }

    private void drawCenteredString( String text, int x, int y, int color )
    {
        font.drawString( text, x - ( font.getStringWidth( text ) / 2.0F ), y, color );
    }

    private void drawRightAlignedString( String text, int x, int y, int color )
    {
        font.drawString( text, x - font.getStringWidth( text ), y, color );
    }

    private int getVerticalCenter( int height )
    {
        return ( height - font.FONT_HEIGHT ) / 2;
    }

    private boolean isInBox( int x, int y, int xStart, int yStart, int xEnd, int yEnd )
    {
        return x >= guiLeft + xStart && x <= guiLeft + xEnd && y >= guiTop + yStart && y <= guiTop + yEnd;
    }

    private void drawPlayerHead( int x, int y )
    {
        RenderHelper.func_227783_c_();

        RenderSystem.pushMatrix();
        RenderSystem.disableCull();
        RenderSystem.translatef( guiLeft + x + 8, guiTop + y + 16, 0.0F );
        RenderSystem.enableRescaleNormal();
        RenderSystem.scalef( -32.0F, -32.0F, 32.0F );
        RenderSystem.rotatef( 180.0F, 0.0F, 0.0F, 1.0F );
        RenderSystem.rotatef( 180.0F, 0.0F, 1.0F, 0.0F );
        RenderSystem.enableAlphaTest();

        MatrixStack matrixStack = new MatrixStack();
        head.func_225603_a_( 0.0F, 0.0F, 0.0F );
        IRenderTypeBuffer.Impl renderTypeBuffer = IRenderTypeBuffer.func_228455_a_( Tessellator.getInstance().getBuffer() );
        IVertexBuilder ivertexbuilder = renderTypeBuffer.getBuffer( playerSkin );
        head.func_225598_a_( matrixStack, ivertexbuilder, 15728880, OverlayTexture.field_229196_a_, 1.0F, 1.0F, 1.0F, 1.0F );

        matrixStack.func_227865_b_();
        renderTypeBuffer.func_228461_a_();

        RenderSystem.popMatrix();

        RenderHelper.func_227784_d_();
    }

    private void buttonHandle( Button button )
    {
        String message = button.getMessage();
        switch ( message )
        {
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
            case "0":
                if ( activeInput == 1 )
                {
                    inputString1 += message;
                }
                else if ( activeInput == 2 )
                {
                    inputString2 += message;
                }
                break;

            case "BS":
                if ( activeInput == 1 && inputString1.length() > 0 )
                {
                    inputString1 = inputString1.substring( 0, inputString1.length() - 1 );
                }
                else if ( activeInput == 2 && inputString2.length() > 0 )
                {
                    inputString2 = inputString2.substring( 0, inputString2.length() - 1 );
                }
                break;

            case "Lv":
                if ( activeInput == 1 && !inputString1.isEmpty() )
                {
                    int level = Integer.parseInt( inputString1 );
                    int exp = ExperienceUtil.levelToExp( level, 0.0F );
                    inputString1 = Integer.toString( Math.max( exp, 0 ) );
                }
                else if ( activeInput == 2 && !inputString2.isEmpty() )
                {
                    int level = Integer.parseInt( inputString2 );
                    int exp = ExperienceUtil.levelToExp( level, 0.0F );
                    inputString2 = Integer.toString( Math.max( exp, 0 ) );
                }
                break;
        }

        if ( !inputString1.isEmpty() && Long.parseLong( inputString1 ) > Integer.MAX_VALUE )
        {
            inputString1 = Integer.toString( Integer.MAX_VALUE );
        }
        if ( !inputString2.isEmpty() && Long.parseLong( inputString2 ) > Integer.MAX_VALUE )
        {
            inputString2 = Integer.toString( Integer.MAX_VALUE );
        }

        sendInputValues();
    }

    private void sendInputValues()
    {
        int value;
        if ( inputString1.isEmpty() && inputString2.isEmpty() )
        {
            value = 0;
        }
        else if ( inputString1.isEmpty() )
        {
            PlayerEntity player = playerInventory.player;
            int playerExp = ExperienceUtil.getPlayerExp( player );
            value = playerExp - Integer.parseInt( inputString2 );
        }
        else if ( inputString2.isEmpty() )
        {
            value = Integer.parseInt( inputString1 );
        }
        else
        {
            int before = Integer.parseInt( inputString1 );
            int after = Integer.parseInt( inputString2 );
            value = after - before;
        }
        container.setBottlingExp( value );
        Networks.EXP_BOTTLING.sendToServer( new SetBottlingExp( value ) );
    }

    @Override
    protected void init()
    {
        super.init();

        playerSkin = getPlayerSkin();
        head = new HumanoidHeadModel();
        inputString1 = "";
        inputString2 = "";

        final String[] buttonText = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "Lv", "0", "BS" };
        for ( int i = 0; i < buttonText.length; i++ )
        {
            int x = guiLeft + ( ( i % 3 ) * 21 );
            int y = guiTop + ( ( i / 3 ) * 21 );
            addButton( new GuiButtonExt( 162 + x, 18 + y, 20, 20, buttonText[ i ], this::buttonHandle ) );
        }

        container.addListener( this );
    }

    @Override
    public void render( int p_render_1_, int p_render_2_, float p_render_3_ )
    {
        this.renderBackground();
        super.render( p_render_1_, p_render_2_, p_render_3_ );
        this.renderHoveredToolTip( p_render_1_, p_render_2_ );
    }

    @Override
    protected void drawGuiContainerForegroundLayer( int mouseX, int mouseY )
    {
        drawCenteredString( getTitle().getFormattedText(), 118, 6, 4210752 );
        drawString( playerInventory.getDisplayName().getFormattedText(), 38, ySize - 92, 4210752 );

        PlayerEntity player = playerInventory.player;
        int playerExp = ExperienceUtil.getPlayerExp( player );
        String exp = Integer.toString( playerExp );
        drawRightAlignedString( exp, 136 - font.getStringWidth( "_" ), 29 + getVerticalCenter( 14 ), 0xFFFFFF );

        String input = inputString1;
        int margin = 0;
        int color = 0xFFFFFF;
        if ( activeInput == 1 )
        {
            if ( blinkCount / 6 % 2 == 0 )
            {
                input += "_";
            }
            else
            {
                margin = font.getStringWidth( "_" );
            }
        }
        else
        {
            margin = font.getStringWidth( "_" );

            if ( inputString1.isEmpty() && !inputString2.isEmpty() )
            {
                int afterExp = Integer.parseInt( inputString2 );
                input = Integer.toString( playerExp - afterExp );
                color = 0xA0A0A0;
            }
        }
        drawRightAlignedString( input, 136 - margin, 49 + getVerticalCenter( 14 ), color );

        input = inputString2;
        margin = 0;
        color = 0xFFFFFF;
        if ( activeInput == 2 )
        {
            if ( blinkCount / 6 % 2 == 0 )
            {
                input += "_";
            }
            else
            {
                margin = font.getStringWidth( "_" );
            }
        }
        else
        {
            margin = font.getStringWidth( "_" );

            if ( inputString2.isEmpty() && !inputString1.isEmpty() )
            {
                int beforeExp = Integer.parseInt( inputString1 );
                input = Integer.toString( playerExp - beforeExp );
                color = 0xA0A0A0;
            }
        }
        drawRightAlignedString( input, 136 - margin, 80 + getVerticalCenter( 14 ), color );
    }

    @Override
    protected void drawGuiContainerBackgroundLayer( float partialTicks, int mouseX, int mouseY )
    {
        RenderSystem.color4f( 1.0F, 1.0F, 1.0F, 1.0F );
        getMinecraft().getTextureManager().bindTexture( GUI_TEXTURE );
        blit( guiLeft, guiTop, 0, 0, xSize, ySize );

        drawPlayerHead( 142, 27 );
    }

    @Override
    public void tick()
    {
        super.tick();
        blinkCount++;
    }

    @Override
    public boolean mouseReleased( double x, double y, int button )
    {
        if ( isInBox( ( int )x, ( int )y, 18, 79, 33, 94 ) )
        {
            return true;
        }
        return super.mouseReleased( x, y, button );
    }

    @Override
    public boolean mouseClicked( double x, double y, int button )
    {
        if ( isInBox( ( int )x, ( int )y, 18, 79, 33, 94 ) )
        {
            activeInput = 0;
            ClickType clickType = hasShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP;
            if ( container.takeBottledExp( button, clickType, playerInventory.player ) )
            {
                Networks.EXP_BOTTLING.sendToServer( new TakeBottledExp( button, clickType ) );
            }
            return true;
        }
        else if ( buttons.stream()
                         .noneMatch( e -> e.isMouseOver( x, y ) ) )
        {
            activeInput = 0;
            if ( isInBox( ( int )x, ( int )y, 48, 47, 138, 63 ) )
            {
                activeInput = 1;
            }
            else if ( isInBox( ( int )x, ( int )y, 48, 78, 138, 94 ) )
            {
                activeInput = 2;
            }
        }
        return super.mouseClicked( x, y, button );
    }

    @Override
    public void sendAllContents( Container containerToSend, NonNullList< ItemStack > itemsList )
    {
        sendSlotContents( containerToSend,
                          0,
                          containerToSend.getSlot( 0 )
                                         .getStack() );
    }

    @Override
    public void sendSlotContents( Container containerToSend, int slotInd, ItemStack stack )
    {
        if ( slotInd == 0 || slotInd == 1 )
        {
            sendInputValues();
        }
    }

    @Override
    public void sendWindowProperty( Container containerIn, int varToUpdate, int newValue )
    {
    }
}
