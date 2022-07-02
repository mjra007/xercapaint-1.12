package xerca.xercapaint.common.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemHangingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xerca.xercapaint.client.CanvasItemRenderer;
import xerca.xercapaint.common.CanvasType;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.entity.EntityCanvas;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCanvas extends ItemHangingEntity {
    private CanvasType canvasType;

    ItemCanvas(String name, CanvasType canvasType) {
        super(EntityCanvas.class);
        this.setRegistryName(name);
        this.setUnlocalizedName(name);
        this.setCreativeTab(Items.paintTab);
        this.setMaxStackSize(1);
        this.canvasType = canvasType;

        this.setTileEntityItemStackRenderer(new CanvasItemRenderer());
        this.addPropertyOverride(new ResourceLocation(XercaPaint.MODID, "drawn"), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                if(!stack.hasTagCompound()) return 0.0f;
                else return 1.0F;
            }
        });
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
        XercaPaint.proxy.showCanvasGui(playerIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        ItemStack itemstack = player.getHeldItem(hand);
        BlockPos blockpos = pos.offset(facing);

        if(player!=null && !this.canPlace(player, facing, itemstack, blockpos)){
            XercaPaint.proxy.showCanvasGui(player);
            return EnumActionResult.SUCCESS;
        }else {
            NBTTagCompound tag = itemstack.getTagCompound();
            if(tag == null || !tag.hasKey("pixels") || !tag.hasKey("name")){
                return EnumActionResult.SUCCESS;
            }

            EntityCanvas entityCanvas = new EntityCanvas(worldIn, tag, blockpos, facing, canvasType);

            if (entityCanvas.onValidSurface())
            {
                if (!worldIn.isRemote)
                {
                    entityCanvas.playPlaceSound();
                    worldIn.spawnEntity(entityCanvas);
                }

                itemstack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }
    }

    private boolean canPlace(EntityPlayer player, EnumFacing facing, ItemStack itemstack, BlockPos blockpos) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP && player.canPlayerEdit(blockpos, facing, itemstack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            String s = tag.getString("author");

            if (!StringUtils.isNullOrEmpty(s)) {
                tooltip.add(I18n.format("canvas.byAuthor", s));
            }

            int generation = tag.getInteger("generation");
            //generation = 0 means empty, 1 means original, more means copy
            if(generation > 0){
                tooltip.add((TextFormatting.GRAY+I18n.format("canvas.generation." + (generation - 1))));
            }
        }else{
            tooltip.add(TextFormatting.GRAY+I18n.format("canvas.empty") );
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if(tag != null){
                String s = tag.getString("title");
                if (!StringUtils.isNullOrEmpty(s)) {
                    return s;
                }
            }
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        if(stack.hasTagCompound()){
            NBTTagCompound tag = stack.getTagCompound();
            if(tag != null) {
                int generation = tag.getInteger("generation");
                return generation > 0;
            }
        }
        return false;
    }

    public int getWidth() {
        return CanvasType.getWidth(canvasType);
    }

    public int getHeight() {
        return CanvasType.getHeight(canvasType);
    }

    public CanvasType getCanvasType() {
        return canvasType;
    }

}
