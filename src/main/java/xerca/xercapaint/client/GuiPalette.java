package xerca.xercapaint.client;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.packets.PaletteUpdatePacket;

import static xerca.xercapaint.client.GuiCanvasEdit.setGLColor;

@SideOnly(Side.CLIENT)
public class GuiPalette extends BasePalette {

    protected GuiPalette(NBTTagCompound paletteTag, ITextComponent title) {
        super(title, paletteTag);

        paletteX = 140;
        paletteY = 40;
    }

    @Override
    public void initGui() {
        if (paletteX == -1000 || paletteY == -1000) {
            paletteX = 140;
            paletteY = 40;
        }
        updatePalettePos(0, 0);
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        renderCursor(mouseX, mouseY);
    }

    private void renderCursor(int mouseX, int mouseY) {
        if (isCarryingColor) {
            setGLColor(carriedColor);
            drawTexturedModalRect(mouseX - brushSpriteSize / 2, mouseY - brushSpriteSize / 2, brushSpriteX + brushSpriteSize, brushSpriteY, dropSpriteWidth, brushSpriteSize);

        } else if (isCarryingWater) {
            setGLColor(waterColor);
            drawTexturedModalRect(mouseX - brushSpriteSize / 2, mouseY - brushSpriteSize / 2, brushSpriteX + brushSpriteSize, brushSpriteY, dropSpriteWidth, brushSpriteSize);
        }
    }

    @Override
    public void onGuiClosed() {
        if (dirty) {
            PaletteUpdatePacket pack = new PaletteUpdatePacket(customColors);
            XercaPaint.network.sendToServer(pack);
        }
    }

}