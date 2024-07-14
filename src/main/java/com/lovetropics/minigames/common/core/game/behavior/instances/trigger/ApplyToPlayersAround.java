package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.function.Supplier;

// TODO: Implement as a decorated PlayerActionTarget with parameters
public record ApplyToPlayersAround(Optional<EntityPredicate> predicate, Optional<EntityPredicate> notPredicate, GameActionList<ServerPlayer> action, float distance, boolean includeSource) implements IGameBehavior {
	public static final MapCodec<ApplyToPlayersAround> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			EntityPredicate.CODEC.optionalFieldOf("predicate").forGetter(ApplyToPlayersAround::predicate),
			EntityPredicate.CODEC.optionalFieldOf("not_predicate").forGetter(ApplyToPlayersAround::notPredicate),
			GameActionList.PLAYER_CODEC.fieldOf("action").forGetter(ApplyToPlayersAround::action),
			Codec.FLOAT.fieldOf("distance").forGetter(ApplyToPlayersAround::distance),
			Codec.BOOL.optionalFieldOf("include_source", false).forGetter(ApplyToPlayersAround::includeSource)
	).apply(i, ApplyToPlayersAround::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		action.register(game, events);

		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			boolean applied = false;
			for (final ServerPlayer otherPlayer : game.participants()) {
				if (player == otherPlayer && !includeSource) {
					continue;
				}
				if (predicate.isPresent() && !predicate.get().matches(otherPlayer, otherPlayer)) {
					continue;
				}
				if (notPredicate.isPresent() && notPredicate.get().matches(otherPlayer, otherPlayer)) {
					continue;
				}
				if (player.closerThan(otherPlayer, distance)) {
					applied |= action.apply(game, context, otherPlayer);
				}
			}
			return applied;
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.APPLY_TO_PLAYERS_AROUND;
	}
}
