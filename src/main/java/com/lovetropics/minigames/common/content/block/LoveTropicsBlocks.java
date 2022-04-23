package com.lovetropics.minigames.common.content.block;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.block.TrashBlock.Attachment;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.block.Block;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LoveTropicsBlocks {
    
    public static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

    public static final Map<TrashType, BlockEntry<TrashBlock>> TRASH = Arrays.<TrashType>stream(TrashType.values())
            .collect(Collectors.toMap(Function.identity(), t -> REGISTRATE.block(t.getId(), p -> new TrashBlock(t, p))
                    .properties(p -> Block.Properties.of(Material.PLANT).noCollission())
                    .addLayer(() -> RenderType::cutout)
                    .blockstate((ctx, prov) -> prov.getVariantBuilder(t.get()) // TODO make horizontalBlock etc support this case
                            .forAllStatesExcept(state -> ConfiguredModel.builder()
                                    .modelFile(prov.models().getExistingFile(prov.modLoc(t.getId())))
                                    .rotationX(state.getValue(TrashBlock.ATTACHMENT) == Attachment.WALL ? 90 : state.getValue(TrashBlock.ATTACHMENT) == Attachment.FLOOR ? 0 : 180)
                                    .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()) % 360)
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
    
    public static void init() {}
}
