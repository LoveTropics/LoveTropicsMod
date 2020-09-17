package com.lovetropics.minigames.common.block;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.block.tileentity.DonationTileEntity;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.RegistryEntry;

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
    
    public static final RegistryEntry<WaterBarrierBlock> WATER_BARRIER = REGISTRATE.block("water_barrier", WaterBarrierBlock::new)
            .properties(p -> Block.Properties.from(Blocks.BARRIER).noDrops())
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), 
                    prov.models().getBuilder(ctx.getName()).texture("particle", new ResourceLocation("item/barrier"))))
            .item()
                .model((ctx, prov) -> prov.generated(ctx::getEntry, new ResourceLocation("block/water_still"), new ResourceLocation("item/barrier")))
                .build()
            .register();

    public static final Map<TrashType, RegistryEntry<TrashBlock>> TRASH = Arrays.<TrashType>stream(TrashType.values())
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
    
    public static final RegistryEntry<DonationBlock> DONATION = REGISTRATE.block("donation", DonationBlock::new)
            .properties(p -> Block.Properties.from(Blocks.BEDROCK).noDrops())
            .tileEntity(DonationTileEntity::new)
            .simpleItem()
            .register();
    
    public static final RegistryEntry<TileEntityType<DonationTileEntity>> DONATION_TILE = REGISTRATE.get("donation", TileEntityType.class);
    
    public static final RegistryEntry<CustomShapeBlock> BUOY = REGISTRATE.block("buoy", p -> new CustomShapeBlock(
                    VoxelShapes.or(
                            Block.makeCuboidShape(2, 0, 2, 14, 3, 14),
                            Block.makeCuboidShape(3, 3, 3, 13, 14, 13)),
                    p))
            .properties(p -> Block.Properties.from(Blocks.BARRIER).noDrops())
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models()
                    .withExistingParent(ctx.getName(), new ResourceLocation("block/block"))
                        .ao(false)
                        .texture("beacon", new ResourceLocation("block/beacon"))
                        .texture("base", new ResourceLocation("block/dark_prismarine"))
                        .texture("particle", new ResourceLocation("block/dark_prismarine"))
                        .element()
                            .from(2, 0, 2)
                            .to(14, 3, 14)
                            .textureAll("#base")
                            .face(Direction.DOWN).cullface(Direction.DOWN).end()
                            .end()
                        .element()
                            .from(3, 3, 3)
                            .to(13, 14, 13)
                            .textureAll("beacon")
                            .end()))
            .simpleItem()
            .register();
    
    public static void init() {}
}
