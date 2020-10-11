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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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

    static
    {
        GUI_TEXTURE = new ResourceLocation( MOD_ID, "textures/gui/container/exp_bottling_machine.png" );
    }

    private RenderType playerSkin;
    private GenericHeadModel headModel;
    private EnumMap< Input, String > inputTexts;
    private Input activeInput;
    private int blinkCount;
    private BiMap< ButtonLogic, Button > logicToButtonMap;

    public ExpBottlingMachineScreen( ExpBottlingMachineContainer screenContainer,
                                     PlayerInventory inv,
                                     ITextComponent titleIn )
    {
        super( screenContainer, inv, titleIn );
        xSize = 236;
        ySize = 204;
        logicToButtonMap = HashBiMap.create();
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
        stimulateInput( logicToButtonMap.inverse().get( button ) );
    }

    private void stimulateInput( ButtonLogic logic )
    {
        if ( activeInput == Input.NULL )
        {
            return;
        }

        String input = logic.handleInput( inputTexts.get( activeInput ) );

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
        logicToButtonMap.clear();

        inputTexts.put( Input.UPPER, "" );
        inputTexts.put( Input.LOWER, "" );

        for ( ButtonLogic logic : ButtonLogic.values() )
        {
            int i = logic.getSortOrder();
            int x = guiLeft + ( ( i % 3 ) * 21 );
            int y = guiTop + ( ( i / 3 ) * 21 );
            Button button = addButton( new Button( 162 + x, 18 + y, 20, 20, logic.getText(), this::buttonHandle ) );
            logicToButtonMap.put( logic, button );
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
    public boolean keyPressed( int keyCode, int scanCode, int modifiers )
    {
        if ( super.keyPressed( keyCode, scanCode, modifiers ) )
        {
            return true;
        }

        switch ( InputMappings.getInputByCode( keyCode, scanCode ).getTranslationKey() )
        {
            case "key.keyboard.0":
            case "key.keyboard.keypad.0":
                stimulateInput( ButtonLogic.NUMBER_0 );
                break;

            case "key.keyboard.1":
            case "key.keyboard.keypad.1":
                stimulateInput( ButtonLogic.NUMBER_1 );
                break;

            case "key.keyboard.2":
            case "key.keyboard.keypad.2":
                stimulateInput( ButtonLogic.NUMBER_2 );
                break;

            case "key.keyboard.3":
            case "key.keyboard.keypad.3":
                stimulateInput( ButtonLogic.NUMBER_3 );
                break;

            case "key.keyboard.4":
            case "key.keyboard.keypad.4":
                stimulateInput( ButtonLogic.NUMBER_4 );
                break;

            case "key.keyboard.5":
            case "key.keyboard.keypad.5":
                stimulateInput( ButtonLogic.NUMBER_5 );
                break;

            case "key.keyboard.6":
            case "key.keyboard.keypad.6":
                stimulateInput( ButtonLogic.NUMBER_6 );
                break;

            case "key.keyboard.7":
            case "key.keyboard.keypad.7":
                stimulateInput( ButtonLogic.NUMBER_7 );
                break;

            case "key.keyboard.8":
            case "key.keyboard.keypad.8":
                stimulateInput( ButtonLogic.NUMBER_8 );
                break;

            case "key.keyboard.9":
            case "key.keyboard.keypad.9":
                stimulateInput( ButtonLogic.NUMBER_9 );
                break;

            case "key.keyboard.backspace":
                stimulateInput( ButtonLogic.BACKSPACE );
                break;

            case "key.keyboard.l":
                stimulateInput( ButtonLogic.LEVEL );
                break;

            default:
                break;
        }

        return true;
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

    private enum ButtonLogic
    {
        NUMBER_1( 0, "gui.exp_bottling.exp_bottling_machine.button.1" )
            {
                @Override
                public String handleInput( String input )
                {
                    return Objects.equals( input, "0" ) ? "1" : input + "1";
                }
            },
        NUMBER_2( 1, "gui.exp_bottling.exp_bottling_machine.button.2" )
            {
                @Override
                public String handleInput( String input )
                {
                    return Objects.equals( input, "0" ) ? "2" : input + "2";
                }
            },
        NUMBER_3( 2, "gui.exp_bottling.exp_bottling_machine.button.3" )
            {
                @Override
                public String handleInput( String input )
                {
                    return Objects.equals( input, "0" ) ? "3" : input + "3";
                }
            },
        NUMBER_4( 3, "gui.exp_bottling.exp_bottling_machine.button.4" )
            {
                @Override
                public String handleInput( String input )
                {
                    return Objects.equals( input, "0" ) ? "4" : input + "4";
                }
            },
        NUMBER_5( 4, "gui.exp_bottling.exp_bottling_machine.button.5" )
            {
                @Override
                public String handleInput( String input )
                {
                    return Objects.equals( input, "0" ) ? "5" : input + "5";
                }
            },
        NUMBER_6( 5, "gui.exp_bottling.exp_bottling_machine.button.6" )
            {
                @Override
                public String handleInput( String input )
                {
                    return Objects.equals( input, "0" ) ? "6" : input + "6";
                }
            },
        NUMBER_7( 6, "gui.exp_bottling.exp_bottling_machine.button.7" )
            {
                @Override
                public String handleInput( String input )
                {
                    return Objects.equals( input, "0" ) ? "7" : input + "7";
                }
            },
        NUMBER_8( 7, "gui.exp_bottling.exp_bottling_machine.button.8" )
            {
                @Override
                public String handleInput( String input )
                {
                    return Objects.equals( input, "0" ) ? "8" : input + "8";
                }
            },
        NUMBER_9( 8, "gui.exp_bottling.exp_bottling_machine.button.9" )
            {
                @Override
                public String handleInput( String input )
                {
                    return Objects.equals( input, "0" ) ? "9" : input + "9";
                }
            },
        NUMBER_0( 10, "gui.exp_bottling.exp_bottling_machine.button.0" )
            {
                @Override
                public String handleInput( String input )
                {
                    return Objects.equals( input, "0" ) ? input : input + "0";
                }
            },
        LEVEL( 9, "gui.exp_bottling.exp_bottling_machine.button.lv" )
            {
                @Override
                public String handleInput( String input )
                {
                    if ( !input.isEmpty() )
                    {
                        int level = MathHelper.clamp( Integer.parseInt( input ), 0, 21863 );
                        if ( level > 0 )
                        {
                            int exp = ExperienceUtil.levelToExp( level, 0.0F );
                            return Integer.toString( Math.max( exp, 0 ) );
                        }
                    }
                    return "";
                }
            },
        BACKSPACE( 11, "gui.exp_bottling.exp_bottling_machine.button.bs" )
            {
                @Override
                public String handleInput( String input )
                {
                    int length = input.length();
                    return length > 0 ? input.substring( 0, length - 1 ) : "";
                }
            };

        private final int sortOrder;
        private final ITextComponent text;

        ButtonLogic( int sortOrder, String text )
        {
            this.sortOrder = sortOrder;
            this.text = new TranslationTextComponent( text );
        }

        public int getSortOrder()
        {
            return sortOrder;
        }

        public ITextComponent getText()
        {
            return text;
        }

        public abstract String handleInput( String input );
    }
}
