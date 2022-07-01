package xerca.xercapaint.common.item;

import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xerca.xercapaint.common.XercaPaint;

import java.util.Arrays;
import java.util.List;

public class ItemPalette extends Item {
    ItemPalette(String name) {
        this.setRegistryName(name);
        this.setUnlocalizedName(name);
        this.setCreativeTab(Items.paintTab);
        this.setMaxStackSize(1);

        this.addPropertyOverride(new ResourceLocation(XercaPaint.MODID, "colors"), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return (float)ItemPalette.basicColorCount(stack)/16.0F;
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
        XercaPaint.proxy.showCanvasGui(playerIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(creativeTab)) {
            // Empty palette
            items.add(new ItemStack(this));

            // Full palette
            ItemStack fullPalette = new ItemStack(this);
            byte[] basicColors = new byte[16];
            Arrays.fill(basicColors, (byte)1);
            NBTTagCompound tag = fullPalette.getTagCompound();
            if(tag == null){
                tag = new NBTTagCompound();
                fullPalette.setTagCompound(tag);
            }
            tag.setByteArray("basic", basicColors);
            items.add(fullPalette);
        }
    }

    public static boolean isFull(ItemStack stack){
        return basicColorCount(stack) == 16;
    }

    public static int basicColorCount(ItemStack stack){
        if(stack.getItem() != Items.ITEM_PALETTE){
            return 0;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if(tag != null && tag.getKeySet().contains("basic")){
            byte[] basicColors = tag.getByteArray("basic");
            if (basicColors.length == 16) {
                int basicCount = 0;
                for(byte basicColor : basicColors){
                    basicCount += basicColor;
                }
                return basicCount;
            }
        }
        return 0;
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if(tag != null){
                byte[] basicColors = tag.getByteArray("basic");
                if (basicColors.length == 16) {
                    int basicCount = 0;
                    for(byte basicColor : basicColors){
                        basicCount += basicColor;
                    }
                    tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("palette.basic_count", String.valueOf(basicCount)));
                }

                int[] ns = tag.getIntArray("n");
                if (ns.length == 12){
                    int fullCount = 0;

                    for(int n : ns){
                        if(n > 0){
                            fullCount++;
                        }
                    }
                    tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("palette.custom_count", String.valueOf(fullCount)));
                }
            }
        }
        else{
            tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("palette.empty"));
        }
    }
}
