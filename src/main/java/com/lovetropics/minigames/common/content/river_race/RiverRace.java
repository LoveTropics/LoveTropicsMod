package com.lovetropics.minigames.common.content.river_race;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.river_race.behaviour.MicrogamesBehaviour;
import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlock;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlockEntity;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RiverRace {
    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();
    public static final GameBehaviorEntry<TriviaBehaviour> TRIVIA_BEHAVIOUR = REGISTRATE.object("trivia").behavior(TriviaBehaviour.CODEC).register();
    public static final GameBehaviorEntry<MicrogamesBehaviour> MICROGAMES_BEHAVIOUR = REGISTRATE.object("microgames").behavior(MicrogamesBehaviour.CODEC).register();

    public static final BlockEntry<TriviaBlock.GateTriviaBlock> TRIVIA_GATE = REGISTRATE
            .block("trivia_gate", TriviaBlock.GateTriviaBlock::new)
            .initialProperties(() -> Blocks.STONE)
            .properties(BlockBehaviour.Properties::noLootTable)
            .blockEntity(TriviaBlockEntity::new)
            .build()
            .blockstate((ctx, prox) -> {
                prox.simpleBlock(ctx.get());
            })
            .simpleItem()
            .register();
    public static final BlockEntry<TriviaBlock.CollectableTriviaBlock> TRIVIA_COLLECTABLE = REGISTRATE
            .block("trivia_collectable", TriviaBlock.CollectableTriviaBlock::new)
            .initialProperties(() -> Blocks.STONE)
            .properties(BlockBehaviour.Properties::noLootTable)
            .blockEntity(TriviaBlockEntity::new)
            .build()
            .blockstate((ctx, prox) -> {
                prox.simpleBlock(ctx.get());
            })
            .simpleItem()
            .register();
    public static final BlockEntry<TriviaBlock.RewardTriviaBlock> TRIVIA_REWARD = REGISTRATE
            .block("trivia_reward", TriviaBlock.RewardTriviaBlock::new)
            .initialProperties(() -> Blocks.STONE)
            .properties(BlockBehaviour.Properties::noLootTable)
            .blockEntity(TriviaBlockEntity::new)
            .build()
            .blockstate((ctx, prox) -> {
                prox.simpleBlock(ctx.get());
            })
            .simpleItem()
            .register();

    public static final BlockEntityEntry<TriviaBlockEntity> TRIVIA_BLOCK_ENTITY = BlockEntityEntry.cast(REGISTRATE.get("trivia_gate", Registries.BLOCK_ENTITY_TYPE));


    public static void init() {
    }

}
