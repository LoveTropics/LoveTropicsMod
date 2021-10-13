package com.lovetropics.minigames.common.content.mangroves_and_pianguas;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpAssignPlotsBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpMerchantBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.MpWaveSpawnerBehavior;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant.*;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantPlacement;
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

	public static final GameBehaviorEntry<AgingPlantBehavior> AGING_PLANT = REGISTRATE.object("aging_plant")
			.behavior(AgingPlantBehavior.CODEC)
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

	public static final GameBehaviorEntry<ScaryPlantBehavior> SCARY_PLANT = REGISTRATE.object("scary_plant")
			.behavior(ScaryPlantBehavior.CODEC)
			.register();

	static {
		PlantPlacement.register();
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {

	}
}
