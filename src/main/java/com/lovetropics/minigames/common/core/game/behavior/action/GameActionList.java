package com.lovetropics.minigames.common.core.game.behavior.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class GameActionList {
	public static final GameActionList EMPTY = new GameActionList(List.of());

	public static final MapCodec<GameActionList> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.listOrUnit(IGameBehavior.CODEC).fieldOf("actions").forGetter(list -> list.behaviors)
	).apply(i, GameActionList::new));

	private static final Codec<GameActionList> SIMPLE_CODEC = MoreCodecs.listOrUnit(IGameBehavior.CODEC).xmap(GameActionList::new, list -> list.behaviors);

	public static final Codec<GameActionList> CODEC = ExtraCodecs.xor(MAP_CODEC.codec(), SIMPLE_CODEC)
			.xmap(either -> either.map(Function.identity(), Function.identity()), Either::right);

	private final List<IGameBehavior> behaviors;

	private final GameEventListeners listeners = new GameEventListeners();

	public GameActionList(List<IGameBehavior> behaviors) {
		this.behaviors = behaviors;
	}

	public void register(IGamePhase game, EventRegistrar events) {
		for (IGameBehavior behavior : behaviors) {
			behavior.register(game, events.redirect(GameActionEvents::matches, listeners));
		}
	}

	public boolean apply(GameActionContext context, ServerPlayer... targets) {
		return apply(context, Arrays.asList(targets));
	}

	public boolean apply(GameActionContext context, Iterable<ServerPlayer> targets) {
		boolean result = listeners.invoker(GameActionEvents.APPLY).apply(context, targets);
		for (ServerPlayer target : targets) {
			result |= listeners.invoker(GameActionEvents.APPLY_TO_PLAYER).apply(context, target);
		}
		return result;
	}
}
