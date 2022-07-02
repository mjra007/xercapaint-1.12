package xerca.xercapaint.common.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.registries.IForgeRegistryEntry;
import xerca.xercapaint.common.item.ItemCanvas;

public class RecipeCanvasCloning  extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack orgCanvas = ItemStack.EMPTY;
        ItemStack freshCanvas = ItemStack.EMPTY;

        for(int j = 0; j < inv.getSizeInventory(); ++j) {
            ItemStack itemstack1 = inv.getStackInSlot(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.getItem() instanceof ItemCanvas && itemstack1.hasTagCompound()  ) {
                    if (!orgCanvas.isEmpty()) {
                        return false;
                    }
                    if (!freshCanvas.isEmpty() && !((ItemCanvas)freshCanvas.getItem()).getCanvasType().equals(((ItemCanvas) itemstack1.getItem()).getCanvasType())){
                        return false;
                    }

                    orgCanvas = itemstack1;
                } else if (itemstack1.getItem() instanceof ItemCanvas && !itemstack1.hasTagCompound()) {
                    if (!freshCanvas.isEmpty()) {
                        return false;
                    }
                    if (!orgCanvas.isEmpty() && !((ItemCanvas)orgCanvas.getItem()).getCanvasType().equals(((ItemCanvas) itemstack1.getItem()).getCanvasType())){
                        return false;
                    }

                    freshCanvas = itemstack1;
                }
            }
        }

        return !orgCanvas.isEmpty() && !freshCanvas.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack orgCanvas = ItemStack.EMPTY;
        ItemStack freshCanvas = ItemStack.EMPTY;

        for(int j = 0; j < inv.getSizeInventory(); ++j) {
            ItemStack itemstack1 = inv.getStackInSlot(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.getItem() instanceof ItemCanvas && itemstack1.hasTagCompound()  ) {
                    if (!orgCanvas.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    if (!freshCanvas.isEmpty() && !((ItemCanvas)freshCanvas.getItem()).getCanvasType().equals(((ItemCanvas) itemstack1.getItem()).getCanvasType())){
                        return ItemStack.EMPTY;
                    }

                    orgCanvas = itemstack1;
                } else if (itemstack1.getItem() instanceof ItemCanvas && !itemstack1.hasTagCompound()) {
                    if (!freshCanvas.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    if (!orgCanvas.isEmpty() && !((ItemCanvas)orgCanvas.getItem()).getCanvasType().equals(((ItemCanvas) itemstack1.getItem()).getCanvasType())){
                        return ItemStack.EMPTY;
                    }

                    freshCanvas = itemstack1;
                }
            }
        }

        if (!orgCanvas.isEmpty() && orgCanvas.hasTagCompound() && !freshCanvas.isEmpty() && !freshCanvas.hasTagCompound() ) {
            ItemStack resultStack = new ItemStack(orgCanvas.getItem());
            NBTTagCompound nbttagcompound = orgCanvas.getTagCompound().copy();
            nbttagcompound.setInteger("generation", orgCanvas.getTagCompound().getInteger("generation") + 1);
            resultStack.setTagCompound(nbttagcompound);
            return resultStack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean canFit(int width, int height) {
         return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            ItemStack itemstack1 = inv.getStackInSlot(i);
            if (itemstack1.getItem() instanceof ItemCanvas && itemstack1.hasTagCompound()  ) {

                    nonnulllist.set(i,itemstack1.copy());
            }
        }
        return nonnulllist;

    }

    public static class Factory implements IRecipeFactory {
        @Override
        public IRecipe parse(JsonContext context, JsonObject json) {
            return new RecipeCanvasCloning();
        }
    }
}
