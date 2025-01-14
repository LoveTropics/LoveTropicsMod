package com.lovetropics.minigames.common.content.river_race;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.CustomItemRenderers;
import com.lovetropics.minigames.common.content.river_race.behaviour.*;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlock;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlockEntity;
import com.lovetropics.minigames.common.content.river_race.block.TriviaChestBlock;
import com.lovetropics.minigames.common.content.river_race.block.TriviaChestBlockEntity;
import com.lovetropics.minigames.common.content.river_race.client_state.RiverRaceClientBarState;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.GameClientTweakEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RiverRace {
    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(LoveTropics.ID);

    public static final GameBehaviorEntry<RiverRaceSetupBehavior> SETUP_BEHAVIOR = REGISTRATE.object("river_race/setup").behavior(RiverRaceSetupBehavior.CODEC).register();
    public static final GameBehaviorEntry<RiverRaceZoneBehavior> ZONE_BEHAVIOR = REGISTRATE.object("river_race/zone").behavior(RiverRaceZoneBehavior.CODEC).register();
    public static final GameBehaviorEntry<RiverRaceSpawnsBehavior> SPAWNS_BEHAVIOUR = REGISTRATE.object("river_race/spawns").behavior(RiverRaceSpawnsBehavior.CODEC).register();
    public static final GameBehaviorEntry<ModifyMaxSpawnsAction> MODIFY_MAX_SPAWNS_ACTION = REGISTRATE.object("river_race/modify_max_spawns").behavior(ModifyMaxSpawnsAction.CODEC).register();
    public static final GameBehaviorEntry<TriviaBehaviour> TRIVIA_BEHAVIOUR = REGISTRATE.object("trivia").behavior(TriviaBehaviour.CODEC).register();
    public static final GameBehaviorEntry<StartMicrogamesAction> START_MICROGAMES_ACTION = REGISTRATE.object("start_microgames").behavior(StartMicrogamesAction.CODEC).register();
    public static final GameBehaviorEntry<VictoryPointsBehavior> VICTORY_POINTS_BEHAVIOR = REGISTRATE.object("victory_points").behavior(VictoryPointsBehavior.CODEC).register();
    public static final GameBehaviorEntry<RiverRaceMerchantBehavior> RIVER_RACE_MERCHANT_BEHAVIOR = REGISTRATE.object("river_race_merchant").behavior(RiverRaceMerchantBehavior.CODEC).register();
    public static final GameBehaviorEntry<OverlordBehavior> OVERLORD_BEHAVIOR = REGISTRATE.object("river_race_overlord").behavior(OverlordBehavior.CODEC).register();
    public static final GameBehaviorEntry<ProgressBehaviour> RIVER_RACE_PROGRESS_BEHAVIOUR = REGISTRATE.object("river_race_progress").behavior(ProgressBehaviour.CODEC).register();
    public static final GameBehaviorEntry<CollectablesBehaviour> COLLECTABLES_BEHAVIOUR = REGISTRATE.object("river_race_collectables").behavior(CollectablesBehaviour.CODEC).register();
    public static final GameBehaviorEntry<UnlockZoneAction> UNLOCK_ZONE_ACTION = REGISTRATE.object("unlock_zone").behavior(UnlockZoneAction.CODEC).register();
    public static final GameBehaviorEntry<KillAboveVoidBehavior> KILL_ABOVE_VOID_BEHAVIOR = REGISTRATE.object("kill_above_void").behavior(KillAboveVoidBehavior.CODEC).register();
    public static final GameBehaviorEntry<RewardsFromMicrogameBehavior> REWARDS_FROM_MICROGAME_BEHAVIOR = REGISTRATE.object("rewards_from_microgame").behavior(RewardsFromMicrogameBehavior.CODEC).register();

    public static final GameClientTweakEntry<RiverRaceClientBarState> BAR_STATE = REGISTRATE.object("river_race_bar")
            .clientState(RiverRaceClientBarState.CODEC).streamCodec(RiverRaceClientBarState.STREAM_CODEC)
            .register();

    public static final BlockEntry<TriviaBlock.GateTriviaBlock> TRIVIA_GATE = REGISTRATE
            .block("trivia_gate", TriviaBlock.GateTriviaBlock::new)
            .initialProperties(() -> Blocks.BEDROCK)
            .properties(BlockBehaviour.Properties::noLootTable)
            .blockstate((ctx, prov) -> triviaBlockModel(ctx, prov, true))
            .simpleItem()
            .register();

    public static final BlockEntry<TriviaBlock.CollectableTriviaBlock> TRIVIA_COLLECTABLE = REGISTRATE
            .block("trivia_collectable", TriviaBlock.CollectableTriviaBlock::new)
            .initialProperties(() -> Blocks.BEDROCK)
            .properties(BlockBehaviour.Properties::noLootTable)
            .blockstate((ctx, prov) -> triviaBlockModel(ctx, prov, false))
            .simpleItem()
            .register();
    public static final BlockEntry<TriviaBlock.VictoryTriviaBlock> TRIVIA_VICTORY = REGISTRATE
            .block("trivia_victory", TriviaBlock.VictoryTriviaBlock::new)
            .initialProperties(() -> Blocks.BEDROCK)
            .properties(BlockBehaviour.Properties::noLootTable)
            .blockstate((ctx, prov) -> triviaBlockModel(ctx, prov, true))
            .simpleItem()
            .register();

    private static void triviaBlockModel(DataGenContext<Block, ?> ctx, RegistrateBlockstateProvider prov, boolean useInactiveTexture) {
		BlockModelBuilder activeModel = prov.models().withExistingParent(ctx.getName(), prov.modLoc("block/cube_glow"))
                .texture("all", prov.modLoc("block/" + ctx.getName()))
                .texture("glow", prov.modLoc("block/trivia_glow"));
        BlockModelBuilder inactiveModel = prov.models().cubeAll(ctx.getName() + "_inactive", prov.modLoc(useInactiveTexture ? "block/trivia_inactive" : "block/" + ctx.getName()));
        prov.getVariantBuilder(ctx.get())
                .partialState().with(TriviaBlock.ANSWERED, false).setModels(new ConfiguredModel(activeModel))
                .partialState().with(TriviaBlock.ANSWERED, true).setModels(new ConfiguredModel(inactiveModel));
    }

    public static final BlockEntry<TriviaChestBlock> TRIVIA_CHEST = REGISTRATE
            .block("trivia_chest", TriviaChestBlock::new)
            .initialProperties(() -> Blocks.BEDROCK)
            .properties(BlockBehaviour.Properties::noLootTable)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().getBuilder(ctx.getName()).texture("particle", prov.modLoc("block/trivia_victory"))))
            .blockEntity(TriviaChestBlockEntity::new)
            .build()
            .item()
            .clientExtension(() -> CustomItemRenderers::triviaChestItem)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), "item/chest")
                    .texture("particle", prov.modLoc("block/trivia_victory")))
            .build()
            .addMiscData(ProviderType.LANG, prov -> prov.add(LoveTropics.ID + ".container.triviaChest", "Trivia Chest"))
            .register();

    public static final BlockEntityEntry<TriviaBlockEntity> TRIVIA_BLOCK_ENTITY =
            REGISTRATE.blockEntity("trivia_block_entity", TriviaBlockEntity::new)
                    .validBlocks(TRIVIA_GATE, TRIVIA_COLLECTABLE, TRIVIA_VICTORY).register();
    public static final BlockEntityEntry<TriviaChestBlockEntity> TRIVIA_CHEST_BLOCK_ENTITY = BlockEntityEntry.cast(REGISTRATE.get("trivia_chest", Registries.BLOCK_ENTITY_TYPE));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> COLLECTABLE_MARKER = DATA_COMPONENTS.registerComponentType(
            "river_race_collectable_marker",
            builder -> builder.persistent(Unit.CODEC)
    );

    public static void init() {
    }

}
