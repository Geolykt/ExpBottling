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

import static net.eidee.minecraft.exp_bottling.ExpBottlingMod.MOD_ID;

import java.util.EnumMap;
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
import net.eidee.minecraft.exp_bottling.network.message.SetBottlingExp;
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
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@OnlyIn( Dist.CLIENT )
public class ExpBottlingMachineScreen
    extends ContainerScreen< ExpBottlingMachineContainer >
    implements IContainerListener
{
    private static final ResourceLocation GUI_TEXTURE;
    private static final ITextComponent[] BUTTON_TEXT;

    static
    {
        GUI_TEXTURE = new ResourceLocation( MOD_ID, "textures/gui/container/exp_bottling_machine.png" );
        BUTTON_TEXT = new ITextComponent[] { new StringTextComponent( "1" ),
                                             new StringTextComponent( "2" ),
                                             new StringTextComponent( "3" ),
                                             new StringTextComponent( "4" ),
                                             new StringTextComponent( "5" ),
                                             new StringTextComponent( "6" ),
                                             new StringTextComponent( "7" ),
                                             new StringTextComponent( "8" ),
                                             new StringTextComponent( "9" ),
                                             new StringTextComponent( "Lv" ),
                                             new StringTextComponent( "0" ),
                                             new StringTextComponent( "BS" )
        };
    }

    private RenderType playerSkin;
    private GenericHeadModel headModel;
    private EnumMap< Input, String > inputTexts;
    private Input activeInput;
    private int blinkCount;

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
        if ( skins.containsKey( skinType ) )
        {
            return RenderType.getEntityTranslucent( skinManager.loadSkin( skins.get( skinType ), skinType ) );
        }
        return RenderType.getEntityCutoutNoCull( DefaultPlayerSkin.getDefaultSkin( player.getUniqueID() ) );
    }

    private void drawString( MatrixStack matrixStack, String string, int x, int y, int color )
    {
        font.drawString( matrixStack, string, x, y, color );
    }

    private void drawCenteredString( MatrixStack matrixStack, String string, int x, int y, int color )
    {
        font.drawString( matrixStack, string, x - ( font.getStringWidth( string ) / 2.0F ), y, color );
    }

    private void drawRightAlignedString( MatrixStack matrixStack, String text, int x, int y, int color )
    {
        font.drawString( matrixStack, text, x - font.getStringWidth( text ), y, color );
    }

    private int getVerticalCenter( int height )
    {
        return ( height - font.FONT_HEIGHT ) / 2;
    }

    private boolean isInBox( int x, int y, int xStart, int yStart, int xEnd, int yEnd )
    {
        return x >= guiLeft + xStart && x <= guiLeft + xEnd && y >= guiTop + yStart && y <= guiTop + yEnd;
    }

    private void drawPlayerHead( MatrixStack matrixStack, int x, int y )
    {
        RenderSystem.matrixMode( 5889 );
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        int k = ( int )getMinecraft().getMainWindow().getGuiScaleFactor();
        RenderSystem.viewport( ( this.width - 320 ) / 2 * k, ( this.height - 240 ) / 2 * k, 320 * k, 240 * k );
        RenderSystem.translatef( 0.20F, 0.495F, 0.0F );
        RenderSystem.multMatrix( Matrix4f.perspective( 90.0D, 1.3333334F, 9.0F, 80.0F ) );
        RenderSystem.matrixMode( 5888 );
        matrixStack.push();
        MatrixStack.Entry matrixStackEntry = matrixStack.getLast();
        matrixStackEntry.getMatrix().setIdentity();
        matrixStackEntry.getNormal().setIdentity();
        matrixStack.translate( 0, 0, 1984.0D );
        matrixStack.scale( 4.0F, 4.0F, 4.0F );
        matrixStack.rotate( Vector3f.ZP.rotationDegrees( 180F ) );
        matrixStack.rotate( Vector3f.YP.rotationDegrees( 180F ) );

        RenderSystem.enableRescaleNormal();
        headModel.func_225603_a_( 0.0F, 0.0F, 0.0F );
        IRenderTypeBuffer.Impl renderTypeBufferImpl = IRenderTypeBuffer.getImpl( Tessellator.getInstance()
                                                                                            .getBuffer() );
        IVertexBuilder vertexBuilder = renderTypeBufferImpl.getBuffer( playerSkin );
        headModel.render( matrixStack, vertexBuilder, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F );
        renderTypeBufferImpl.finish();
        matrixStack.pop();
        RenderSystem.matrixMode( 5889 );
        RenderSystem.viewport( 0,
                               0,
                               getMinecraft().getMainWindow().getFramebufferWidth(),
                               getMinecraft().getMainWindow().getFramebufferHeight() );
        RenderSystem.popMatrix();
        RenderSystem.matrixMode( 5888 );
        RenderHelper.setupGui3DDiffuseLighting();
        RenderSystem.color4f( 1.0F, 1.0F, 1.0F, 1.0F );
    }

    private void buttonHandle( Button button )
    {
        if ( activeInput == Input.NULL )
        {
            return;
        }

        String input = inputTexts.get( activeInput );
        String message = button.getMessage().getString();
        switch ( message )
        {
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                if ( Objects.equals( input, "0" ) )
                {
                    if ( Objects.equals( message, "0" ) )
                    {
                        break;
                    }
                    input = message;
                }
                else
                {
                    input += message;
                }
                break;

            case "BS":
                int length = input.length();
                if ( length > 0 )
                {
                    input = input.substring( 0, length - 1 );
                }
                break;

            case "Lv":
                if ( !input.isEmpty() )
                {
                    int level = Math.min( Integer.parseInt( input ), 21863 );
                    int exp = ExperienceUtil.levelToExp( level, 0.0F );
                    input = Integer.toString( Math.max( exp, 0 ) );
                }
                break;
        }

        if ( !input.isEmpty() && Long.parseLong( input ) > Integer.MAX_VALUE )
        {
            input = Integer.toString( Integer.MAX_VALUE );
        }

        inputTexts.put( activeInput, input );

        Input otherInput = ( activeInput == Input.UPPER ) ? Input.LOWER : Input.UPPER;
        if ( !input.isEmpty() && !inputTexts.get( otherInput ).isEmpty() )
        {
            inputTexts.put( otherInput, "" );
        }

        sendInputValues();
    }

    private void sendInputValues()
    {
        String upperInput = inputTexts.get( Input.UPPER );
        String lowerInput = inputTexts.get( Input.LOWER );

        int value;
        if ( upperInput.isEmpty() && lowerInput.isEmpty() )
        {
            value = 0;
        }
        else if ( upperInput.isEmpty() )
        {
            PlayerEntity player = playerInventory.player;
            int playerExp = ExperienceUtil.getPlayerExp( player );
            value = playerExp - Integer.parseInt( lowerInput );
        }
        else if ( lowerInput.isEmpty() )
        {
            value = Integer.parseInt( upperInput );
        }
        else
        {
            int before = Integer.parseInt( upperInput );
            int after = Integer.parseInt( lowerInput );
            value = after - before;
        }
        container.setBottlingExp( value );
        Networks.getChannel().sendToServer( new SetBottlingExp( value ) );
    }

    private void drawInputText( MatrixStack matrixStack, Input input, int playerExp )
    {
        Input otherInput = ( input == Input.UPPER ) ? Input.LOWER : Input.UPPER;

        String text = inputTexts.get( input );
        int margin = 0;
        int color = 0xFFFFFF;
        if ( activeInput == input )
        {
            if ( blinkCount / 6 % 2 == 0 )
            {
                text += "_";
            }
            else
            {
                margin = font.getStringWidth( "_" );
            }
        }
        else
        {
            margin = font.getStringWidth( "_" );

            if ( inputTexts.get( input ).isEmpty() && !inputTexts.get( otherInput ).isEmpty() )
            {
                int otherInputExp = Integer.parseInt( inputTexts.get( otherInput ) );
                text = Integer.toString( playerExp - otherInputExp );
                color = 0xA0A0A0;
            }
        }

        int offsetY = input == Input.UPPER ? 49 : 80;
        drawRightAlignedString( matrixStack, text, 136 - margin, offsetY + getVerticalCenter( 14 ), color );
    }

    @Override
    protected void init()
    {
        super.init();

        playerSkin = getPlayerSkin();
        headModel = new HumanoidHeadModel();
        inputTexts = new EnumMap<>( Input.class );
        activeInput = Input.NULL;
        blinkCount = 0;

        inputTexts.put( Input.UPPER, "" );
        inputTexts.put( Input.LOWER, "" );

        for ( int i = 0; i < BUTTON_TEXT.length; i++ )
        {
            int x = guiLeft + ( ( i % 3 ) * 21 );
            int y = guiTop + ( ( i / 3 ) * 21 );
            addButton( new Button( 162 + x, 18 + y, 20, 20, BUTTON_TEXT[ i ], this::buttonHandle ) );
        }

        container.addListener( this );
    }

    @Override
    protected void drawGuiContainerForegroundLayer( MatrixStack matrixStack, int mouseX, int mouseY )
    {
        drawCenteredString( matrixStack, getTitle().getString(), 118, 6, 4210752 );
        drawString( matrixStack, playerInventory.getDisplayName().getString(), 38, ySize - 92, 4210752 );

        PlayerEntity player = playerInventory.player;
        int playerExp = ExperienceUtil.getPlayerExp( player );
        String exp = Integer.toString( playerExp );
        drawRightAlignedString( matrixStack,
                                exp,
                                136 - font.getStringWidth( "_" ),
                                29 + getVerticalCenter( 14 ),
                                0xFFFFFF );

        drawInputText( matrixStack, Input.UPPER, playerExp );
        drawInputText( matrixStack, Input.LOWER, playerExp );
    }

    @Override
    protected void drawGuiContainerBackgroundLayer( MatrixStack matrixStack,
                                                    float partialTicks,
                                                    int mouseX,
                                                    int mouseY )
    {
        RenderHelper.setupGuiFlatDiffuseLighting();
        RenderSystem.color4f( 1.0F, 1.0F, 1.0F, 1.0F );
        getMinecraft().getTextureManager().bindTexture( GUI_TEXTURE );
        blit( matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize );

        drawPlayerHead( matrixStack, 142, 27 );
    }

    @Override
    public void onClose()
    {
        super.onClose();
        container.removeListener( this );
    }

    @Override
    public void render( MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks )
    {
        this.renderBackground( matrixStack );
        super.render( matrixStack, mouseX, mouseY, partialTicks );
        this.renderHoveredTooltip( matrixStack, mouseX, mouseY );
    }

    @Override
    public void tick()
    {
        super.tick();
        blinkCount++;
    }

    @Override
    public boolean mouseClicked( double x, double y, int button )
    {
        if ( buttons.stream().noneMatch( e -> e.isMouseOver( x, y ) ) )
        {
            activeInput = Input.NULL;
            if ( isInBox( ( int )x, ( int )y, 48, 47, 138, 63 ) )
            {
                activeInput = Input.UPPER;
            }
            else if ( isInBox( ( int )x, ( int )y, 48, 78, 138, 94 ) )
            {
                activeInput = Input.LOWER;
            }
        }
        return super.mouseClicked( x, y, button );
    }

    @Override
    public void sendAllContents( Container containerToSend, NonNullList< ItemStack > itemsList )
    {
        sendSlotContents( containerToSend, 0, containerToSend.getSlot( 0 ).getStack() );
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

    private enum Input
    {
        NULL,
        UPPER,
        LOWER
    }
}
