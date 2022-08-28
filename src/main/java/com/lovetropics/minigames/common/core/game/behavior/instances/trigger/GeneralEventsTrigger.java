package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

import javax.annotation.Nullable;
import java.util.Map;

// TODO: split up into separate trigger types
public record GeneralEventsTrigger(Map<String, GameActionList> eventActions) implements IGameBehavior {
	public static final Codec<GeneralEventsTrigger> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(Codec.STRING, GameActionList.CODEC).fieldOf("actions").forGetter(GeneralEventsTrigger::eventActions)
	).apply(i, GeneralEventsTrigger::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		for (GameActionList actions : eventActions.values()) {
			actions.register(game, events);
		}

		this.invoke("ready");

		events.listen(GamePhaseEvents.START, () -> this.invoke("start"));
		events.listen(GamePhaseEvents.TICK, () -> this.invoke("update"));
		events.listen(GamePhaseEvents.STOP, reason -> {
			this.invoke("stop");
			if (reason.isFinished()) {
				this.invoke("finish");
			} else {
				this.invoke("canceled");
			}
		});

		events.listen(GamePlayerEvents.JOIN, player -> this.invoke("player_join", player));
		events.listen(GamePlayerEvents.LEAVE, player -> this.invoke("player_leave", player));
		events.listen(GamePlayerEvents.ADD, player -> this.invoke("player_add", player));
		events.listen(GamePlayerEvents.REMOVE, player -> this.invoke("player_remove", player));

		events.listen(GamePlayerEvents.SET_ROLE, this::onPlayerSetRole);

		events.listen(GamePlayerEvents.TICK, player -> this.invoke("player_update", player));
		events.listen(GamePlayerEvents.RESPAWN, player -> this.invoke("player_respawn", player));

		events.listen(GamePlayerEvents.DAMAGE, (player, damageSource, amount) -> {
			this.invoke("player_hurt", player);
			return InteractionResult.PASS;
		});
		events.listen(GamePlayerEvents.ATTACK, (player, target) -> {
			this.invoke("player_attack", player);
			return InteractionResult.PASS;
		});
		events.listen(GamePlayerEvents.DEATH, (player, damageSource) -> {
			this.invoke("player_death", player);
			return InteractionResult.PASS;
		});

		events.listen(GameLogicEvents.GAME_OVER, () -> this.invoke("game_over"));
		events.listen(GameLogicEvents.PHASE_CHANGE, (phase, lastPhase) -> {
			this.invoke("phase_finish/" + lastPhase.key());
			this.invoke("phase_start/" + phase.key());
		});

		events.listen(GamePhaseEvents.TICK, () -> this.invoke("tick"));
	}

	private void onPlayerSetRole(ServerPlayer player, @Nullable PlayerRole role, @Nullable PlayerRole lastRole) {
		if (lastRole == PlayerRole.PARTICIPANT) {
			this.invoke("player_stop_participate", player);
		} else if (lastRole == PlayerRole.SPECTATOR) {
			this.invoke("player_stop_spectate", player);
		}

		if (role == PlayerRole.PARTICIPANT) {
			this.invoke("player_participate", player);
		} else if (role == PlayerRole.SPECTATOR) {
			this.invoke("player_spectate", player);
		}
	}

	private void invoke(String event) {
		GameActionList actions = eventActions.get(event);
		if (actions != null) {
			actions.apply(GameActionContext.EMPTY);
		}
	}

	private void invoke(String event, ServerPlayer player) {
		GameActionList actions = eventActions.get(event);
		if (actions != null) {
			actions.apply(GameActionContext.EMPTY, player);
		}
	}
}
