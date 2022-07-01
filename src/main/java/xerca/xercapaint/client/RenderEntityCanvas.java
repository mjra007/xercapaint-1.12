package xerca.xercapaint.client;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.xml.dtd.impl.Base;
import xerca.xercapaint.common.PaletteUtil;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.entity.EntityCanvas;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class RenderEntityCanvas extends Render<EntityCanvas> {
    static private final ResourceLocation backLocation = new ResourceLocation("minecraft", "textures/blocks/planks_birch.png");
    static RenderEntityCanvas theInstance;
    private final TextureManager textureManager;
    private final Map<String, RenderEntityCanvas.Instance> loadedCanvases = Maps.newHashMap();

    RenderEntityCanvas(RenderManager renderManager) {
        super(renderManager);
        this.textureManager = Minecraft.getMinecraft().getTextureManager();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityCanvas canvas) {
        return getMapRendererInstance(canvas).location;
    }

    @Override
    public void doRender(EntityCanvas canvas, double x, double y, double z, float yaw, float partialTick) {
        NBTTagCompound tag = canvas.getCanvasNBT();
        if(tag != null && tag.hasKey("name") ){
            getMapRendererInstance(canvas).render(x, y, z, yaw, partialTick, canvas.getHorizontalFacing());
        }
        super.doRender(canvas, x, y, z, yaw, partialTick);
    }

    RenderEntityCanvas.Instance getMapRendererInstance(NBTTagCompound tag, int width, int height) {
        RenderEntityCanvas.Instance instance = this.loadedCanvases.get(tag.getString("name"));
        if (instance == null) {
            instance = new Instance(tag, width, height);
            this.loadedCanvases.put(tag.getString("name"), instance);
        }else{
            int currentVersion = tag.getInteger("v");
            if(instance.version < currentVersion){
                instance.updateCanvasTexture(tag);
            }
        }

        return instance;
    }

    public static class RenderEntityCanvasFactory implements IRenderFactory<EntityCanvas> {
        @Override
        public Render<? super EntityCanvas> createRenderFor(RenderManager manager) {
            theInstance = new RenderEntityCanvas(manager);
            return theInstance;
        }
    }

    private RenderEntityCanvas.Instance getMapRendererInstance(EntityCanvas canvas) {
        NBTTagCompound textureData = canvas.getCanvasNBT();
        RenderEntityCanvas.Instance instance = this.loadedCanvases.get(textureData.getString("name"));
        if (instance == null) {
            instance = new Instance(canvas);
            this.loadedCanvases.put(textureData.getString("name"), instance);
        }else{
            int currentVersion = textureData.getInteger("v");
            if(instance.version != currentVersion){
                instance.updateCanvasTexture(textureData);
            }
        }

        return instance;
    }

    @Nullable
    public RenderEntityCanvas.Instance getMapInstanceIfExists(String name) {
        return this.loadedCanvases.get(name);
    }

    /**
     * Clears the currently loaded maps and removes their corresponding textures
     */
    public void clearLoadedMaps() {
        for(RenderEntityCanvas.Instance instance : this.loadedCanvases.values()) {
            instance.close();
        }

        this.loadedCanvases.clear();
    }

    public void close() {
        this.clearLoadedMaps();
    }

    @SideOnly(Side.CLIENT)
    class Instance implements AutoCloseable {
        int version = 0;
        int width;
        int height;
        private final DynamicTexture canvasTexture;
        private final ResourceLocation location;

        private Instance(EntityCanvas canvas) {
            NBTTagCompound tag = canvas.getCanvasNBT();
            this.width = canvas.getWidthPixels();
            this.height = canvas.getHeightPixels();
            this.canvasTexture = new DynamicTexture(width, height);
            this.location = RenderEntityCanvas.this.textureManager.getDynamicTextureLocation("canvas/" + tag.getString("name"), this.canvasTexture);
            updateCanvasTexture(tag);
        }

        private Instance(NBTTagCompound tag, int width, int height) {
            this.width = width;
            this.height = height;
            this.canvasTexture = new DynamicTexture(width, height);
            this.location = RenderEntityCanvas.this.textureManager.getDynamicTextureLocation("canvas/" + tag.getString("name"), this.canvasTexture);

            updateCanvasTexture(tag);
        }

        private void updateCanvasTexture(NBTTagCompound textureData) {
            this.version = textureData.getInteger("v");

            int[] pixels = textureData.getIntArray("pixels");
            int[] textureArray = canvasTexture.getTextureData();

            if(pixels.length < height*width){
                XercaPaint.LOGGER.warn("Pixels array length (" + pixels.length + ") is smaller than canvas area (" + height*width + ")");
                return;
            }
            if(textureArray.length < height*width){
                XercaPaint.LOGGER.warn("Texture array length (" + textureArray.length + ") is smaller than canvas area (" + height*width + ")");
                return;
            }

            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width; ++j) {
                    int k = j + i * width;

                    textureArray[k] = pixels[k];
                }
            }

            canvasTexture.updateDynamicTexture();
        }

        public void render( double x, double y, double z, float yaw, float partialTick, EnumFacing facing) {
            final float wScale = width/16.0f;
            final float hScale = height/16.0f;
            int lightmap = 15728880;

            GlStateManager.pushMatrix();
            final float xOffset = facing.getFrontOffsetX();
            final float zOffset = facing.getFrontOffsetZ();
            final float yOffset = -1.0f;
            GlStateManager.translate(x + zOffset*0.5d*wScale, y + yOffset*0.5d*hScale, z - xOffset*0.5d*wScale);

            GlStateManager.rotate(180.0F - yaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.disableLighting();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();

            float f = 1.0f/32.0f;
            GlStateManager.scale(f, f, f);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            textureManager.bindTexture(location);
            GlStateManager.disableAlpha();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);

            // Draw the front
            bufferbuilder.pos(0.0D, 32.0D*hScale, -1.0D).tex(1.0D, 0.0D).normal(xOffset, 0.0F, zOffset).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, -1.0D).tex(0.0D, 0.0D).normal(xOffset, 0.0F, zOffset).endVertex();
            bufferbuilder.pos(32.0D*wScale, 0.0D, -1.0D).tex(0.0D, 1.0D).normal(xOffset, 0.0F, zOffset).endVertex();
            bufferbuilder.pos(0.0D, 0.0D, -1.0D).tex(1.0D, 1.0D).normal(xOffset, 0.0F, zOffset).endVertex();
            tessellator.draw();

            // Draw the back and sides
            final double sideWidth = 1.0D/16.0D;
            bufferbuilder = tessellator.getBuffer();
            textureManager.bindTexture(backLocation);
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(0.0D, 0.0D, 1.0F).tex(0.0D, 0.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 0.0D, 1.0F).tex(1.0D, 0.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, 1.0F).tex(1.0D, 1.0D).endVertex();
            bufferbuilder.pos(0.0D, 32.0D*hScale, 1.0F).tex(0.0D, 1.0D).endVertex();

            // Sides
            bufferbuilder.pos(0.0D, 0.0D, 1.0F).tex(sideWidth, 0.0D).endVertex();
            bufferbuilder.pos(0.0D, 32.0D*hScale, 1.0F).tex(sideWidth, 1.0D).endVertex();
            bufferbuilder.pos(0.0D, 32.0D*hScale, -1.0F).tex(0.0D, 1.0D).endVertex();
            bufferbuilder.pos(0.0D, 0.0D, -1.0F).tex(0.0D, 0.0D).endVertex();

            bufferbuilder.pos(0.0D, 32.0D*hScale, 1.0F).tex(0.0D, 0.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, 1.0F).tex(1.0D, 0.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, -1.0F).tex(1.0D, sideWidth).endVertex();
            bufferbuilder.pos(0.0D, 32.0D*hScale, -1.0F).tex(0.0D, sideWidth).endVertex();

            bufferbuilder.pos(32.0D*wScale, 0.0D, -1.0F).tex(0.0D, 0.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, -1.0F).tex(0.0D, 1.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, 1.0F).tex(sideWidth, 1.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 0.0D, 1.0F).tex(sideWidth, 0.0D).endVertex();

            bufferbuilder.pos(0.0D, 0.0D, -1.0F).tex(0.0D, 1.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 0.0D, -1.0F).tex(1.0D, 1.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 0.0D, 1.0F).tex(1.0D, 1.0D-sideWidth).endVertex();
            bufferbuilder.pos(0.0D, 0.0D, 1.0F).tex(0.0D, 1.0D-sideWidth).endVertex();

            tessellator.draw();
            GlStateManager.enableAlpha();
            RenderHelper.disableStandardItemLighting();

            GlStateManager.enableLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }

        public void renderItemStack() {
            final float wScale = width/16.0f;
            final float hScale = height/16.0f;

            GlStateManager.pushMatrix();
            final float xOffset = EnumFacing.UP.getFrontOffsetX();
            final float zOffset = EnumFacing.UP.getFrontOffsetZ();
            final float yOffset = EnumFacing.UP.getFrontOffsetY();

            GlStateManager.rotate( 180f, 0.0F, 1.0F, 0.0F);
            GlStateManager.enableRescaleNormal();

            float f = 1.0f/32.0f;
            GlStateManager.translate(-0.75, 0.5, -0.5);
            if(wScale > 1 || hScale > 1){
                f /= 3.3f;
            }else{
                f /= 2.0f;
            }

            GlStateManager.scale(f, f, f);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            textureManager.bindTexture(location);
            GlStateManager.disableAlpha();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);

            // Draw the front
            bufferbuilder.pos(0.0D, 32.0D*hScale, -1.0D).tex(1.0D, 0.0D).normal(xOffset, yOffset, zOffset).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, -1.0D).tex(0.0D, 0.0D).normal(xOffset, yOffset, zOffset).endVertex();
            bufferbuilder.pos(32.0D*wScale, 0.0D, -1.0D).tex(0.0D, 1.0D).normal(xOffset, yOffset, zOffset).endVertex();
            bufferbuilder.pos(0.0D, 0.0D, -1.0D).tex(1.0D, 1.0D).normal(xOffset, yOffset, zOffset).endVertex();
            tessellator.draw();

            // Draw the back and sides
            final double sideWidth = 1.0D/16.0D;
            bufferbuilder = tessellator.getBuffer();
            textureManager.bindTexture(backLocation);
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(0.0D, 0.0D, 1.0F).tex(0.0D, 0.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 0.0D, 1.0F).tex(1.0D, 0.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, 1.0F).tex(1.0D, 1.0D).endVertex();
            bufferbuilder.pos(0.0D, 32.0D*hScale, 1.0F).tex(0.0D, 1.0D).endVertex();

            // Sides
            bufferbuilder.pos(0.0D, 0.0D, 1.0F).tex(sideWidth, 0.0D).endVertex();
            bufferbuilder.pos(0.0D, 32.0D*hScale, 1.0F).tex(sideWidth, 1.0D).endVertex();
            bufferbuilder.pos(0.0D, 32.0D*hScale, -1.0F).tex(0.0D, 1.0D).endVertex();
            bufferbuilder.pos(0.0D, 0.0D, -1.0F).tex(0.0D, 0.0D).endVertex();

            bufferbuilder.pos(0.0D, 32.0D*hScale, 1.0F).tex(0.0D, 0.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, 1.0F).tex(1.0D, 0.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, -1.0F).tex(1.0D, sideWidth).endVertex();
            bufferbuilder.pos(0.0D, 32.0D*hScale, -1.0F).tex(0.0D, sideWidth).endVertex();

            bufferbuilder.pos(32.0D*wScale, 0.0D, -1.0F).tex(0.0D, 0.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, -1.0F).tex(0.0D, 1.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 32.0D*hScale, 1.0F).tex(sideWidth, 1.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 0.0D, 1.0F).tex(sideWidth, 0.0D).endVertex();

            bufferbuilder.pos(0.0D, 0.0D, -1.0F).tex(0.0D, 1.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 0.0D, -1.0F).tex(1.0D, 1.0D).endVertex();
            bufferbuilder.pos(32.0D*wScale, 0.0D, 1.0F).tex(1.0D, 1.0D-sideWidth).endVertex();
            bufferbuilder.pos(0.0D, 0.0D, 1.0F).tex(0.0D, 1.0D-sideWidth).endVertex();

            tessellator.draw();
            GlStateManager.enableAlpha();

            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }


        public void close() {
//            this.canvasTexture.close();
        }
    }
}