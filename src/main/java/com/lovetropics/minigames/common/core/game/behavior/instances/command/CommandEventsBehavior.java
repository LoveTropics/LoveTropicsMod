package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public final class CommandEventsBehavior extends CommandInvokeMapBehavior {
	public static final Codec<CommandEventsBehavior> CODEC = COMMANDS_CODEC.xmap(CommandEventsBehavior::new, c -> c.commands);

	public CommandEventsBehavior(Map<String, List<String>> commands) {
		super(commands);
	}

	// TODO: this can handle passing additional data to datapacks in the future too
	@Override
	protected void registerEvents(IGamePhase game, EventRegistrar events) {
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

		events.listen(GameLivingEntityEvents.TICK, entity -> this.invoke("entity_update", entity));

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
}
