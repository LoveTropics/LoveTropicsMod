package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemPickupPointsBehavior(HolderSet<Item> items) implements IGameBehavior {
	public static final MapCodec<ItemPickupPointsBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(ItemPickupPointsBehavior::items)
	).apply(i, ItemPickupPointsBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		events.listen(GamePlayerEvents.PICK_UP_ITEM, (player, item) -> {
			final ItemStack stack = item.getItem();
			if (items.contains(stack.getItemHolder())) {
				final int count = stack.getCount();
				game.getStatistics().forPlayer(player).incrementInt(StatisticKey.POINTS, count);
				stack.shrink(count);
			}
		});
	}
}
