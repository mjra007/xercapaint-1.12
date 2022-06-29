package xerca.xercapaint.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.ITextComponent;
import xerca.xercapaint.common.PaletteUtil;
import xerca.xercapaint.common.XercaPaint;

import java.io.IOException;

import static xerca.xercapaint.common.PaletteUtil.readCustomColorArrayFromNBT;

public abstract class BasePalette extends GuiScreen {
    protected static final ResourceLocation noteGuiTextures = new ResourceLocation(XercaPaint.MODID, "textures/gui/palette.png");
    final static int dyeSpriteX = 240;
    final static int dyeSpriteSize = 16;
    final static int brushSpriteX = 0;
    final static int brushSpriteY = 247;
    final static int brushSpriteSize = 9;
    final static int dropSpriteWidth = 6;
    final static int paletteWidth = 157;
    final static int paletteHeight = 193;
    static int paletteX;
    static int paletteY;
    int lastClickX =0;
    int lastClickY = 0;
    final static PaletteUtil.Color waterColor = new PaletteUtil.Color(53, 118, 191);
    final static PaletteUtil.Color emptinessColor = new PaletteUtil.Color(255, 236, 229);

    final static PaletteUtil.Color[] basicColors = {
            new PaletteUtil.Color(0xFF1D1D21),
            new PaletteUtil.Color(0xFFB02E26),
            new PaletteUtil.Color(0xFF5E7C16),
            new PaletteUtil.Color(0xFF835432),
            new PaletteUtil.Color(0xFF3C44AA),
            new PaletteUtil.Color(0xFF8932B8),
            new PaletteUtil.Color(0xFF169C9C),
            new PaletteUtil.Color(0xFF9D9D97),
            new PaletteUtil.Color(0xFF474F52),
            new PaletteUtil.Color(0xFFF38BAA),
            new PaletteUtil.Color(0xFF80C71F),
            new PaletteUtil.Color(0xFFFED83D),
            new PaletteUtil.Color(0xFF3AB3DA),
            new PaletteUtil.Color(0xFFC74EBD),
            new PaletteUtil.Color(0xFFF9801D),
            new PaletteUtil.Color(0xFFF9FFFE)
    };
    final static Vec2f[] basicColorCenters = {
            new Vec2f(23.5f, 172.5f),
            new Vec2f(18.5f, 145.5f),
            new Vec2f(16.5f, 117.5f),
            new Vec2f(17.5f, 89.5f),
            new Vec2f(23.5f, 62.5f),
            new Vec2f(38.5f, 39.5f),
            new Vec2f(61.5f, 24.5f),
            new Vec2f(87.5f, 17.5f),
            new Vec2f(114.5f, 15.5f),
            new Vec2f(44.5f, 154.5f),
            new Vec2f(41.5f, 127.5f),
            new Vec2f(42.5f, 100.5f),
            new Vec2f(48.5f, 74.5f),
            new Vec2f(64.5f, 52.5f),
            new Vec2f(90.5f, 44.5f),
            new Vec2f(117.5f, 42.5f)
    };
    final static Vec2f[] customColorCenters = {
            new Vec2f(101.5f, 132.0f),
            new Vec2f(113.5f, 118.0f),
            new Vec2f(120.5f, 102.0f),
            new Vec2f(124.5f, 084.0f),
            new Vec2f(126.5f, 066.0f),
            new Vec2f(097.5f, 152.0f),
            new Vec2f(114.5f, 146.0f),
            new Vec2f(127.5f, 133.0f),
            new Vec2f(134.5f, 116.0f),
            new Vec2f(139.5f, 098.0f),
            new Vec2f(142.5f, 080.0f),
            new Vec2f(144.5f, 062.0f),
    };
    final static Vec2f waterCenter = new Vec2f(140.5f, 28.f);
    final static float basicColorRadius = 11.f;
    final static float customColorRadius = 6.5f;

    boolean isCarryingColor = false;
    boolean isCarryingWater = false;
    boolean isCarryingPalette = false;
    boolean dirty = false;
    PaletteUtil.Color carriedColor;
    PaletteUtil.Color currentColor = basicColors[0];
    PaletteUtil.CustomColor[] customColors;
    boolean[] basicColorFlags;

    BasePalette(ITextComponent titleIn, NBTTagCompound paletteTag) {
        super();
        this.customColors = new PaletteUtil.CustomColor[12];
        this.basicColorFlags = new boolean[16];

        if (paletteTag != null) {
            if(paletteTag.hasKey("r") && paletteTag.hasKey("g") && paletteTag.hasKey("b")
                    && paletteTag.hasKey("m") && paletteTag.hasKey("n")){
                readCustomColorArrayFromNBT(paletteTag, this.customColors);

            }else{
                for(int i=0; i < customColors.length; i++){
                    customColors[i] = new PaletteUtil.CustomColor();
                }
            }

            if(paletteTag.hasKey("basic")){
                byte[] basics = paletteTag.getByteArray("basic");
                for(int i=0; i<basics.length; i++){
                    basicColorFlags[i] = basics[i] > 0;
                }
            }
            else{

            }
        }
        else{
            for(int i=0; i < customColors.length; i++){
                customColors[i] = new PaletteUtil.CustomColor();
            }
        }
    }

    protected void updatePalettePos(int deltaX, int deltaY) {
        paletteX += deltaX;
        paletteY += deltaY;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(noteGuiTextures);

        // Draw basic colors
        for(int i=0; i<basicColorFlags.length; i++){
            int x = paletteX + (int)basicColorCenters[i].x;
            int y = paletteY + (int)basicColorCenters[i].y;
            int r = (int)basicColorRadius;
            if(basicColorFlags[i]){
                drawRect(x-r, y-r, x+r+1, y+r+1, basicColors[i].rgbVal());

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                drawTexturedModalRect(x - 8, y - 8, dyeSpriteX, i*dyeSpriteSize, dyeSpriteSize, dyeSpriteSize);
            }
            else{
                drawRect(x-r, y-r, x+r+1, y+r+1, emptinessColor.rgbVal());
            }
        }

        // Draw custom colors
        for(int i=0; i<customColors.length; i++){
            int x = paletteX + (int)customColorCenters[i].x;
            int y = paletteY + (int)customColorCenters[i].y;
            drawRect(x-6, y-7, x+7, y+6, customColors[i].getColor().rgbVal());
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexturedModalRect(paletteX, paletteY, 0, 0, paletteWidth, paletteHeight);
    }

    // Mouse button 0: left, 1: right
    @Override
    public void mouseClicked(int posX, int posY, int mouseButton) throws IOException {
        int mouseX = lastClickX = Math.round(posX);
        int mouseY = lastClickY = Math.round(posY);

        if(paletteClick(mouseX, mouseY)){ 
            int x = (mouseX - paletteX);
            int y = (mouseY - paletteY);
            Vec2f clickVec = new Vec2f(x, y);
            float sqrBasicRadius = basicColorRadius * basicColorRadius;
            float sqrCustomRadius = customColorRadius * customColorRadius;

            boolean colorFound = false;
            for(int i=0; i<basicColorCenters.length; i++){
                if(basicColorFlags[i] && sqrDist(clickVec, basicColorCenters[i]) <= sqrBasicRadius){
                    if(mouseButton == 0){
                        carriedColor = currentColor = basicColors[i];
                        isCarryingColor = true;
                    }
                    isCarryingPalette = false;
                    colorFound = true;
                    break;
                }
            }

            if(!colorFound){
                for(int i=0; i<customColorCenters.length; i++){
                    if(sqrDist(clickVec, customColorCenters[i]) <= sqrCustomRadius){
                        if(mouseButton == 0) {
                            if(customColors[i].getNumberOfColors() > 0){
                                carriedColor = currentColor = customColors[i].getColor();
                                isCarryingColor = true;
                            }
                        }
                        isCarryingPalette = false;
                        colorFound = true;
                        break;
                    }
                }
            }

            if(!colorFound) {
                if(sqrDist(clickVec, waterCenter) <= sqrCustomRadius){
                    if(mouseButton == 0) {
                        isCarryingPalette = false;
                        isCarryingWater = true;
                    }
                }
            }

            if(!colorFound && !isCarryingWater){
                isCarryingPalette = true;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int posX, int posY, int mouseButton) {
        int mouseX = Math.round(posX);
        int mouseY = Math.round(posY);
        isCarryingPalette = false;
        if(isCarryingColor || isCarryingWater) {
            if (paletteClick(mouseX, mouseY)) {
                float sqrCustomRadius = customColorRadius * customColorRadius;
                int x = (mouseX - paletteX);
                int y = (mouseY - paletteY);
                Vec2f clickVec = new Vec2f(x, y);
                for (int i = 0; i < customColorCenters.length; i++) {
                    if (sqrDist(clickVec, customColorCenters[i]) <= sqrCustomRadius) {
                        PaletteUtil.CustomColor customColor = customColors[i];
                        if(isCarryingWater){
                            customColor.reset();
                        }else{
                            customColor.mix(carriedColor);
                            currentColor = customColor.getColor();
                        }
                        dirty = true;
                        break;
                    }
                }
            }
            isCarryingColor = false;
            isCarryingWater = false;
        }

        super.mouseReleased(posX, posY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isCarryingPalette) {
            super.mouseClickMove( mouseX,  mouseY, clickedMouseButton, timeSinceLastClick);
            updatePalettePos(mouseX-lastClickX , mouseY-lastClickY);
            lastClickY= mouseY;
            lastClickX= mouseX;
        }
        super.mouseClickMove( mouseX,  mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    boolean paletteClick(int x, int y) {
        return x <= paletteX + paletteWidth && x >= paletteX && y <= paletteY + paletteHeight && y >= paletteY;
    }

    float sqrDist(Vec2f a, Vec2f b){
        return (a.x - b.x)*(a.x - b.x) + (a.y - b.y)*(a.y - b.y);
    }
}
