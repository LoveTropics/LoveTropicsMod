package com.lovetropics.minigames.common.block;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.block.tileentity.DonationTileEntity;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.TileEntityEntry;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;

public class LoveTropicsBlocks {
    
    public static final Registrate REGISTRATE = LoveTropics.registrate();

    public static final Map<TrashType, BlockEntry<TrashBlock>> TRASH = Arrays.<TrashType>stream(TrashType.values())
            .collect(Collectors.toMap(Function.identity(), t -> REGISTRATE.block(t.getId(), p -> new TrashBlock(t.getShape(), p))
                    .properties(p -> Block.Properties.create(Material.PLANTS).doesNotBlockMovement())
                    .addLayer(() -> RenderType::getCutout)
                    .blockstate((ctx, prov) -> prov.getVariantBuilder(t.get()) // TODO make horizontalBlock etc support this case
                            .forAllStatesExcept(state -> ConfiguredModel.builder()
                                    .modelFile(prov.models().getExistingFile(prov.modLoc(t.getId())))
                                    .rotationY(((int) state.get(BlockStateProperties.HORIZONTAL_FACING).getHorizontalAngle() + 180) % 360)
                                    .build(), LadderBlock.WATERLOGGED))
                    .item()
                        .model((ctx, prov) -> prov.blockItem(t).transforms()
                                .transform(Perspective.GUI)
                                    .rotation(30, 225, 0)
                                    .translation(0, t.getModelYOffset(), 0)
                                    .scale(t.getModelScale(0.625f))
                                    .end()
                                .transform(Perspective.GROUND)
                                    .translation(0, t.getModelYOffset(), 0)
                                    .scale(t.getModelScale(0.5f))
                                    .end()
                                .transform(Perspective.FIRSTPERSON_RIGHT)
                                    .rotation(0, 45, 0)
                                    .translation(0, t.getModelYOffset(), 0)
                                    .scale(t.getModelScale(0.4f))
                                    .end()
                                .transform(Perspective.FIRSTPERSON_LEFT)
                                    .rotation(0, 255, 0)
                                    .translation(0, t.getModelYOffset(), 0)
                                    .scale(t.getModelScale(0.4f))
                                    .end()
                                .transform(Perspective.THIRDPERSON_RIGHT)
                                    .rotation(75, 45, 0)
                                    .translation(0, 2.5f, t.getModelYOffset())
                                    .scale(t.getModelScale(0.375f))
                                    .end())
                            .build()
                    .register(),
                    (f1, f2) -> { throw new IllegalStateException(); }, () -> new EnumMap<>(TrashType.class)));
    
    public static final BlockEntry<DonationBlock> DONATION = REGISTRATE.block("donation", DonationBlock::new)
            .properties(p -> Block.Properties.from(Blocks.BEDROCK).noDrops())
            .simpleTileEntity(DonationTileEntity::new)
            .simpleItem()
            .register();
    
    public static final TileEntityEntry<DonationTileEntity> DONATION_TILE = TileEntityEntry.cast(REGISTRATE.get("donation", TileEntityType.class));

    
    public static void init() {}
}
