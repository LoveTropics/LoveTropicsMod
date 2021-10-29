package com.lovetropics.minigames.common.content.biodiversity_blitz;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.*;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.*;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.drops.DropLootTableBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.drops.DropPlantItemBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.placement.PlaceDoublePlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.placement.PlaceFeaturePlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.placement.PlaceSinglePlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_tweak.CheckeredPlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_tweak.ClientBiodiversityBlitzState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.item.UniqueBlockNamedItem;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.GameClientTweakEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class BiodiversityBlitz {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	// Behaviors

	public static final GameBehaviorEntry<BbBehavior> BIODIVERSITY_BLITZ = REGISTRATE.object("biodiversity_blitz")
			.behavior(BbBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<BbAssignPlotsBehavior> ASSIGN_PLOTS = REGISTRATE.object("biodiversity_blitz_assign_plots")
			.behavior(BbAssignPlotsBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<BbMerchantBehavior> MERCHANT = REGISTRATE.object("biodiversity_blitz_merchant")
			.behavior(BbMerchantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<BbWaveSpawnerBehavior> WAVE_SPAWNER = REGISTRATE.object("biodiversity_blitz_wave_spawner")
			.behavior(BbWaveSpawnerBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<PlantBehavior> PLANT = REGISTRATE.object("biodiversity_blitz_plant")
			.behavior(PlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<PlantItemBehavior> PLANT_ITEM = REGISTRATE.object("biodiversity_blitz_plant_item")
			.behavior(PlantItemBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<PlaceSinglePlantBehavior> PLACE_SINGLE_PLANT = REGISTRATE.object("place_single_plant")
			.behavior(PlaceSinglePlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<PlaceDoublePlantBehavior> PLACE_DOUBLE_PLANT = REGISTRATE.object("place_double_plant")
			.behavior(PlaceDoublePlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<PlaceFeaturePlantBehavior> PLACE_FEATURE_PLANT = REGISTRATE.object("place_feature_plant")
			.behavior(PlaceFeaturePlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<DropPlantItemBehavior> DROP_PLANT_ITEM = REGISTRATE.object("drop_plant_item")
			.behavior(DropPlantItemBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<DropLootTableBehavior> DROP_LOOT_TABLE = REGISTRATE.object("drop_loot_table")
			.behavior(DropLootTableBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<AgingCropPlantBehavior> AGING_PLANT = REGISTRATE.object("aging_plant")
			.behavior(AgingCropPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<BarrierPlantBehavior> BARRIER_PLANT = REGISTRATE.object("barrier_plant")
			.behavior(BarrierPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<BerriesPlantBehavior> BERRIES_PLANT = REGISTRATE.object("berries_plant")
			.behavior(BerriesPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<EffectAddingPlantBehavior> EFFECT_ADDING_PLANT = REGISTRATE.object("effect_adding_plant")
			.behavior(EffectAddingPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<GrowPlantBehavior> GROW_PLANT = REGISTRATE.object("grow_plant")
			.behavior(GrowPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<PlantHealthBehavior> PLANT_HEALTH = REGISTRATE.object("plant_health")
			.behavior(PlantHealthBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<ProximityBombPlantBehavior> PROXIMITY_BOMB_PLANT = REGISTRATE.object("proximity_bomb_plant")
			.behavior(ProximityBombPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<ScareTrapPlantBehavior> SCARE_TRAP_PLANT = REGISTRATE.object("scare_trap_plant")
			.behavior(ScareTrapPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<FlamingPlantBehavior> FLAMING_PLANT = REGISTRATE.object("flaming_plant")
			.behavior(FlamingPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<WateryPlantBehavior> WATERY_PLANT = REGISTRATE.object("watery_plant")
			.behavior(WateryPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<IdleDropItemPlantBehavior> IDLE_DROP_ITEM = REGISTRATE.object("idle_drop_item")
			.behavior(IdleDropItemPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<PianguasPlantBehavior> PIANGUAS = REGISTRATE.object("pianguas")
			.behavior(PianguasPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<BbDropCurrencyBehavior> DROP_CURRENCY = REGISTRATE.object("drop_currency")
			.behavior(BbDropCurrencyBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<BbClientStateBehavior> SEND_CLIENT_STATE = REGISTRATE.object("biodiversity_blitz_client_state")
			.behavior(BbClientStateBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<RemoveFromBlockBehavior> REMOVE_FROM_BLOCK = REGISTRATE.object("remove_from_block")
            .behavior(RemoveFromBlockBehavior.CODEC)
            .register();

	// Tweaks

	public static final GameClientTweakEntry<CheckeredPlotsState> CHECKERED_PLOTS_STATE = REGISTRATE.object("checkered_plots")
			.clientState(CheckeredPlotsState.CODEC)
			.register();

	public static final GameClientTweakEntry<ClientBiodiversityBlitzState> CLIENT_STATE = REGISTRATE.object("biodiversity_blitz_client_state")
			.clientState(ClientBiodiversityBlitzState.CODEC)
			.register();

	// Items

	public static final ItemEntry<UniqueBlockNamedItem> CARROT_SEEDS = REGISTRATE.item("carrot_seeds", p -> new UniqueBlockNamedItem(Blocks.CARROTS, p))
			.register();

	public static final ItemEntry<UniqueBlockNamedItem> POTATO_SEEDS = REGISTRATE.item("potato_seeds", p -> new UniqueBlockNamedItem(Blocks.POTATOES, p))
			.register();

	public static final ItemEntry<UniqueBlockNamedItem> SWEET_BERRY_SEEDS = REGISTRATE.item("sweet_berry_seeds", p -> new UniqueBlockNamedItem(Blocks.SWEET_BERRY_BUSH, p))
			.register();

	public static final ItemEntry<Item> OSA_POINT = REGISTRATE.item("osa_point", Item::new)
			.register();

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {

	}
}
