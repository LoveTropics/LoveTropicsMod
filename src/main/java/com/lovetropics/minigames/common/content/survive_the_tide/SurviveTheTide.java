package com.lovetropics.minigames.common.content.survive_the_tide;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.render.block.BigRedButtonBlockEntityRenderer;
import com.lovetropics.minigames.client.render.entity.DriftwoodRenderer;
import com.lovetropics.minigames.client.render.entity.LightningArrowRenderer;
import com.lovetropics.minigames.client.render.entity.PlatformRenderer;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.PhasedWeatherControlBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.RevealPlayersBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.RisingPlatformBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.RisingTidesGameBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SttChatBroadcastBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SttPetsBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SttSidebarBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SttWinLogicBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SurviveTheTideRulesetBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SurviveTheTideWeatherControlBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SurviveTheTideWindController;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.WorldBorderGameBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.block.BigRedButtonBlock;
import com.lovetropics.minigames.common.content.survive_the_tide.block.BigRedButtonBlockEntity;
import com.lovetropics.minigames.common.content.survive_the_tide.block.LootDispenserBlock;
import com.lovetropics.minigames.common.content.survive_the_tide.block.LootDispenserBlockEntity;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodEntity;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.LightningArrowEntity;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.PlatformEntity;
import com.lovetropics.minigames.common.content.survive_the_tide.item.AcidRepellentUmbrellaItem;
import com.lovetropics.minigames.common.content.survive_the_tide.item.LightningArrowItem;
import com.lovetropics.minigames.common.content.survive_the_tide.item.PaddleItem;
import com.lovetropics.minigames.common.content.survive_the_tide.item.SuperSunscreenItem;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;

public final class SurviveTheTide {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final BlockEntry<BigRedButtonBlock> BIG_RED_BUTTON = REGISTRATE
			.block("big_red_button", BigRedButtonBlock::new)
			.initialProperties(() -> Blocks.STONE_BUTTON)
			.properties(BlockBehaviour.Properties::noLootTable)
			.blockEntity(BigRedButtonBlockEntity::new)
			.renderer(() -> BigRedButtonBlockEntityRenderer::new)
			.build()
			.blockstate((ctx, prov) -> {
				ResourceLocation texture = prov.mcLoc("block/redstone_block");
				prov.buttonBlock(ctx.get(),
						buttonModel(ctx.getName(), prov, BigRedButtonBlock.HALF_SIZE, BigRedButtonBlock.HALF_SIZE, BigRedButtonBlock.UNPRESSED_DEPTH, texture),
						buttonModel(ctx.getName() + "_pressed", prov, BigRedButtonBlock.HALF_SIZE, BigRedButtonBlock.HALF_SIZE, BigRedButtonBlock.PRESSED_DEPTH, texture)
				);
			})
			.simpleItem()
			.register();

	public static final BlockEntityEntry<BigRedButtonBlockEntity> BIG_RED_BUTTON_ENTITY = BlockEntityEntry.cast(REGISTRATE.get("big_red_button", Registries.BLOCK_ENTITY_TYPE));

	private static BlockModelBuilder buttonModel(String name, RegistrateBlockstateProvider prov, int halfWidth, int halfHeight, int depth, ResourceLocation texture) {
		return prov.models().getBuilder(name)
				.texture("button", texture)
				.texture("surface", prov.mcLoc("block/polished_deepslate"))
				.texture("particle", "#button")
				.element()
				.from(8 - halfWidth, 0, 8 - halfHeight)
				.to(8 + halfWidth, depth, 8 + halfHeight)
				.allFaces((direction, face) -> face.texture("#button"))
				.end()
				.element()
				.from(2, 0, 2)
				.to(14, 1, 14)
				.allFaces((direction, face) -> face.texture("#surface"))
				.end();
	}

	public static final BlockEntry<LootDispenserBlock> LOOT_DISPENSER = REGISTRATE
			.block("loot_dispenser", LootDispenserBlock::new)
			.initialProperties(() -> Blocks.DISPENSER)
			.properties(BlockBehaviour.Properties::noLootTable)
			.blockEntity(LootDispenserBlockEntity::new).build()
			.blockstate((ctx, prov) -> prov.directionalBlock(ctx.get(), prov.models().orientableVertical(
					ctx.getName(),
					prov.mcLoc("block/furnace_side"),
					prov.mcLoc("block/dispenser_front_vertical")
			)))
			.simpleItem()
			.register();

	public static final BlockEntityEntry<LootDispenserBlockEntity> LOOT_DISPENSER_ENTITY = BlockEntityEntry.cast(REGISTRATE.get("loot_dispenser", Registries.BLOCK_ENTITY_TYPE));

	public static final ItemEntry<SuperSunscreenItem> SUPER_SUNSCREEN = REGISTRATE.item("super_sunscreen", SuperSunscreenItem::new)
			.register();

	public static final ItemEntry<AcidRepellentUmbrellaItem> ACID_REPELLENT_UMBRELLA = REGISTRATE.item("acid_repellent_umbrella", AcidRepellentUmbrellaItem::new)
			.model((ctx, prov) -> {})
			.register();

	public static final ItemEntry<PaddleItem> PADDLE = REGISTRATE.item("paddle", PaddleItem::new)
			.model((ctx, prov) -> {})
			.register();

	public static final ItemEntry<LightningArrowItem> LIGHTNING_ARROW = REGISTRATE.item("lightning_arrow", LightningArrowItem::new)
			.tag(ItemTags.ARROWS)
			.register();

	public static final RegistryEntry<EntityType<?>, EntityType<DriftwoodEntity>> DRIFTWOOD = REGISTRATE.entity("driftwood", DriftwoodEntity::new, MobCategory.MISC)
			.properties(properties -> properties.sized(2.0F, 1.0F).setShouldReceiveVelocityUpdates(true).setUpdateInterval(3))
			.defaultLang()
			.renderer(() -> DriftwoodRenderer::new)
			.register();

	public static final RegistryEntry<EntityType<?>, EntityType<LightningArrowEntity>> LIGHTNING_ARROW_ENTITY = REGISTRATE.<LightningArrowEntity>entity("lightning_arrow", LightningArrowEntity::new, MobCategory.MISC)
			.properties(properties -> properties.sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(SharedConstants.TICKS_PER_SECOND))
			.defaultLang()
			.renderer(() -> LightningArrowRenderer::new)
			.register();

	public static final RegistryEntry<EntityType<?>, EntityType<PlatformEntity>> PLATFORM = REGISTRATE.entity("platform", PlatformEntity::new, MobCategory.MISC)
			.properties(properties -> properties.sized(0.0f, 0.0f).updateInterval(3).clientTrackingRange(1).noSave())
			.defaultLang()
			.renderer(() -> PlatformRenderer::new)
			.register();

	public static final GameBehaviorEntry<RisingTidesGameBehavior> RISING_TIDES = REGISTRATE.object("rising_tides")
			.behavior(RisingTidesGameBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SurviveTheTideRulesetBehavior> SURVIVE_THE_TIDE_RULESET = REGISTRATE.object("survive_the_tide_ruleset")
			.behavior(SurviveTheTideRulesetBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SttChatBroadcastBehavior> SURVIVE_THE_TIDE_CHAT_BROADCAST = REGISTRATE.object("survive_the_tide_chat_broadcast")
			.behavior(SttChatBroadcastBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SttWinLogicBehavior> SURVIVE_THE_TIDE_WIN_LOGIC = REGISTRATE.object("survive_the_tide_win_logic")
			.behavior(SttWinLogicBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<RevealPlayersBehavior> REVEAL_PLAYERS = REGISTRATE.object("reveal_players")
			.behavior(RevealPlayersBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<WorldBorderGameBehavior> WORLD_BORDER = REGISTRATE.object("world_border")
			.behavior(WorldBorderGameBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SurviveTheTideWeatherControlBehavior> WEATHER_CONTROL = REGISTRATE.object("stt_weather_control")
			.behavior(SurviveTheTideWeatherControlBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SurviveTheTideWindController> WIND_CONTROL = REGISTRATE.object("stt_wind_control")
			.behavior(SurviveTheTideWindController.CODEC)
			.register();
	public static final GameBehaviorEntry<PhasedWeatherControlBehavior> PHASED_WEATHER_CONTROL = REGISTRATE.object("phased_weather_control")
			.behavior(PhasedWeatherControlBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SttSidebarBehavior> SURVIVE_THE_TIDE_SIDEBAR = REGISTRATE.object("survive_the_tide_sidebar")
			.behavior(SttSidebarBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SttPetsBehavior> PETS = REGISTRATE.object("survive_the_tide_pets")
			.behavior(SttPetsBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<RisingPlatformBehavior> RISING_PLATFORM = REGISTRATE.object("rising_platform")
			.behavior(RisingPlatformBehavior.CODEC)
			.register();

	public static void init() {
	}
}
