package com.lovetropics.minigames.common.content.mangroves_and_pianguas;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpAssignPlotsBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpMerchantBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpWaveSpawnerBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant.*;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant.drops.DropLootTableBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant.drops.DropPlantItemBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant.placement.PlaceDoublePlantBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant.placement.PlaceFeaturePlantBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant.placement.PlaceSinglePlantBehavior;
import com.lovetropics.minigames.common.util.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.LoveTropicsRegistrate;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class MangrovesAndPianguas {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final GameBehaviorEntry<MpBehavior> MANGROVES_AND_PIANGUAS = REGISTRATE.object("mangroves_and_pianguas")
			.behavior(MpBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<MpAssignPlotsBehavior> ASSIGN_PLOTS = REGISTRATE.object("mangroves_and_pianguas_assign_plots")
			.behavior(MpAssignPlotsBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<MpMerchantBehavior> MERCHANT = REGISTRATE.object("mangroves_and_pianguas_merchant")
			.behavior(MpMerchantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<MpWaveSpawnerBehavior> WAVE_SPAWNER = REGISTRATE.object("mangroves_and_pianguas_wave_spawner")
			.behavior(MpWaveSpawnerBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<MpPlantBehavior> PLANT = REGISTRATE.object("mangroves_and_pianguas_plant")
			.behavior(MpPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<MpPlantItemBehavior> PLANT_ITEM = REGISTRATE.object("mangroves_and_pianguas_plant_item")
			.behavior(MpPlantItemBehavior.CODEC)
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

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {

	}
}
