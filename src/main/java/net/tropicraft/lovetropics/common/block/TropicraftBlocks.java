package net.tropicraft.lovetropics.common.block;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tropicraft.lovetropics.Constants;
import net.tropicraft.lovetropics.LoveTropics;
import net.tropicraft.lovetropics.common.item.TropicraftItems;

public class TropicraftBlocks {
    
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Constants.MODID);
    public static final DeferredRegister<Item> ITEMS = TropicraftItems.ITEMS;

    public static final RegistryObject<Block> WATER_BARRIER = register(
            "water_barrier", () -> new WaterBarrierBlock(Block.Properties.from(Blocks.BARRIER).noDrops()), LoveTropics.LOVE_TROPICS_ITEM_GROUP);

    public static final Map<TrashType, RegistryObject<CustomShapeBlock>> TRASH = Arrays.<TrashType>stream(TrashType.values())
            .collect(Collectors.toMap(Function.identity(), t -> register(t.getId(), Builder.trash(t), LoveTropics.LOVE_TROPICS_ITEM_GROUP),
                    (f1, f2) -> { throw new IllegalStateException(); }, () -> new EnumMap<>(TrashType.class)));
    
    public static final RegistryObject<DonationBlock> DONATION = register(
            "donation", () -> new DonationBlock(Block.Properties.from(Blocks.BEDROCK).noDrops()), LoveTropics.LOVE_TROPICS_ITEM_GROUP);
    public static final RegistryObject<Block> BUOY = register(
            "buoy", () -> new CustomShapeBlock(
                    VoxelShapes.or(
                            Block.makeCuboidShape(2, 0, 2, 14, 3, 14),
                            Block.makeCuboidShape(3, 3, 3, 13, 14, 13)),
                    Block.Properties.from(Blocks.BEACON)), LoveTropics.LOVE_TROPICS_ITEM_GROUP);
    
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup) {
        return register(name, sup, TropicraftBlocks::itemDefault);
    }
    
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup, Supplier<Callable<ItemStackTileEntityRenderer>> renderMethod) {
        return register(name, sup, block -> item(block, renderMethod));
    }
    
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup, ItemGroup tab) {
        return register(name, sup, block -> item(block, tab));
    }
    
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup, Function<RegistryObject<T>, Supplier<? extends Item>> itemCreator) {
        RegistryObject<T> ret = registerNoItem(name, sup);
        ITEMS.register(name, itemCreator.apply(ret));
        return ret;
    }
    
    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<? extends T> sup) {
        return BLOCKS.register(name, sup);
    }

    private static Supplier<BlockItem> itemDefault(final RegistryObject<? extends Block> block) {
        return item(block, LoveTropics.TROPICRAFT_ITEM_GROUP);
    }

    private static Supplier<BlockItem> item(final RegistryObject<? extends Block> block, final Supplier<Callable<ItemStackTileEntityRenderer>> renderMethod) {
        return () -> new BlockItem(block.get(), new Item.Properties().group(LoveTropics.TROPICRAFT_ITEM_GROUP).setTEISR(renderMethod));
    }

    private static Supplier<BlockItem> item(final RegistryObject<? extends Block> block, final ItemGroup itemGroup) {
        return () -> new BlockItem(block.get(), new Item.Properties().group(itemGroup));
    }
}
