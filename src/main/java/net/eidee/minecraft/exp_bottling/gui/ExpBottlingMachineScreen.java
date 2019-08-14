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

import java.io.IOException;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import mcp.MethodsReturnNonnullByDefault;
import net.eidee.minecraft.exp_bottling.inventory.container.ExpBottlingMachineContainer;
import net.eidee.minecraft.exp_bottling.network.Networks;
import net.eidee.minecraft.exp_bottling.network.message.gui.SetBottlingExp;
import net.eidee.minecraft.exp_bottling.network.message.gui.TakeBottledExp;
import net.eidee.minecraft.exp_bottling.util.ExperienceUtil;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelHumanoidHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SideOnly( Side.CLIENT )
public class ExpBottlingMachineScreen
    extends GuiContainer
    implements IContainerListener
{
    private static final ResourceLocation GUI_TEXTURE;

    static
    {
        GUI_TEXTURE = new ResourceLocation( MOD_ID, "textures/gui/container/exp_bottling_machine.png" );
    }

    private ExpBottlingMachineContainer container;
    private InventoryPlayer inventoryPlayer;

    private TextComponentTranslation title;
    private ResourceLocation playerSkin;
    private ModelBase head;
    private String inputString1;
    private String inputString2;
    private int blinkCount;
    private int activeInput;

    public ExpBottlingMachineScreen( ExpBottlingMachineContainer screenContainer,
                                     InventoryPlayer inventoryPlayer )
    {
        super( screenContainer );
        this.container = screenContainer;
        this.inventoryPlayer = inventoryPlayer;
        xSize = 236;
        ySize = 204;
    }

    private ResourceLocation getPlayerSkin()
    {
        SkinManager skinManager = mc.getSkinManager();
        MinecraftProfileTexture.Type skinType = MinecraftProfileTexture.Type.SKIN;
        Map< MinecraftProfileTexture.Type, MinecraftProfileTexture > skins;
        skins = skinManager.loadSkinFromCache( inventoryPlayer.player.getGameProfile() );
        return skins.containsKey( skinType ) ? skinManager.loadSkin( skins.get( skinType ), skinType )
                                             : DefaultPlayerSkin.getDefaultSkin( inventoryPlayer.player.getUniqueID() );
    }

    private void drawString( String text, int x, int y, int color )
    {
        fontRenderer.drawString( text, x, y, color );
    }

    private void drawCenteredString( String text, int x, int y, int color )
    {
        fontRenderer.drawString( text, x - ( fontRenderer.getStringWidth( text ) / 2 ), y, color );
    }

    private void drawRightAlignedString( String text, int x, int y, int color )
    {
        fontRenderer.drawString( text, x - fontRenderer.getStringWidth( text ), y, color );
    }

    private int getVerticalCenter( int height )
    {
        return ( height - fontRenderer.FONT_HEIGHT ) / 2;
    }

    private boolean isInBox( int x, int y, int xStart, int yStart, int xEnd, int yEnd )
    {
        return x >= guiLeft + xStart && x <= guiLeft + xEnd && y >= guiTop + yStart && y <= guiTop + yEnd;
    }

    private void drawPlayerHead( int x, int y )
    {
        mc.getTextureManager().bindTexture( playerSkin );
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.translate( guiLeft + x + 8, guiTop + y + 16, 0.0F );
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale( -32.0F, -32.0F, 32.0F );
        GlStateManager.rotate( 180.0F, 0.0F, 0.0F, 1.0F );
        GlStateManager.rotate( 180.0F, 0.0F, 1.0F, 0.0F );
        GlStateManager.enableAlpha();
        GlStateManager.enableBlendProfile( GlStateManager.Profile.PLAYER_SKIN );
        head.render( null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F );
        GlStateManager.popMatrix();
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
            EntityPlayer player = inventoryPlayer.player;
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
    protected void actionPerformed( GuiButton button )
        throws IOException
    {
        String message = button.displayString;
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

    @Override
    public void initGui()
    {
        super.initGui();

        title = new TextComponentTranslation( "container.exp_bottling.exp_bottling_machine" );
        playerSkin = getPlayerSkin();
        head = new ModelHumanoidHead();
        inputString1 = "";
        inputString2 = "";

        final String[] buttonText = {
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "Lv", "0", "BS"
        };
        for ( int i = 0; i < buttonText.length; i++ )
        {
            int x = guiLeft + ( ( i % 3 ) * 21 );
            int y = guiTop + ( ( i / 3 ) * 21 );
            addButton( new GuiButtonExt( i, 162 + x, 18 + y, 20, 20, buttonText[ i ] ) );
        }

        container.addListener( this );
    }

    @Override
    public void drawScreen( int mouseX, int mouseY, float partialTicks )
    {
        this.drawDefaultBackground();
        super.drawScreen( mouseX, mouseY, partialTicks );
        this.renderHoveredToolTip( mouseX, mouseY );
    }

    @Override
    protected void drawGuiContainerForegroundLayer( int mouseX, int mouseY )
    {
        drawCenteredString( title.getFormattedText(), 118, 6, 4210752 );
        drawString( inventoryPlayer.getDisplayName().getFormattedText(), 38, ySize - 91, 4210752 );

        EntityPlayer player = inventoryPlayer.player;
        int playerExp = ExperienceUtil.getPlayerExp( player );
        String exp = Integer.toString( playerExp );
        drawRightAlignedString( exp, 136 - fontRenderer.getStringWidth( "_" ), 28 + getVerticalCenter( 14 ), 0xFFFFFF );

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
                margin = fontRenderer.getStringWidth( "_" );
            }
        }
        else
        {
            margin = fontRenderer.getStringWidth( "_" );

            if ( inputString1.isEmpty() && !inputString2.isEmpty() )
            {
                int afterExp = Integer.parseInt( inputString2 );
                input = Integer.toString( playerExp - afterExp );
                color = 0xA0A0A0;
            }
        }
        drawRightAlignedString( input, 136 - margin, 48 + getVerticalCenter( 14 ), color );

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
                margin = fontRenderer.getStringWidth( "_" );
            }
        }
        else
        {
            margin = fontRenderer.getStringWidth( "_" );

            if ( inputString2.isEmpty() && !inputString1.isEmpty() )
            {
                int beforeExp = Integer.parseInt( inputString1 );
                input = Integer.toString( playerExp - beforeExp );
                color = 0xA0A0A0;
            }
        }
        drawRightAlignedString( input, 136 - margin, 79 + getVerticalCenter( 14 ), color );
    }

    @Override
    protected void drawGuiContainerBackgroundLayer( float partialTicks, int mouseX, int mouseY )
    {
        GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
        mc.getTextureManager().bindTexture( GUI_TEXTURE );
        drawTexturedModalRect( guiLeft, guiTop, 0, 0, xSize, ySize );
        drawPlayerHead( 142, 27 );
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
        blinkCount++;
    }

    @Override
    protected void mouseReleased( int mouseX, int mouseY, int state )
    {
        if ( isInBox( mouseX, mouseY, 18, 79, 33, 94 ) )
        {
            return;
        }
        super.mouseReleased( mouseX, mouseY, state );
    }

    @Override
    protected void mouseClicked( int mouseX, int mouseY, int mouseButton )
        throws IOException
    {
        if ( isInBox( mouseX, mouseY, 18, 79, 33, 94 ) )
        {
            activeInput = 0;
            ClickType clickType = isShiftKeyDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP;
            if ( container.takeBottledExp( mouseButton, clickType, inventoryPlayer.player ) )
            {
                Networks.EXP_BOTTLING.sendToServer( new TakeBottledExp( mouseButton, clickType ) );
            }
            return;
        }
        else
        {
            boolean isButtonClicked = false;
            for ( GuiButton button : buttonList )
            {
                if ( button.isMouseOver() )
                {
                    isButtonClicked = true;
                    break;
                }
            }
            if ( !isButtonClicked )
            {
                activeInput = 0;
                if ( isInBox( mouseX, mouseY, 48, 47, 138, 63 ) )
                {
                    activeInput = 1;
                }
                else if ( isInBox( mouseX, mouseY, 48, 78, 138, 94 ) )
                {
                    activeInput = 2;
                }
            }
        }
        super.mouseClicked( mouseX, mouseY, mouseButton );
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

    @Override
    public void sendAllWindowProperties( Container containerIn, IInventory inventory )
    {
    }
}
