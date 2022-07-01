package xerca.xercapaint.client;

import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import xerca.xercapaint.common.item.ItemCanvas;

public class CanvasItemRenderer extends TileEntityItemStackRenderer
{

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        if (stack.getItem() instanceof ItemCanvas) {
            NBTTagCompound nbt = stack.getTagCompound();
            if(nbt != null){
                ItemCanvas itemCanvas = (ItemCanvas) stack.getItem();
                RenderEntityCanvas.Instance canvasIns = RenderEntityCanvas.theInstance.getMapRendererInstance(nbt, itemCanvas.getWidth(), itemCanvas.getHeight());
                if(canvasIns != null){
                     canvasIns.renderItemStack();
                }
            }
        }
    }

}