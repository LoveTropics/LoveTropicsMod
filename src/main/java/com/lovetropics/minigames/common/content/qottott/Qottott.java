package com.lovetropics.minigames.common.content.qottott;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.qottott.behavior.CoinDropAttributeBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.GivePointsAction;
import com.lovetropics.minigames.common.content.qottott.behavior.ItemDropperBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.ItemPickupPriorityBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.KitSelectionBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.LeakyPocketsAction;
import com.lovetropics.minigames.common.content.qottott.behavior.LobbyWithPortalBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.PowerUpIndicatorBehavior;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Qottott {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final GameBehaviorEntry<ItemDropperBehavior> ITEM_DROPPER = REGISTRATE.object("item_dropper").behavior(ItemDropperBehavior.CODEC).register();
	public static final GameBehaviorEntry<GivePointsAction> GIVE_POINTS = REGISTRATE.object("give_points").behavior(GivePointsAction.CODEC).register();
	public static final GameBehaviorEntry<LobbyWithPortalBehavior> LOBBY_WITH_PORTAL = REGISTRATE.object("lobby_with_portal").behavior(LobbyWithPortalBehavior.CODEC).register();
	public static final GameBehaviorEntry<KitSelectionBehavior> KIT_SELECTION = REGISTRATE.object("kit_selection").behavior(KitSelectionBehavior.CODEC).register();
	public static final GameBehaviorEntry<ItemPickupPriorityBehavior> PICKUP_PRIORITY_BEHAVIOR = REGISTRATE.object("pickup_priority").behavior(ItemPickupPriorityBehavior.CODEC).register();
	public static final GameBehaviorEntry<CoinDropAttributeBehavior> COIN_DROP_ATTRIBUTE_BEHAVIOR = REGISTRATE.object("coin_drop_attribute").behavior(CoinDropAttributeBehavior.CODEC).register();
	public static final GameBehaviorEntry<PowerUpIndicatorBehavior> POWER_UP_INDICATOR = REGISTRATE.object("power_up_indicator").behavior(PowerUpIndicatorBehavior.CODEC).register();
	public static final GameBehaviorEntry<LeakyPocketsAction> LEAKY_POCKETS = REGISTRATE.object("leaky_pockets").behavior(LeakyPocketsAction.CODEC).register();

	public static final RegistryEntry<Attribute> COIN_MULTIPLIER = REGISTRATE.object("coin_multiplier").attribute(translationKey ->
			new RangedAttribute(translationKey, 1.0, 0.0, 100.0).setSyncable(false)
	).lang("Coin Multiplier").register();
	public static final RegistryEntry<Attribute> PICKUP_PRIORITY = REGISTRATE.object("pickup_priority").attribute(translationKey ->
			new RangedAttribute(translationKey, 0.0, -100.0, 100.0).setSyncable(false)
	).lang("Item Pickup Priority").register();
	public static final RegistryEntry<Attribute> COIN_DROPS = REGISTRATE.object("coin_drops").attribute(translationKey ->
			new RangedAttribute(translationKey, 0.0, 0.0, 1.0).setSyncable(false)
	).lang("Coin Drops").register();

	public static final RegistryEntry<MobEffect> COIN_MULTIPLIER_POWER_UP = REGISTRATE.object("coin_multiplier_power_up").mobEffect(() -> new CustomMobEffect(MobEffectCategory.BENEFICIAL).addAttributeModifier(
			COIN_MULTIPLIER.get(),
			"515fbc2f-9a69-4f4b-bf94-c152a2c16bce",
			1.0,
			AttributeModifier.Operation.MULTIPLY_BASE
	)).lang("Coin Multiplier Power-up").register();
	public static final RegistryEntry<MobEffect> PICKUP_PRIORITY_POWER_UP = REGISTRATE.object("pickup_priority_power_up").mobEffect(() -> new CustomMobEffect(MobEffectCategory.BENEFICIAL).addAttributeModifier(
			PICKUP_PRIORITY.get(),
			"5f0e6e3d-c0f5-4f48-9152-0bf5ee26e93e",
			1.0,
			AttributeModifier.Operation.ADDITION
	)).lang("Pickup Priority Power-up").register();
	public static final RegistryEntry<MobEffect> KNOCKBACK_RESISTANCE_POWER_UP = REGISTRATE.object("knockback_resistance_power_up").mobEffect(() -> new CustomMobEffect(MobEffectCategory.BENEFICIAL).addAttributeModifier(
			Attributes.KNOCKBACK_RESISTANCE,
			"12babcd7-d94e-41d2-b791-c20ae6ef4eaa",
			1.0,
			AttributeModifier.Operation.ADDITION
	)).lang("Knockback Resistance Power-Up").register();
	public static final RegistryEntry<MobEffect> SPEED_POWER_UP = REGISTRATE.object("speed_power_up").mobEffect(() -> new CustomMobEffect(MobEffectCategory.BENEFICIAL).addAttributeModifier(
			Attributes.MOVEMENT_SPEED,
			"c88be5fa-eae9-4c1f-bcf4-79f6c44bfb44",
			0.05,
			AttributeModifier.Operation.ADDITION
	)).lang("Speed Power-up").register();

	public static void init() {
	}

	@SubscribeEvent
	public static void onModifyDefaultAttributes(final EntityAttributeModificationEvent event) {
		event.add(EntityType.PLAYER, COIN_MULTIPLIER.get());
		event.add(EntityType.PLAYER, PICKUP_PRIORITY.get());
		event.add(EntityType.PLAYER, COIN_DROPS.get());
	}

	private static class CustomMobEffect extends MobEffect {
		protected CustomMobEffect(final MobEffectCategory category) {
			super(category, 0xffffff);
		}
	}
}
