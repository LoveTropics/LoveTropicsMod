package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;

import java.util.List;
import java.util.Map;

public final class CommandEventsBehavior extends CommandInvokeBehavior {
	public static final Codec<CommandEventsBehavior> CODEC = COMMANDS_CODEC.xmap(CommandEventsBehavior::new, c -> c.commands);

	public CommandEventsBehavior(Map<String, List<String>> commands) {
		super(commands);
	}

	@Override
	protected void registerEvents(GameEventListeners events) {
		this.invoke("ready");

		events.listen(GameLifecycleEvents.START, game -> this.invoke("start"));
		events.listen(GameLifecycleEvents.TICK, game -> this.invoke("update"));
		events.listen(GameLifecycleEvents.FINISH, game -> this.invoke("finish"));
		events.listen(GameLifecycleEvents.POST_FINISH, game -> this.invoke("post_finish"));
		events.listen(GameLifecycleEvents.CANCEL, game -> this.invoke("cancel"));

		events.listen(GamePlayerEvents.JOIN, this::onPlayerJoin);
		events.listen(GamePlayerEvents.LEAVE, (game, player) -> this.invoke("player_leave", player));
		events.listen(GamePlayerEvents.TICK, (game, player) -> this.invoke("player_update", player));
		events.listen(GamePlayerEvents.RESPAWN, (game, player) -> this.invoke("player_respawn", player));

		events.listen(GamePlayerEvents.DAMAGE, (game, player, damageSource, amount) -> {
			this.invoke("player_hurt", player);
			return ActionResultType.PASS;
		});
		events.listen(GamePlayerEvents.ATTACK, (game, player, target) -> {
			this.invoke("player_attack", player);
			return ActionResultType.PASS;
		});
		events.listen(GamePlayerEvents.DEATH, (game, player, damageSource) -> {
			this.invoke("player_death", player);
			return ActionResultType.PASS;
		});

		events.listen(GameLivingEntityEvents.TICK, (game, entity) -> this.invoke("entity_update", entity));
	}

	private void onPlayerJoin(IGameInstance game, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			this.invoke("player_join", player);
		} else {
			this.invoke("player_spectate", player);
		}
	}
}
