package com.lovetropics.minigames.common.content.qottott;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.qottott.behavior.GivePointsAction;
import com.lovetropics.minigames.common.content.qottott.behavior.ItemDropperBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.ItemPickupPriorityBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.KillBonusBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.KitSelectionBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.LobbyWithPortalBehavior;
import com.lovetropics.minigames.common.util.Util;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
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
	public static final GameBehaviorEntry<KillBonusBehavior> KILL_BONUS = REGISTRATE.object("kill_bonus").behavior(KillBonusBehavior.CODEC).register();
	public static final GameBehaviorEntry<ItemPickupPriorityBehavior> PICKUP_PRIORITY_BEHAVIOR = REGISTRATE.object("pickup_priority").behavior(ItemPickupPriorityBehavior.CODEC).register();

	private static final Component POINT_MULTIPLIER_NAME = REGISTRATE.addLang("attribute", new ResourceLocation(Constants.MODID, "point_multiplier"), "Point Multiplier");
	public static final RegistryEntry<Attribute> POINT_MULTIPLIER = REGISTRATE.simple("point_multiplier", Registries.ATTRIBUTE, () ->
			new RangedAttribute(Util.unpackTranslationKey(POINT_MULTIPLIER_NAME), 1.0, 0.0, 100.0).setSyncable(false)
	);
	private static final Component PICKUP_PRIORITY_NAME = REGISTRATE.addLang("attribute", new ResourceLocation(Constants.MODID, "pickup_priority"), "Item Pickup Priority");
	public static final RegistryEntry<Attribute> PICKUP_PRIORITY = REGISTRATE.simple("pickup_priority", Registries.ATTRIBUTE, () ->
			new RangedAttribute(Util.unpackTranslationKey(PICKUP_PRIORITY_NAME), 0.0, -100.0, 100.0).setSyncable(false)
	);

	public static void init() {
	}

	@SubscribeEvent
	public static void onModifyDefaultAttributes(final EntityAttributeModificationEvent event) {
		event.add(EntityType.PLAYER, POINT_MULTIPLIER.get());
		event.add(EntityType.PLAYER, PICKUP_PRIORITY.get());
	}
}
