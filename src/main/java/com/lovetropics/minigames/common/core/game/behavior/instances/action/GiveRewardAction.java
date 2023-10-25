package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.rewards.GameRewardsMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record GiveRewardAction(List<ItemStack> items) implements IGameBehavior {
	public static final MapCodec<GiveRewardAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.ITEM_STACK.listOf().fieldOf("items").forGetter(GiveRewardAction::items)
	).apply(i, GiveRewardAction::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) throws GameException {
		GameRewardsMap rewards = game.getState().getOrThrow(GameRewardsMap.STATE);
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
			for (final ItemStack item : items) {
				rewards.give(target, item);
			}
			return true;
		});
	}
}
