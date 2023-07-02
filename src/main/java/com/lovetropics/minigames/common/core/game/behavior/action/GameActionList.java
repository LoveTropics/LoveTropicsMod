package com.lovetropics.minigames.common.core.game.behavior.action;

import com.google.common.collect.Lists;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class GameActionList {
	public static final GameActionList EMPTY = new GameActionList(List.of(), Target.SOURCE);

	public static final MapCodec<GameActionList> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.listOrUnit(IGameBehavior.CODEC).fieldOf("actions").forGetter(list -> list.behaviors),
			Target.CODEC.optionalFieldOf("target", Target.SOURCE).forGetter(list -> list.target)
	).apply(i, GameActionList::new));

	private static final Codec<GameActionList> SIMPLE_CODEC = MoreCodecs.listOrUnit(IGameBehavior.CODEC)
			.flatComapMap(
					behaviors -> new GameActionList(behaviors, Target.SOURCE),
					list -> {
						if (list.target != Target.SOURCE) {
							return DataResult.error(() -> "Cannot encode simple action list with target: " + list.target.getSerializedName());
						}
						return DataResult.success(list.behaviors);
					}
			);

	public static final Codec<GameActionList> CODEC = Codec.either(SIMPLE_CODEC, MAP_CODEC.codec())
			.xmap(either -> either.map(Function.identity(), Function.identity()), Either::right);

	private final List<IGameBehavior> behaviors;
	private final Target target;

	private final GameEventListeners listeners = new GameEventListeners();

	public GameActionList(List<IGameBehavior> behaviors, Target target) {
		this.behaviors = behaviors;
		this.target = target;
	}

	public void register(IGamePhase game, EventRegistrar events) {
		for (IGameBehavior behavior : behaviors) {
			behavior.register(game, events.redirect(GameActionEvents::matches, listeners));
		}
	}

	public boolean apply(IGamePhase game, GameActionContext context, ServerPlayer... sources) {
		return apply(game, context, Arrays.asList(sources));
	}

	public boolean apply(IGamePhase game, GameActionContext context, Iterable<ServerPlayer> sources) {
		boolean result = listeners.invoker(GameActionEvents.APPLY).apply(context, sources);
		for (ServerPlayer target : target.resolve(game, sources)) {
			result |= listeners.invoker(GameActionEvents.APPLY_TO_PLAYER).apply(context, target);
		}
		return result;
	}

	public enum Target implements StringRepresentable {
		NONE("none"),
		SOURCE("source"),
		PARTICIPANTS("participants"),
		SPECTATORS("spectators"),
		ALL("all"),
		;

		public static final Codec<Target> CODEC = MoreCodecs.stringVariants(values(), Target::getSerializedName);

		private final String name;

		Target(String name) {
			this.name = name;
		}

		public List<ServerPlayer> resolve(IGamePhase game, Iterable<ServerPlayer> sources) {
			// Copy the lists because we might otherwise get concurrent modification from whatever the actions do!
			return switch (this) {
				case NONE -> List.of();
				case SOURCE -> Lists.newArrayList(sources);
				case PARTICIPANTS -> Lists.newArrayList(game.getParticipants());
				case SPECTATORS -> Lists.newArrayList(game.getSpectators());
				case ALL -> Lists.newArrayList(game.getAllPlayers());
			};
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}
}
