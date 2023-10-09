package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

import javax.annotation.Nullable;
import java.util.Map;

// TODO: split up into separate trigger types
public record GeneralEventsTrigger(Map<String, GameActionList> eventActions) implements IGameBehavior {
	public static final Codec<GeneralEventsTrigger> CODEC = Codec.unboundedMap(Codec.STRING, GameActionList.CODEC)
			.xmap(GeneralEventsTrigger::new, b -> b.eventActions);

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		for (GameActionList actions : eventActions.values()) {
			actions.register(game, events);
		}

		this.invoke(game, "ready");

		events.listen(GamePhaseEvents.START, () -> this.invoke(game, "start"));
		events.listen(GamePhaseEvents.TICK, () -> this.invoke(game, "update"));
		events.listen(GamePhaseEvents.STOP, reason -> {
			this.invoke(game, "stop");
			if (reason.isFinished()) {
				this.invoke(game, "finish");
			} else {
				this.invoke(game, "canceled");
			}
		});

		events.listen(GamePlayerEvents.JOIN, player -> this.invoke(game, "player_join", player));
		events.listen(GamePlayerEvents.LEAVE, player -> this.invoke(game, "player_leave", player));
		events.listen(GamePlayerEvents.ADD, player -> this.invoke(game, "player_add", player));
		events.listen(GamePlayerEvents.REMOVE, player -> this.invoke(game, "player_remove", player));

		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> onPlayerSetRole(game, player, role, lastRole));

		events.listen(GamePlayerEvents.TICK, player -> this.invoke(game, "player_update", player));
		events.listen(GamePlayerEvents.RESPAWN, player -> this.invoke(game, "player_respawn", player));

		events.listen(GamePlayerEvents.DAMAGE, (player, damageSource, amount) -> {
			this.invoke(game, "player_hurt", player);
			return InteractionResult.PASS;
		});
		events.listen(GamePlayerEvents.ATTACK, (player, target) -> {
			this.invoke(game, "player_attack", player);
			return InteractionResult.PASS;
		});
		events.listen(GamePlayerEvents.DEATH, (player, damageSource) -> {
			this.invoke(game, "player_death", player);
			return InteractionResult.PASS;
		});

		events.listen(GameLogicEvents.GAME_OVER, () -> this.invoke(game, "game_over"));

		events.listen(GamePhaseEvents.TICK, () -> this.invoke(game, "tick"));
	}

	private void onPlayerSetRole(IGamePhase game, ServerPlayer player, @Nullable PlayerRole role, @Nullable PlayerRole lastRole) {
		if (lastRole == PlayerRole.PARTICIPANT) {
			this.invoke(game, "player_stop_participate", player);
		} else if (lastRole == PlayerRole.SPECTATOR) {
			this.invoke(game, "player_stop_spectate", player);
		}

		if (role == PlayerRole.PARTICIPANT) {
			this.invoke(game, "player_participate", player);
		} else if (role == PlayerRole.SPECTATOR) {
			this.invoke(game, "player_spectate", player);
		}
	}

	private void invoke(IGamePhase game, String event) {
		GameActionList actions = eventActions.get(event);
		if (actions != null) {
			actions.applyPlayer(game, GameActionContext.EMPTY);
		}
	}

	private void invoke(IGamePhase game, String event, ServerPlayer player) {
		GameActionList actions = eventActions.get(event);
		if (actions != null) {
			actions.applyPlayer(game, GameActionContext.EMPTY, player);
		}
	}
}
