package com.lovetropics.minigames.common.content.river_race;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.CustomItemRenderers;
import com.lovetropics.minigames.common.content.river_race.behaviour.StartMicrogamesAction;
import com.lovetropics.minigames.common.content.river_race.behaviour.RiverRaceMerchantBehavior;
import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import com.lovetropics.minigames.common.content.river_race.behaviour.VictoryPointsBehavior;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlock;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlockEntity;
import com.lovetropics.minigames.common.content.river_race.block.TriviaChestBlock;
import com.lovetropics.minigames.common.content.river_race.block.TriviaChestBlockEntity;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RiverRace {
    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();
    public static final GameBehaviorEntry<TriviaBehaviour> TRIVIA_BEHAVIOUR = REGISTRATE.object("trivia").behavior(TriviaBehaviour.CODEC).register();
    public static final GameBehaviorEntry<StartMicrogamesAction> START_MICROGAMES_ACTION = REGISTRATE.object("start_microgames").behavior(StartMicrogamesAction.CODEC).register();
    public static final GameBehaviorEntry<VictoryPointsBehavior> VICTORY_POINTS_BEHAVIOR = REGISTRATE.object("victory_points").behavior(VictoryPointsBehavior.CODEC).register();
    public static final GameBehaviorEntry<RiverRaceMerchantBehavior> RIVER_RACE_MERCHANT_BEHAVIOR = REGISTRATE.object("river_race_merchant").behavior(RiverRaceMerchantBehavior.CODEC).register();

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

    public static final BlockEntry<TriviaChestBlock> TRIVIA_CHEST = REGISTRATE
            .block("trivia_chest", TriviaChestBlock::new)
            .initialProperties(() -> Blocks.STONE)
            .properties(BlockBehaviour.Properties::noLootTable)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().getBuilder(ctx.getName()).texture("particle", prov.modLoc("block/trivia_reward"))))
            .blockEntity(TriviaChestBlockEntity::new)
            .build()
            .item()
            .clientExtension(() -> CustomItemRenderers::triviaChestItem)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), "item/chest")
                    .texture("particle", prov.modLoc("block/trivia_reward")))
            .build()
            .addMiscData(ProviderType.LANG, prov -> {
                prov.add(LoveTropics.ID + ".container.triviaChest", "Trivia Chest");
                prov.add(LoveTropics.ID + ".container.triviaChestDouble", "Large Trivia Chest");
            })
            .register();
    public static final BlockEntityEntry<TriviaBlockEntity> TRIVIA_BLOCK_ENTITY = BlockEntityEntry.cast(REGISTRATE.get("trivia_gate", Registries.BLOCK_ENTITY_TYPE));
    public static final BlockEntityEntry<TriviaChestBlockEntity> TRIVIA_CHEST_BLOCK_ENTITY = BlockEntityEntry.cast(REGISTRATE.get("trivia_chest", Registries.BLOCK_ENTITY_TYPE));


    public static void init() {
    }

}
