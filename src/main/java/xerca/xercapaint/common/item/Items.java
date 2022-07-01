package xerca.xercapaint.common.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import xerca.xercapaint.common.CanvasType;
import xerca.xercapaint.common.PaintCreativeTab;
import xerca.xercapaint.common.XercaPaint;

@GameRegistry.ObjectHolder(XercaPaint.MODID)
public final class Items {
    public static final ItemPalette ITEM_PALETTE = null;
    public static final ItemCanvas ITEM_CANVAS = null;
    public static final ItemCanvas ITEM_CANVAS_LARGE = null;
    public static final ItemCanvas ITEM_CANVAS_LONG = null;
    public static final ItemCanvas ITEM_CANVAS_TALL = null;

    public static PaintCreativeTab paintTab;

    public static void initModels() {
        initModel(ITEM_PALETTE, "item_palette");
        initModel(ITEM_CANVAS, "item_canvas");
        initModel(ITEM_CANVAS_LARGE, "item_canvas_large");
        initModel(ITEM_CANVAS_LONG, "item_canvas_long");
        initModel(ITEM_CANVAS_TALL, "item_canvas_tall");
    }

    private static void initModel(Item item, String location) {
        initModel(item, location, 0);
    }

    private static void initModel(Item item, String location, int meta) {
//		System.out.println("Location: " + location + " regName: " + item.getRegistryName());
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @Mod.EventBusSubscriber(modid = XercaPaint.MODID)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            paintTab = new PaintCreativeTab();

            event.getRegistry().registerAll(
                    new ItemPalette("item_palette"),
                    new ItemCanvas("item_canvas", CanvasType.SMALL),
                    new ItemCanvas("item_canvas_large", CanvasType.LARGE),
                    new ItemCanvas("item_canvas_long", CanvasType.LONG),
                    new ItemCanvas("item_canvas_tall", CanvasType.TALL)
            );

        }

        @SubscribeEvent
        public static void onModelRegistry(ModelRegistryEvent event) {
            initModels();
        }
    }

}