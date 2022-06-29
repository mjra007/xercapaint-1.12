package xerca.xercapaint.client;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import xerca.xercapaint.common.CanvasType;
import xerca.xercapaint.common.PaletteUtil;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.packets.CanvasUpdatePacket;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

@SideOnly(Side.CLIENT)
public class GuiCanvasEdit extends BasePalette {
    private static final ResourceLocation noteGuiTextures = new ResourceLocation(XercaPaint.MODID, "textures/gui/palette.png");
    private int canvasX = 240;
    private int canvasY = 40;
    private int canvasWidth;
    private int canvasHeight;
    private int brushMeterX = 420;
    private int brushMeterY = 120;
    private int canvasPixelScale;
    private int canvasPixelWidth;
    private int canvasPixelHeight;
    private int brushSize = 0;
    private boolean touchedCanvas = false;
    private boolean undoStarted = false;

    private final EntityPlayer editingPlayer;

    private CanvasType canvasType;
    private boolean isSigned = false;
    private int[] pixels;
    private String authorName = "";
    private String canvasTitle = "";
    private String name = "";
    private int version = 0;

    private static final Vec2f[] outlinePoss1 = {
            new Vec2f(0.f, 199.0f),
            new Vec2f(12.f, 199.0f),
            new Vec2f(34.f, 199.0f),
            new Vec2f(76.f, 199.0f),
    };

    private static final Vec2f[] outlinePoss2 = {
            new Vec2f(128.f, 199.0f),
            new Vec2f(135.f, 199.0f),
            new Vec2f(147.f, 199.0f),
            new Vec2f(169.f, 199.0f),
    };

    private static final int maxUndoLength = 16;
    private Deque<int[]> undoStack = new ArrayDeque<>(maxUndoLength);

    protected GuiCanvasEdit(EntityPlayer player, NBTTagCompound canvasTag, NBTTagCompound paletteTag, ITextComponent title, CanvasType canvasType) {
        super(title, paletteTag);
        paletteX = 40;
        paletteY = 40;
        this.canvasType = canvasType;
        this.canvasPixelScale = canvasType == CanvasType.SMALL ? 10 : 5;
        this.canvasPixelWidth = CanvasType.getWidth(canvasType);
        this.canvasPixelHeight = CanvasType.getHeight(canvasType);
        int canvasPixelArea = canvasPixelHeight*canvasPixelWidth;
        this.canvasWidth = this.canvasPixelWidth * this.canvasPixelScale;
        this.canvasHeight = this.canvasPixelHeight * this.canvasPixelScale;
        if(canvasType.equals(CanvasType.LONG)){
            this.canvasY += 40;
        }

        this.editingPlayer = player;
        if (canvasTag != null && !canvasTag.hasNoTags()) {
            int[] nbtPixels = canvasTag.getIntArray("pixels");
            this.authorName = canvasTag.getString("author");
            this.canvasTitle = canvasTag.getString("title");
            this.name = canvasTag.getString("name");
            this.version = canvasTag.getInteger("v");

            this.pixels =  Arrays.copyOfRange(nbtPixels, 0, canvasPixelArea);
        } else {
            this.isSigned = false;
        }

        if (this.pixels == null) {
            this.pixels = new int[canvasPixelArea];
            Arrays.fill(this.pixels, basicColors[15].rgbVal());

            long secs = System.currentTimeMillis()/1000;
            this.name = "" + player.getUniqueID().toString() + "_" + secs;
        }
    }

    private int getPixelAt(int x, int y){
        return this.pixels[y*canvasPixelWidth + x];
    }

    private void setPixelAt(int x, int y, PaletteUtil.Color color){
        if(x >= 0 && y >= 0 && x < canvasPixelWidth && y < canvasPixelHeight){
            this.pixels[y*canvasPixelWidth + x] = color.rgbVal();
        }
    }

    private void setPixelsAt(int mouseX, int mouseY, PaletteUtil.Color color, int brushSize){
        int x, y;
        final int pixelHalf = canvasPixelScale/2;
        switch (brushSize){
            case 0:
                x = (mouseX - canvasX)/ canvasPixelScale;
                y = (mouseY - canvasY)/ canvasPixelScale;

                setPixelAt(x, y, color);
                break;
            case 1:
                x = (mouseX - canvasX + pixelHalf)/ canvasPixelScale;
                y = (mouseY - canvasY + pixelHalf)/ canvasPixelScale;

                setPixelAt(x, y, color);
                setPixelAt(x-1, y, color);
                setPixelAt(x, y-1, color);
                setPixelAt(x-1, y-1, color);
                break;
            case 2:
                x = (mouseX - canvasX + pixelHalf)/ canvasPixelScale;
                y = (mouseY - canvasY + pixelHalf)/ canvasPixelScale;

                setPixelAt(x-1, y+2, color);
                setPixelAt(x, y+2, color);

                setPixelAt(x-2, y+1, color);
                setPixelAt(x-1, y+1, color);
                setPixelAt(x, y+1, color);
                setPixelAt(x+1, y+1, color);

                setPixelAt(x-2, y, color);
                setPixelAt(x-1, y, color);
                setPixelAt(x, y, color);
                setPixelAt(x+1, y, color);

                setPixelAt(x-1, y-1, color);
                setPixelAt(x, y-1, color);
                break;
            case 3:
                x = (mouseX - canvasX)/ canvasPixelScale;
                y = (mouseY - canvasY)/ canvasPixelScale;

                setPixelAt(x-1, y+2, color);
                setPixelAt(x+0, y+2, color);
                setPixelAt(x+1, y+2, color);

                setPixelAt(x-2, y+1, color);
                setPixelAt(x-1, y+1, color);
                setPixelAt(x+0, y+1, color);
                setPixelAt(x+1, y+1, color);
                setPixelAt(x+2, y+1, color);

                setPixelAt(x-2, y, color);
                setPixelAt(x-1, y, color);
                setPixelAt(x+0, y, color);
                setPixelAt(x+1, y, color);
                setPixelAt(x+2, y, color);

                setPixelAt(x-2, y-1, color);
                setPixelAt(x-1, y-1, color);
                setPixelAt(x+0, y-1, color);
                setPixelAt(x+1, y-1, color);
                setPixelAt(x+2, y-1, color);

                setPixelAt(x-1, y-2, color);
                setPixelAt(x+0, y-2, color);
                setPixelAt(x+1, y-2, color);
                break;
        }
    }

    @Override
    public void initGui() {
        // Hide mouse cursor
//        GLFW.glfwSetInputMode(this.getMinecraft().mainWindow.getHandle(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        Mouse.setGrabbed(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        // Draw the canvas
        for(int i=0; i<canvasPixelHeight; i++){
            for(int j=0; j<canvasPixelWidth; j++){
                int y = canvasY + i* canvasPixelScale;
                int x = canvasX + j* canvasPixelScale;
                drawRect(x, y, x + canvasPixelScale, y + canvasPixelScale, getPixelAt(j, i));
            }
        }

        // Draw brush meter
        for(int i=0; i<4; i++){
            int y = brushMeterY + i*brushSpriteSize;
            drawRect(brushMeterX, y, brushMeterX + 3, y + 3, currentColor.rgbVal());
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexturedModalRect(brushMeterX, brushMeterY + (3 - brushSize)*brushSpriteSize, 15, 246, 10, 10);
        drawTexturedModalRect(brushMeterX, brushMeterY, brushSpriteX, brushSpriteY - brushSpriteSize*3, brushSpriteSize, brushSpriteSize*4);

        // Draw brush and outline
        renderCursor(mouseX, mouseY);
    }

    public static void setGLColor(PaletteUtil.Color c){
        GlStateManager.color(((float)c.r)/255.f, ((float)c.g)/255.f, ((float)c.b)/255.f, 1.0f);
    }

    private void renderCursor(int mouseX, int mouseY){
        if(isCarryingColor){
            setGLColor(carriedColor);
            drawTexturedModalRect(mouseX-brushSpriteSize/2, mouseY-brushSpriteSize/2, brushSpriteX+brushSpriteSize, brushSpriteY, dropSpriteWidth, brushSpriteSize);

        }else if(isCarryingWater){
            setGLColor(waterColor);
            drawTexturedModalRect(mouseX-brushSpriteSize/2, mouseY-brushSpriteSize/2, brushSpriteX+brushSpriteSize, brushSpriteY, dropSpriteWidth, brushSpriteSize);
        }else{
            if(inCanvas(mouseX, mouseY)){
                // Render drawing outline
                int x = 0;
                int y = 0;
                int outlineSize = 0;
                int pixelHalf = canvasPixelScale/2;
                if(brushSize == 0){
                    x = ((mouseX - canvasX)/ canvasPixelScale)*canvasPixelScale + canvasX - 1;
                    y = ((mouseY - canvasY)/ canvasPixelScale)*canvasPixelScale + canvasY - 1;
                    outlineSize = canvasPixelScale + 2;
                }
                if(brushSize == 1){
                    x = (((mouseX - canvasX + pixelHalf) / canvasPixelScale) - 1)*canvasPixelScale + canvasX - 1;
                    y = (((mouseY - canvasY + pixelHalf) / canvasPixelScale) - 1)*canvasPixelScale + canvasY - 1;
                    outlineSize = canvasPixelScale*2 + 2;
                }
                if(brushSize == 2){
                    x = (((mouseX - canvasX + pixelHalf) / canvasPixelScale) - 2)*canvasPixelScale + canvasX - 1;
                    y = (((mouseY - canvasY + pixelHalf) / canvasPixelScale) - 2)*canvasPixelScale + canvasY - 1;
                    outlineSize = canvasPixelScale*4 + 2;
                }
                if(brushSize == 3){
                    x = (((mouseX - canvasX)/ canvasPixelScale) - 2)*canvasPixelScale + canvasX - 1;
                    y = (((mouseY - canvasY)/ canvasPixelScale) - 2)*canvasPixelScale + canvasY - 1;
                    outlineSize = canvasPixelScale*5 + 2;
                }

                Vec2f textureVec;
                if(canvasPixelScale == 10){
                    textureVec = outlinePoss1[brushSize];
                }
                else{
                    textureVec = outlinePoss2[brushSize];
                }

                GlStateManager.color(0.3F, 0.3F, 0.3F, 1.0F);
                drawTexturedModalRect(x, y, (int)textureVec.x, (int)textureVec.y, outlineSize, outlineSize);

            }

            drawRect(mouseX, mouseY, mouseX + 3, mouseY + 3, currentColor.rgbVal());

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int trueBrushY = brushSpriteY - brushSpriteSize*brushSize;
            drawTexturedModalRect(mouseX, mouseY, brushSpriteX, trueBrushY, brushSpriteSize, brushSpriteSize);
        }
    }

    public static boolean isKeyComboCtrlZ(int keyID)
    {
        return keyID == Keyboard.KEY_Z && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if(isKeyComboCtrlZ(keyCode)){
            if(undoStack.size() > 0){
                pixels = undoStack.pop();
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheelState = Mouse.getEventDWheel();
        if (wheelState != 0) {
            final int maxBrushSize = 3;
            brushSize += wheelState > 0 ? 1 : -1;
            if (brushSize > maxBrushSize) brushSize = 0;
            else if (brushSize < 0) brushSize = maxBrushSize;
        }
    }

    // Mouse button 0: left, 1: right
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        undoStarted = true;
        touchedCanvas = false;
        if(undoStack.size() >= maxUndoLength){
            undoStack.removeLast();
        }
        undoStack.push(pixels.clone());

        if(inCanvas(mouseX, mouseY)){
            touchedCanvas = true;
            setPixelsAt(mouseX, mouseY, currentColor, brushSize);
            dirty = true;
        }

        if(inBrushMeter(mouseX, mouseY)){
            int selectedSize = 3 - (mouseY - brushMeterY)/brushSpriteSize;
            if(selectedSize >= 0){
                brushSize = selectedSize;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int posX, int posY, int mouseButton) {
        if(undoStarted && !touchedCanvas){
            undoStarted = false;
            undoStack.removeFirst();
        }
        super.mouseReleased(posX, posY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(inCanvas(mouseX, mouseY)){
            touchedCanvas = true;
            setPixelsAt(mouseX, mouseY, currentColor, brushSize);
            dirty = true;
        }else{
            if(inBrushMeter(lastClickX, lastClickY) == false && paletteClick(lastClickX, lastClickY) == false){
                updatePalettePos(mouseX-lastClickX , mouseY-lastClickY);
                lastClickY= mouseY;
                lastClickX= mouseX;
            }
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    protected void updatePalettePos(int deltaX, int deltaY) {
        super.updatePalettePos(deltaX,deltaY);
        canvasX += deltaX;
        canvasY += deltaY;
        brushMeterX += (int)deltaX;
        brushMeterY += (int)deltaY;
    }

    private boolean inCanvas(int x, int y) {
        return x < canvasX + canvasWidth && x >= canvasX && y < canvasY + canvasHeight && y >= canvasY;
    }

    private boolean inBrushMeter(int x, int y) {
        return x < brushMeterX + brushSpriteSize && x >= brushMeterX && y < brushMeterY + brushSpriteSize*4 && y >= brushMeterY;
    }

    @Override
    public void onGuiClosed() {
        if (dirty) {
            version ++;
            CanvasUpdatePacket pack = new CanvasUpdatePacket(pixels, isSigned, canvasTitle, name, version, customColors, canvasType);
            XercaPaint.network.sendToServer(pack);
        }
    }

}