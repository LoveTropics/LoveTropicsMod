package net.tropicraft.core.common.block;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.LogBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.trees.Tree;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;

@SuppressWarnings("unused")
public class Builder {

    public static Supplier<Block> block(final Block.Properties properties) {
        return block(() -> properties);
    }
    
    public static Supplier<Block> block(final Supplier<Block.Properties> properties) {
        return block(Block::new, properties);
    }
    
    public static <T extends Block> Supplier<T> block(final Function<Block.Properties, T> ctor, final Block.Properties properties) {
        return block(ctor, () -> properties);
    }
    
    public static <T extends Block> Supplier<T> block(final Function<Block.Properties, T> ctor, final Supplier<Block.Properties> properties) {
        return () -> ctor.apply(properties.get());
    }

    public static Supplier<CustomShapeBlock> trash(TrashType type) {
        return block(p -> new TrashBlock(type.getShape(), p), prop(Material.PLANTS).doesNotBlockMovement());
    }

    private static Block.Properties prop(final Material material) {
        return Block.Properties.create(material);
    }

    private static Block.Properties prop(final Material material, final MaterialColor color) {
        return Block.Properties.create(material, color);
    }
    
    interface ComposableSupplier<T> extends Supplier<T> {
        
        default <R> ComposableSupplier<R> then(Function<T, R> func) {
            return () -> func.apply(get());
        }
    }

    private static ComposableSupplier<Block.Properties> lazyProp(final Material material) {
        return () -> prop(material);
    }

    private static ComposableSupplier<Block.Properties> lazyProp(final Material material, final MaterialColor color) {
        return () -> prop(material, color);
    }
    
    private static ComposableSupplier<Block.Properties> lazyProp(final Supplier<? extends Block> source) {
        return () -> {
            Objects.requireNonNull(source.get(), "Must register source block before using it");
            return Block.Properties.from(source.get());
        };
    }
}
