package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.lovetropics.minigames.common.core.chat.ChatChannel;
import com.lovetropics.minigames.common.core.chat.ChatChannelStore;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamConfig;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public record TeamChatBehavior(ResourceKey<ChatType> chatType) implements IGameBehavior {
	public static final MapCodec<TeamChatBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ResourceKey.codec(Registries.CHAT_TYPE).fieldOf("chat_type").forGetter(TeamChatBehavior::chatType)
	).apply(i, TeamChatBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		TeamState teams = game.getInstanceState().getOrThrow(TeamState.KEY);
		events.listen(GamePlayerEvents.ADD, player -> {
			ChatChannelStore.set(player, ChatChannel.TEAM);
			player.sendSystemMessage(GameTexts.Commands.TEAM_CHAT_INTRO);
		});
		events.listen(GamePlayerEvents.CHAT, (player, signedMessage) -> {
			ChatChannel channel = ChatChannelStore.get(player);
			if (channel != ChatChannel.TEAM) {
				return false;
			}
			GameTeamKey teamKey = teams.getTeamForPlayer(player);
			if (teamKey != null) {
				GameTeam team = teams.getTeamByKey(teamKey);
				if (team != null) {
					broadcastToTeam(player, signedMessage, game.getInstanceState().getOrThrow(TeamState.KEY), team);
					return true;
				}
			}
			return false;
		});
	}

	private void broadcastToTeam(ServerPlayer player, PlayerChatMessage signedMessage, TeamState teams, GameTeam team) {
		ChatType.Bound chatType = ChatType.bind(this.chatType, player)
				.withTargetName(team.config().name().copy().withStyle(team.config().formatting()));

		player.server.logChatMessage(signedMessage.decoratedContent(), chatType, null);
		OutgoingChatMessage message = OutgoingChatMessage.create(signedMessage);
		for (ServerPlayer otherPlayer : teams.getPlayersForTeam(team.key())) {
			otherPlayer.sendChatMessage(message, false, chatType);
		}
	}
}
