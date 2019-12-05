package net.tropicraft.lovetropics.common.block;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tterrag.registrate.Registrate;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.fml.RegistryObject;
import net.tropicraft.lovetropics.Constants;
import net.tropicraft.lovetropics.LoveTropics;
import net.tropicraft.lovetropics.common.block.tileentity.DonationTileEntity;

public class LoveTropicsBlocks {
    
    public static final Registrate REGISTRATE = Registrate.create(Constants.MODID);
    
    public static final RegistryObject<WaterBarrierBlock> WATER_BARRIER = REGISTRATE.block("water_barrier", WaterBarrierBlock::new)
            .properties(p -> Block.Properties.from(Blocks.BARRIER).noDrops())
            .item()
                .properties(p -> p.group(LoveTropics.LOVE_TROPICS_ITEM_GROUP))
                .build()
            .register();

    public static final Map<TrashType, RegistryObject<TrashBlock>> TRASH = Arrays.<TrashType>stream(TrashType.values())
            .collect(Collectors.toMap(Function.identity(), t -> REGISTRATE.block(t.getId(), p -> new TrashBlock(t.getShape(), p))
                    .properties(p -> Block.Properties.create(Material.PLANTS).doesNotBlockMovement())
                    .item()
                        .properties(p -> p.group(LoveTropics.LOVE_TROPICS_ITEM_GROUP))
                        .build()
                    .register(),
                    (f1, f2) -> { throw new IllegalStateException(); }, () -> new EnumMap<>(TrashType.class)));
    
    public static final RegistryObject<DonationBlock> DONATION = REGISTRATE.block("donation", DonationBlock::new)
            .properties(p -> Block.Properties.from(Blocks.BEDROCK).noDrops())
            .tileEntity(DonationTileEntity::new)
            .item()
                .properties(p -> p.group(LoveTropics.LOVE_TROPICS_ITEM_GROUP))
                .build()
            .register();
    
    public static final RegistryObject<TileEntityType<DonationTileEntity>> DONATION_TILE = REGISTRATE.get("donation", TileEntityType.class);
    
    public static final RegistryObject<CustomShapeBlock> BUOY = REGISTRATE.block("buoy", p -> new CustomShapeBlock(
                    VoxelShapes.or(
                            Block.makeCuboidShape(2, 0, 2, 14, 3, 14),
                            Block.makeCuboidShape(3, 3, 3, 13, 14, 13)),
                    p))
            .properties(p -> Block.Properties.from(Blocks.BARRIER).noDrops())
            .item()
                .properties(p -> p.group(LoveTropics.LOVE_TROPICS_ITEM_GROUP))
                .build()
            .register();
    
    public static void init() {}
}
