package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.google.common.primitives.Booleans;
import com.lovetropics.minigames.client.toast.NotificationDisplay;
import com.lovetropics.minigames.client.toast.NotificationIcon;
import com.lovetropics.minigames.client.toast.ShowNotificationToastMessage;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.SpectatingClientState;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.role.StreamHosts;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class SpectatorChaseBehavior implements IGameBehavior {
	public static final Codec<SpectatorChaseBehavior> CODEC = Codec.unit(SpectatorChaseBehavior::new);

	private static final Component SPECTATING_NOTIFICATION_MESSAGE = new TextComponent("You are a ")
			.append(new TextComponent("spectator").withStyle(ChatFormatting.BOLD))
			.append("!\n")
			.append("Scroll or use the arrow keys to select players.\n")
			.append("Hold ").append(new TextComponent("Left Control").withStyle(ChatFormatting.UNDERLINE)).append(" and scroll to zoom.");

	private static final NotificationDisplay SPECTATING_NOTIFICATION_STYLE = new NotificationDisplay(
			NotificationIcon.item(new ItemStack(Items.ENDER_EYE)),
			NotificationDisplay.Sentiment.NEUTRAL,
			NotificationDisplay.Color.LIGHT,
			10 * 1000
	);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (role == PlayerRole.SPECTATOR) {
				LoveTropicsNetwork.CHANNEL.send(
						PacketDistributor.PLAYER.with(() -> player),
						new ShowNotificationToastMessage(SPECTATING_NOTIFICATION_MESSAGE, SPECTATING_NOTIFICATION_STYLE)
				);
			}
			sendSpectatingUpdate(game);
		});
		events.listen(GamePlayerEvents.REMOVE, player -> removePlayer(game, player));
		events.listen(GamePhaseEvents.DESTROY, () -> stop(game));
	}

	private void removePlayer(IGamePhase game, ServerPlayer player) {
		GameClientState.removeFromPlayer(GameClientStateTypes.SPECTATING.get(), player);

		this.sendSpectatingUpdate(game);
	}

	private void sendSpectatingUpdate(IGamePhase game) {
		SpectatingClientState spectating = this.buildSpectatingState(game);
		GameClientState.sendToPlayers(spectating, game.getSpectators());
	}

	private void stop(IGamePhase game) {
		GameClientState.removeFromPlayers(GameClientStateTypes.SPECTATING.get(), game.getSpectators());
	}

	private SpectatingClientState buildSpectatingState(IGamePhase game) {
		PlayerSet participants = game.getParticipants();

        Comparator<ServerPlayer> comparator = Comparator
				.comparing(ServerPlayer::getScoreboardName, String::compareToIgnoreCase)
				.thenComparing((ServerPlayer player) -> {
					Team team = player.getTeam();
					return team != null ? team.getName() : "";
				})
				.thenComparing(StreamHosts::isHost, Booleans.trueFirst());

		List<UUID> ids = participants.stream()
				.sorted(comparator)
				.map(ServerPlayer::getUUID)
				.toList();
		return new SpectatingClientState(ids);
	}
}
