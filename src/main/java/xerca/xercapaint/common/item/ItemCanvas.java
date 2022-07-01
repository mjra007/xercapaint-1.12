package xerca.xercapaint.common.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemHangingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xerca.xercapaint.client.CanvasItemRenderer;
import xerca.xercapaint.common.CanvasType;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.entity.EntityCanvas;

import javax.annotation.Nullable;

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

        if (facing != EnumFacing.DOWN && facing != EnumFacing.UP && player.canPlayerEdit(blockpos, facing, itemstack))
        {
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
        else
        {
            return EnumActionResult.FAIL;
        }
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
