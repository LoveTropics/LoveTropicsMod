package com.lovetropics.minigames.common.content.biodiversity_blitz;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.BbAssignPlotsBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.BbBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.BbClientStateBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.BbCurrencyBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.BbCurrencyWinTrigger;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.BbGivePlantsBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.BbMerchantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.BbWaveSpawnerBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.EqualizeCurrencyBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.RemoveFromBlockBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.SpawnSurpriseWaveBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.AgingCropPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.BarrierPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.BerriesPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.EffectAddingPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.FlamingPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.FruitDropEntityBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.GrowCoconutsBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.GrowPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.IdleDropItemPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.LightningPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.PianguasPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.PlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.PlantBiomeCheckBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.PlantHealthBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.PlantItemBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.ProximityBombPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.ScareTrapPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.WateryPlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.drops.DropLootTableBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.drops.DropPlantItemBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.placement.PlaceDoublePlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.placement.PlaceFeaturePlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.placement.PlaceSinglePlantBehavior;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.tutorial.BbTutorialAction;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.CheckeredPlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbGlobalState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbSelfState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.CurrencyTargetState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.item.UniqueBlockNamedItem;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.GameClientTweakEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

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

	public static final GameBehaviorEntry<PlantBiomeCheckBehavior> PLANT_BIOME_CHECK = REGISTRATE.object("plant_biome_check")
			.behavior(PlantBiomeCheckBehavior.CODEC)
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

	public static final GameBehaviorEntry<LightningPlantBehavior> LIGHTNING_PLANT = REGISTRATE.object("lightning_plant")
			.behavior(LightningPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<IdleDropItemPlantBehavior> IDLE_DROP_ITEM = REGISTRATE.object("idle_drop_item")
			.behavior(IdleDropItemPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<PianguasPlantBehavior> PIANGUAS = REGISTRATE.object("pianguas")
			.behavior(PianguasPlantBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<FruitDropEntityBehavior> FRUIT_DROP_ENTITY = REGISTRATE.object("fruit_drop_entity")
			.behavior(FruitDropEntityBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<GrowCoconutsBehavior> GROW_COCONUTS = REGISTRATE.object("grow_coconuts")
			.behavior(GrowCoconutsBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<BbCurrencyBehavior> CURRENCY = REGISTRATE.object("biodiversity_blitz_currency")
			.behavior(BbCurrencyBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<BbClientStateBehavior> SEND_CLIENT_STATE = REGISTRATE.object("biodiversity_blitz_client_state")
			.behavior(BbClientStateBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<RemoveFromBlockBehavior> REMOVE_FROM_BLOCK = REGISTRATE.object("remove_from_block")
            .behavior(RemoveFromBlockBehavior.CODEC)
            .register();

	public static final GameBehaviorEntry<BbCurrencyWinTrigger> CURRENCY_WIN_TRIGGER = REGISTRATE.object("biodiversity_blitz_currency_win_trigger")
			.behavior(BbCurrencyWinTrigger.CODEC)
			.register();

	public static final GameBehaviorEntry<BbGivePlantsBehavior> GIVE_PLANTS = REGISTRATE.object("biodiversity_blitz_give_plants")
			.behavior(BbGivePlantsBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<SpawnSurpriseWaveBehavior> SPAWN_SURPRISE_WAVE = REGISTRATE.object("spawn_surprise_wave")
			.behavior(SpawnSurpriseWaveBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<EqualizeCurrencyBehavior> EQUALIZE_CURRENCY = REGISTRATE.object("equalize_currency")
			.behavior(EqualizeCurrencyBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<BbTutorialAction> BB_TUTORIAL = REGISTRATE.object("bb_tutorial")
			.behavior(BbTutorialAction.CODEC)
			.register();

	// Client State

	public static final GameClientTweakEntry<CheckeredPlotsState> CHECKERED_PLOTS_STATE = REGISTRATE.object("checkered_plots")
			.clientState(CheckeredPlotsState.CODEC)
			.register();

	public static final GameClientTweakEntry<CurrencyTargetState> CURRENCY_TARGET = REGISTRATE.object("currency_target")
			.clientState(CurrencyTargetState.CODEC)
			.register();

	public static final GameClientTweakEntry<ClientBbSelfState> SELF_STATE = REGISTRATE.object("biodiversity_blitz_self_state")
			.clientState(ClientBbSelfState.CODEC)
			.register();

	public static final GameClientTweakEntry<ClientBbGlobalState> GLOBAL_STATE = REGISTRATE.object("biodiversity_blitz_global_state")
			.clientState(ClientBbGlobalState.CODEC)
			.register();

	// Items

	public static final ItemEntry<UniqueBlockNamedItem> CARROT_SEEDS = REGISTRATE.item("carrot_seeds", p -> new UniqueBlockNamedItem(Blocks.CARROTS, p))
			.register();

	public static final ItemEntry<UniqueBlockNamedItem> POTATO_SEEDS = REGISTRATE.item("potato_seeds", p -> new UniqueBlockNamedItem(Blocks.POTATOES, p))
			.register();

	public static final ItemEntry<UniqueBlockNamedItem> SWEET_BERRY_SEEDS = REGISTRATE.item("sweet_berry_seeds", p -> new UniqueBlockNamedItem(Blocks.SWEET_BERRY_BUSH, p))
			.register();

	public static final ItemEntry<Item> OSA_POINT = REGISTRATE.item("osa_point", Item::new)
			.lang("Biodiversity Point")
			.register();

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {

	}


	private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(true);

	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		List<Component> list = event.getToolTip();

		// Used to pop the advanced tooltip off the stack and add it back at the end
		List<Component> removedComponents = null;

		CompoundTag rootTag = stack.getTag();
		if (rootTag != null && rootTag.contains("display", 10)) {
			CompoundTag display = rootTag.getCompound("display");

			if (display.getTagType("ShiftLore") == 9) {
				if (event.getFlags().isAdvanced()) {
					removedComponents = new ArrayList<>();
					removedComponents.add(list.remove(list.size() - 1));
					removedComponents.add(list.remove(list.size() - 1));
				}

				// Supposed to be used to create a buffer space, but it doesn't seem to work?
//				list.add(Component.literal(""));

				if (Screen.hasShiftDown()) {
					ListTag listtag = display.getList("ShiftLore", 8);

					for (int i = 0; i < listtag.size(); ++i) {
						String s = listtag.getString(i);

						try {
							MutableComponent mutablecomponent1 = Component.Serializer.fromJson(s);
							if (mutablecomponent1 != null) {
								list.add(ComponentUtils.mergeStyles(mutablecomponent1, LORE_STYLE));
							}
						} catch (Exception exception) {
							display.remove("ShiftLore");
						}
					}
				} else {
					list.add(Component.literal("Press Shift for more information").withStyle(ChatFormatting.GOLD));
				}
			}
		}

		if (removedComponents != null) {
			list.addAll(removedComponents);
		}
	}
}
