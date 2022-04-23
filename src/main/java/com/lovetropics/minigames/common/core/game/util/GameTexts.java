package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.BiConsumer;

public final class GameTexts {
	public static void collectTranslations(BiConsumer<String, String> consumer) {
		Keys.collectTranslations(consumer);
		Commands.Keys.collectTranslations(consumer);
		Status.Keys.collectTranslations(consumer);
		Ui.Keys.collectTranslations(consumer);
	}

	static final class Keys {
		static final String CLICK_HERE = key("click_here");

		static void collectTranslations(BiConsumer<String, String> consumer) {
			consumer.accept(CLICK_HERE, "Click here");
		}

		static String key(String key) {
			return Constants.MODID + "." + key;
		}
	}

	private static MutableComponent formatName(MutableComponent name) {
		return name.withStyle(ChatFormatting.ITALIC, ChatFormatting.GREEN);
	}

	private static MutableComponent formatStatus(MutableComponent message) {
		return message.withStyle(ChatFormatting.GOLD);
	}

	private static MutableComponent formatPositive(MutableComponent message) {
		return message.withStyle(ChatFormatting.AQUA);
	}

	private static MutableComponent formatNegative(MutableComponent message) {
		return message.withStyle(ChatFormatting.RED);
	}

	private static MutableComponent formatLink(MutableComponent link, String command) {
		Style style = Style.EMPTY
				.setUnderlined(true).withColor(ChatFormatting.BLUE)
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(command)));

		return link.setStyle(style);
	}

	public static MutableComponent clickHere(String command) {
		return formatLink(new TranslatableComponent(Keys.CLICK_HERE), command);
	}

	public static MutableComponent lobbyName(IGameLobby lobby) {
		return formatName(new TextComponent(lobby.getMetadata().name()));
	}

	public static MutableComponent gameName(IGameDefinition game) {
		return formatName(game.getName().copy());
	}

	public static MutableComponent playerName(ServerPlayer player) {
		return player.getDisplayName().copy().withStyle(ChatFormatting.GREEN);
	}

	public static final class Commands {
		static final class Keys {
			static final String JOINED_LOBBY = key("joined_lobby");
			static final String LEFT_LOBBY = key("left_lobby");

			static final String STARTED_GAME = key("started_game");
			static final String STOPPED_GAME = key("stopped_game");

			static final String ALREADY_IN_LOBBY = key("already_in_lobby");
			static final String NO_JOINABLE_LOBBIES = key("no_joinable_lobbies");
			static final String CANNOT_START_LOBBY = key("cannot_start");
			static final String NOT_IN_LOBBY = key("not_in_lobby");
			static final String NOT_IN_GAME = key("not_in_game");
			static final String GAME_ALREADY_STOPPED = key("game_already_stopped");
			static final String NO_MANAGE_PERMISSION = key("no_manage_permission");

			static final String GAMES_INTERSECT = key("games_intersect");

			static final String LOBBY_SELECTOR_HEADER = key("lobby_selector_header");
			static final String LOBBY_SELECTOR_ENTRY = key("lobby_selector_entry");

			static final String CANNOT_TELEPORT_INTO_GAME = key("cannot_teleport_into_game");

			static void collectTranslations(BiConsumer<String, String> consumer) {
				consumer.accept(JOINED_LOBBY, "You have joined %s!");
				consumer.accept(LEFT_LOBBY, "You have left %s!");

				consumer.accept(STARTED_GAME, "You have started %s!");
				consumer.accept(STOPPED_GAME, "You have stopped %s!");

				consumer.accept(ALREADY_IN_LOBBY, "You have already joined a lobby!");
				consumer.accept(NO_JOINABLE_LOBBIES, "There are no public lobbies to join!");
				consumer.accept(CANNOT_START_LOBBY, "There is no game to start in this lobby!");
				consumer.accept(NOT_IN_LOBBY, "You are not currently in any lobby!");
				consumer.accept(NOT_IN_GAME, "You are not currently in any game!");
				consumer.accept(GAME_ALREADY_STOPPED, "This game has already been stopped!");
				consumer.accept(NO_MANAGE_PERMISSION, "You do not have permission to manage this lobby!");

				consumer.accept(GAMES_INTERSECT, "The game cannot be started because it intersects with another active game!");

				consumer.accept(LOBBY_SELECTOR_HEADER, "There are multiple lobbies available to join! Select one from this list:");
				consumer.accept(LOBBY_SELECTOR_ENTRY, "- %s (%s players): %s to join");

				consumer.accept(CANNOT_TELEPORT_INTO_GAME, "You cannot teleport into a game without being apart of it!");
			}

			static String key(String key) {
				return Constants.MODID + ".command." + key;
			}
		}

		public static MutableComponent joinedLobby(IGameLobby lobby) {
			return formatPositive(new TranslatableComponent(Keys.JOINED_LOBBY, lobbyName(lobby)));
		}

		public static MutableComponent leftLobby(IGameLobby lobby) {
			return formatNegative(new TranslatableComponent(Keys.LEFT_LOBBY, lobbyName(lobby)));
		}

		public static MutableComponent startedGame(IGameDefinition game) {
			return formatPositive(new TranslatableComponent(Keys.STARTED_GAME, gameName(game)));
		}

		public static MutableComponent stoppedGame(IGameDefinition game) {
			return formatNegative(new TranslatableComponent(Keys.STOPPED_GAME, gameName(game)));
		}

		public static MutableComponent alreadyInLobby() {
			return formatNegative(new TranslatableComponent(Keys.ALREADY_IN_LOBBY));
		}

		public static MutableComponent noJoinableLobbies() {
			return formatNegative(new TranslatableComponent(Keys.NO_JOINABLE_LOBBIES));
		}

		public static MutableComponent cannotStartLobby() {
			return formatNegative(new TranslatableComponent(Keys.CANNOT_START_LOBBY));
		}

		public static MutableComponent notInLobby() {
			return formatNegative(new TranslatableComponent(Keys.NOT_IN_LOBBY));
		}

		public static MutableComponent notInGame() {
			return formatNegative(new TranslatableComponent(Keys.NOT_IN_GAME));
		}

		public static MutableComponent gameAlreadyStopped() {
			return formatNegative(new TranslatableComponent(Keys.GAME_ALREADY_STOPPED));
		}

		public static MutableComponent noManagePermission() {
			return formatNegative(new TranslatableComponent(Keys.NO_MANAGE_PERMISSION));
		}

		public static MutableComponent gamesIntersect() {
			return formatNegative(new TranslatableComponent(Keys.GAMES_INTERSECT));
		}

		public static MutableComponent lobbySelector(Collection<? extends IGameLobby> lobbies, @Nullable PlayerRole role) {
			MutableComponent selector = new TranslatableComponent(Keys.LOBBY_SELECTOR_HEADER).append("\n")
					.withStyle(ChatFormatting.GOLD);

			for (IGameLobby lobby : lobbies) {
				Component lobbyName = lobbyName(lobby);
				int players = lobby.getPlayers().size();
				Component link = clickHere(lobby.getMetadata().joinCommand(role));

				TranslatableComponent entry = new TranslatableComponent(Keys.LOBBY_SELECTOR_ENTRY, lobbyName, players, link);
				selector = selector.append(entry.withStyle(ChatFormatting.GRAY)).append("\n");
			}

			return selector;
		}

		public static MutableComponent cannotTeleportIntoGame() {
			return formatNegative(new TranslatableComponent(Keys.CANNOT_TELEPORT_INTO_GAME));
		}
	}

	public static final class Status {
		static final class Keys {
			static final String LOBBY_OPENED = key("lobby_opened");

			static final String PLAYER_JOINED = key("player_joined");
			static final String SPECTATOR_JOINED = key("spectator_joined");

			static final String ENOUGH_PLAYERS = key("enough_players");
			static final String NO_LONGER_ENOUGH_PLAYERS = key("no_longer_enough_players");

			static final String LEFT_GAME_DIMENSION = key("left_game_dimension");

			static final String LOBBY_PAUSED = key("lobby_paused");
			static final String LOBBY_STOPPED = key("lobby_stopped");

			static final String TELEMETRY_NOT_CONNECTED = key("telemetry_warning");

			static void collectTranslations(BiConsumer<String, String> consumer) {
				consumer.accept(LOBBY_OPENED, "%s has opened for registration! %s to get a chance to play!");

				consumer.accept(PLAYER_JOINED, "%s has joined %s!");
				consumer.accept(SPECTATOR_JOINED, "%s has joined %s as a spectator!");

				consumer.accept(ENOUGH_PLAYERS, "There are now enough players to start the game!");
				consumer.accept(NO_LONGER_ENOUGH_PLAYERS, "There are no longer enough players to start game!");

				consumer.accept(LEFT_GAME_DIMENSION, "You left the game dimension and have been removed from the lobby!");

				consumer.accept(LOBBY_PAUSED, "Your current lobby has paused! You have been teleported to your last location.");
				consumer.accept(LOBBY_STOPPED, "Your current lobby has stopped! You have been teleported to your last location.");

				consumer.accept(TELEMETRY_NOT_CONNECTED, "Telemetry socket is not connected!");
			}

			static String key(String key) {
				return Constants.MODID + ".status." + key;
			}
		}

		public static MutableComponent lobbyOpened(IGameLobby lobby) {
			Component link = clickHere(lobby.getMetadata().joinCommand(null));
			return formatStatus(new TranslatableComponent(Keys.LOBBY_OPENED, lobbyName(lobby), link));
		}

		public static MutableComponent playerJoined(IGameLobby lobby, ServerPlayer player, @Nullable PlayerRole role) {
			String message = role != PlayerRole.SPECTATOR ? Keys.PLAYER_JOINED : Keys.SPECTATOR_JOINED;
			return formatPositive(new TranslatableComponent(message, playerName(player), lobbyName(lobby)));
		}

		public static MutableComponent lobbyPaused() {
			return formatStatus(new TranslatableComponent(Keys.LOBBY_PAUSED));
		}

		public static MutableComponent lobbyStopped() {
			return formatStatus(new TranslatableComponent(Keys.LOBBY_STOPPED));
		}

		public static MutableComponent enoughPlayers() {
			return formatPositive(new TranslatableComponent(Keys.ENOUGH_PLAYERS));
		}

		public static MutableComponent noLongerEnoughPlayers() {
			return formatNegative(new TranslatableComponent(Keys.NO_LONGER_ENOUGH_PLAYERS));
		}

		public static MutableComponent leftGameDimension() {
			return formatNegative(new TranslatableComponent(Keys.LEFT_GAME_DIMENSION));
		}

		public static MutableComponent telemetryNotConnected() {
			return formatNegative(new TranslatableComponent(Keys.TELEMETRY_NOT_CONNECTED));
		}
	}

	public static final class Ui {
		static final class Keys {
			static final String MANAGE_GAME_LOBBY = key("manage_game_lobby");
			static final String MANAGING_GAME = key("managing_game");
			static final String LOBBY_NAME = key("lobby_name");
			static final String PUBLISH = key("publish");
			static final String FOCUS_LIVE = key("focus_live");
			static final String GAME_QUEUE = key("game_queue");
			static final String INSTALLED_GAMES = key("installed_games");
			static final String GAME_INACTIVE = key("game_inactive");
			static final String CLOSE_LOBBY = key("close_lobby");

			static final String SELECT_PLAYER_ROLE = key("select_player_role");

			static final String GAME_PLAYER_COUNT = key("game_player_count");
			static final String GAME_PLAYER_RANGE = key("game_player_bounds");

			static final String PARTICIPATING = key("participating");
			static final String SPECTATING = key("spectating");

			static void collectTranslations(BiConsumer<String, String> consumer) {
				consumer.accept(MANAGE_GAME_LOBBY, "Manage Game Lobby");
				consumer.accept(MANAGING_GAME, "Managing Game: %s");
				consumer.accept(LOBBY_NAME, "Lobby Name");
				consumer.accept(PUBLISH, "Publish");
				consumer.accept(FOCUS_LIVE, "Focus Live");
				consumer.accept(GAME_QUEUE, "Game Queue");
				consumer.accept(INSTALLED_GAMES, "Installed");
				consumer.accept(GAME_INACTIVE, "Inactive");
				consumer.accept(CLOSE_LOBBY, "Close");

				consumer.accept(SELECT_PLAYER_ROLE, "Select Player Role");

				consumer.accept(GAME_PLAYER_COUNT, "%s players");
				consumer.accept(GAME_PLAYER_RANGE, "%s-%s players");

				consumer.accept(PARTICIPATING, "Participating");
				consumer.accept(SPECTATING, "Spectating");
			}

			static String key(String key) {
				return Constants.MODID + ".ui." + key;
			}
		}

		public static MutableComponent manageGameLobby() {
			return new TranslatableComponent(Keys.MANAGE_GAME_LOBBY);
		}

		public static MutableComponent managingGame(ClientGameDefinition game) {
			Component name = game.name.copy().withStyle(ChatFormatting.RESET);
			return new TranslatableComponent(Keys.MANAGING_GAME, name).withStyle(ChatFormatting.BOLD);
		}

		public static MutableComponent lobbyName() {
			return new TranslatableComponent(Keys.LOBBY_NAME);
		}

		public static MutableComponent publish() {
			return new TranslatableComponent(Keys.PUBLISH);
		}

		public static MutableComponent focusLive() {
			return new TranslatableComponent(Keys.FOCUS_LIVE);
		}

		public static MutableComponent gameQueue() {
			return new TranslatableComponent(Keys.GAME_QUEUE);
		}

		public static MutableComponent installedGames() {
			return new TranslatableComponent(Keys.INSTALLED_GAMES);
		}

		public static MutableComponent gameInactive() {
			return new TranslatableComponent(Keys.GAME_INACTIVE);
		}

		public static MutableComponent playerRange(int min, int max) {
			if (min == max) {
				return new TranslatableComponent(Keys.GAME_PLAYER_COUNT, min);
			} else {
				return new TranslatableComponent(Keys.GAME_PLAYER_RANGE, min, max);
			}
		}

		public static MutableComponent roleDescription(PlayerRole role) {
			return new TranslatableComponent(role == PlayerRole.SPECTATOR ? Keys.SPECTATING : Keys.PARTICIPATING);
		}

		public static MutableComponent closeLobby() {
			return new TranslatableComponent(Keys.CLOSE_LOBBY);
		}

		public static MutableComponent selectPlayerRole() {
			return new TranslatableComponent(Keys.SELECT_PLAYER_ROLE);
		}
	}
}
