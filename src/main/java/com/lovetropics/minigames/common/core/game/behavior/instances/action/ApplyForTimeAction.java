package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.function.Supplier;

public record ApplyForTimeAction(GameActionList<ServerPlayer> apply, GameActionList<ServerPlayer> clear, int seconds) implements IGameBehavior {
	public static final MapCodec<ApplyForTimeAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			GameActionList.PLAYER_CODEC.fieldOf("apply").forGetter(ApplyForTimeAction::apply),
			GameActionList.PLAYER_CODEC.fieldOf("clear").forGetter(ApplyForTimeAction::clear),
			Codec.INT.fieldOf("seconds").forGetter(ApplyForTimeAction::seconds)
	).apply(i, ApplyForTimeAction::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		apply.register(game, events);
		clear.register(game, events);

		final State state = new State();
		events.listen(GameActionEvents.APPLY, context -> state.tryApply(game, context));
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> state.tryApplyTo(game, context, player));
		events.listen(GamePhaseEvents.TICK, () -> state.tick(game));
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.APPLY_FOR_TIME;
	}

	private class State {
		private static final long NOT_ACTIVE = -1;

		private long finishTime = NOT_ACTIVE;
		private final Object2LongMap<UUID> playerFinishTimes = new Object2LongOpenHashMap<>();

		private State() {
			playerFinishTimes.defaultReturnValue(NOT_ACTIVE);
		}

		private void tick(final IGamePhase game) {
			final long time = game.ticks();
			if (finishTime != NOT_ACTIVE && time >= finishTime) {
				clear.apply(game, GameActionContext.EMPTY);
				finishTime = NOT_ACTIVE;
			}
			playerFinishTimes.object2LongEntrySet().removeIf(entry -> {
				if (time >= entry.getLongValue()) {
					final ServerPlayer player = game.getAllPlayers().getPlayerBy(entry.getKey());
					if (player != null) {
						clear.apply(game, GameActionContext.EMPTY, player);
					}
					return true;
				}
				return false;
			});
		}

		private boolean tryApply(final IGamePhase game, final GameActionContext context) {
			if (finishTime == NOT_ACTIVE && apply.apply(game, context)) {
				finishTime = game.ticks() + (long) seconds * SharedConstants.TICKS_PER_SECOND;
				return true;
			}
			return false;
		}

		public boolean tryApplyTo(final IGamePhase game, final GameActionContext context, final ServerPlayer player) {
			if (!playerFinishTimes.containsKey(player.getUUID()) && apply.apply(game, context, player)) {
				playerFinishTimes.put(player.getUUID(), game.ticks() + (long) seconds * SharedConstants.TICKS_PER_SECOND);
				return true;
			}
			return false;
		}
	}
}
