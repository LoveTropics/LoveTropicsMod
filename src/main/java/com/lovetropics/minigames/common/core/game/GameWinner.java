package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public sealed interface GameWinner {
	static GameWinner byPlayerKey(IGamePhase game, PlayerKey key) {
		ServerPlayer player = game.allPlayers().getPlayerBy(key);
		return player != null ? new Player(player) : new OfflinePlayer(key.id(), Component.literal(key.name()));
	}

	Component name();

	record Nobody() implements GameWinner {
		@Override
		public Component name() {
			return MinigameTexts.NOBODY_WON;
		}
	}

	record Player(ServerPlayer player) implements GameWinner {
		@Override
		public Component name() {
			return player.getDisplayName();
		}
	}

	record OfflinePlayer(UUID playerId, Component name) implements GameWinner {
	}

	record Team(GameTeam team) implements GameWinner {
		@Override
		public Component name() {
			return team.config().styledName();
		}
	}
}
