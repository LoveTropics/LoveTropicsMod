package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

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

	private static IFormattableTextComponent formatName(IFormattableTextComponent name) {
		return name.mergeStyle(TextFormatting.ITALIC, TextFormatting.GREEN);
	}

	private static IFormattableTextComponent formatStatus(IFormattableTextComponent message) {
		return message.mergeStyle(TextFormatting.GOLD);
	}

	private static IFormattableTextComponent formatPositive(IFormattableTextComponent message) {
		return message.mergeStyle(TextFormatting.AQUA);
	}

	private static IFormattableTextComponent formatNegative(IFormattableTextComponent message) {
		return message.mergeStyle(TextFormatting.RED);
	}

	private static IFormattableTextComponent formatLink(IFormattableTextComponent link, String command) {
		Style style = Style.EMPTY
				.setUnderlined(true).setFormatting(TextFormatting.BLUE)
				.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(command)));

		return link.setStyle(style);
	}

	public static IFormattableTextComponent clickHere(String command) {
		return formatLink(new TranslationTextComponent(Keys.CLICK_HERE), command);
	}

	public static IFormattableTextComponent lobbyName(IGameLobby lobby) {
		return formatName(new StringTextComponent(lobby.getMetadata().name()));
	}

	public static IFormattableTextComponent gameName(IGameDefinition game) {
		return formatName(game.getName().deepCopy());
	}

	public static IFormattableTextComponent playerName(ServerPlayerEntity player) {
		return player.getDisplayName().deepCopy().mergeStyle(TextFormatting.GREEN);
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

			static void collectTranslations(BiConsumer<String, String> consumer) {
				consumer.accept(JOINED_LOBBY, "You have joined %s!");
				consumer.accept(LEFT_LOBBY, "You have left %s!");

				consumer.accept(STARTED_GAME, "You have started %s!");
				consumer.accept(STOPPED_GAME, "You have stopped %s!");

				consumer.accept(ALREADY_IN_LOBBY, "You have already joined this lobby!");
				consumer.accept(NO_JOINABLE_LOBBIES, "There are no public lobbies to join!");
				consumer.accept(CANNOT_START_LOBBY, "There is no game to start in this lobby!");
				consumer.accept(NOT_IN_LOBBY, "You are not currently in any lobby!");
				consumer.accept(NOT_IN_GAME, "You are not currently in any game!");
				consumer.accept(GAME_ALREADY_STOPPED, "This game has already been stopped!");
				consumer.accept(NO_MANAGE_PERMISSION, "You do not have permission to manage this lobby!");

				consumer.accept(GAMES_INTERSECT, "The game cannot be started because it intersects with another active game!");

				consumer.accept(LOBBY_SELECTOR_HEADER, "There are multiple lobbies available to join! Select one from this list:");
				consumer.accept(LOBBY_SELECTOR_ENTRY, "- %s (%s players): %s to join");
			}

			static String key(String key) {
				return Constants.MODID + ".command." + key;
			}
		}

		public static IFormattableTextComponent joinedLobby(IGameLobby lobby) {
			return formatPositive(new TranslationTextComponent(Keys.JOINED_LOBBY, lobbyName(lobby)));
		}

		public static IFormattableTextComponent leftLobby(IGameLobby lobby) {
			return formatNegative(new TranslationTextComponent(Keys.LEFT_LOBBY, lobbyName(lobby)));
		}

		public static IFormattableTextComponent startedGame(IGameDefinition game) {
			return formatPositive(new TranslationTextComponent(Keys.STARTED_GAME, gameName(game)));
		}

		public static IFormattableTextComponent stoppedGame(IGameDefinition game) {
			return formatNegative(new TranslationTextComponent(Keys.STOPPED_GAME, gameName(game)));
		}

		public static IFormattableTextComponent alreadyInLobby() {
			return formatNegative(new TranslationTextComponent(Keys.ALREADY_IN_LOBBY));
		}

		public static IFormattableTextComponent noJoinableLobbies() {
			return formatNegative(new TranslationTextComponent(Keys.NO_JOINABLE_LOBBIES));
		}

		public static IFormattableTextComponent cannotStartLobby() {
			return formatNegative(new TranslationTextComponent(Keys.CANNOT_START_LOBBY));
		}

		public static IFormattableTextComponent notInLobby() {
			return formatNegative(new TranslationTextComponent(Keys.NOT_IN_LOBBY));
		}

		public static IFormattableTextComponent notInGame() {
			return formatNegative(new TranslationTextComponent(Keys.NOT_IN_GAME));
		}

		public static IFormattableTextComponent gameAlreadyStopped() {
			return formatNegative(new TranslationTextComponent(Keys.GAME_ALREADY_STOPPED));
		}

		public static IFormattableTextComponent noManagePermission() {
			return formatNegative(new TranslationTextComponent(Keys.NO_MANAGE_PERMISSION));
		}

		public static IFormattableTextComponent gamesIntersect() {
			return formatNegative(new TranslationTextComponent(Keys.GAMES_INTERSECT));
		}

		public static IFormattableTextComponent lobbySelector(Collection<? extends IGameLobby> lobbies, @Nullable PlayerRole role) {
			IFormattableTextComponent selector = new TranslationTextComponent(Keys.LOBBY_SELECTOR_HEADER).appendString("\n")
					.mergeStyle(TextFormatting.GOLD);

			for (IGameLobby lobby : lobbies) {
				ITextComponent lobbyName = lobbyName(lobby);
				int players = lobby.getPlayers().getParticipantCount();
				ITextComponent link = clickHere(lobby.getMetadata().joinCommand(role));

				TranslationTextComponent entry = new TranslationTextComponent(Keys.LOBBY_SELECTOR_ENTRY, lobbyName, players, link);
				selector = selector.appendSibling(entry.mergeStyle(TextFormatting.GRAY)).appendString("\n");
			}

			return selector;
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

		public static IFormattableTextComponent lobbyOpened(IGameLobby lobby) {
			ITextComponent link = clickHere(lobby.getMetadata().joinCommand(null));
			return formatStatus(new TranslationTextComponent(Keys.LOBBY_OPENED, lobbyName(lobby), link));
		}

		public static IFormattableTextComponent playerJoined(IGameLobby lobby, ServerPlayerEntity player, @Nullable PlayerRole role) {
			String message = role != PlayerRole.SPECTATOR ? Keys.PLAYER_JOINED : Keys.SPECTATOR_JOINED;
			return formatPositive(new TranslationTextComponent(message, playerName(player), lobbyName(lobby)));
		}

		public static IFormattableTextComponent lobbyPaused() {
			return formatStatus(new TranslationTextComponent(Keys.LOBBY_PAUSED));
		}

		public static IFormattableTextComponent lobbyStopped() {
			return formatStatus(new TranslationTextComponent(Keys.LOBBY_STOPPED));
		}

		public static IFormattableTextComponent enoughPlayers() {
			return formatPositive(new TranslationTextComponent(Keys.ENOUGH_PLAYERS));
		}

		public static IFormattableTextComponent noLongerEnoughPlayers() {
			return formatNegative(new TranslationTextComponent(Keys.NO_LONGER_ENOUGH_PLAYERS));
		}

		public static IFormattableTextComponent leftGameDimension() {
			return formatNegative(new TranslationTextComponent(Keys.LEFT_GAME_DIMENSION));
		}

		public static IFormattableTextComponent telemetryNotConnected() {
			return formatNegative(new TranslationTextComponent(Keys.TELEMETRY_NOT_CONNECTED));
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

				consumer.accept(GAME_PLAYER_COUNT, "%s players");
				consumer.accept(GAME_PLAYER_RANGE, "%s-%s players");

				consumer.accept(PARTICIPATING, "Participating");
				consumer.accept(SPECTATING, "Spectating");
			}

			static String key(String key) {
				return Constants.MODID + ".ui." + key;
			}
		}

		public static IFormattableTextComponent manageGameLobby() {
			return new TranslationTextComponent(Keys.MANAGE_GAME_LOBBY);
		}

		public static IFormattableTextComponent managingGame(ClientGameDefinition game) {
			ITextComponent name = game.name.deepCopy().mergeStyle(TextFormatting.RESET);
			return new TranslationTextComponent(Keys.MANAGING_GAME, name).mergeStyle(TextFormatting.BOLD);
		}

		public static IFormattableTextComponent lobbyName() {
			return new TranslationTextComponent(Keys.LOBBY_NAME);
		}

		public static IFormattableTextComponent publish() {
			return new TranslationTextComponent(Keys.PUBLISH);
		}

		public static IFormattableTextComponent focusLive() {
			return new TranslationTextComponent(Keys.FOCUS_LIVE);
		}

		public static IFormattableTextComponent gameQueue() {
			return new TranslationTextComponent(Keys.GAME_QUEUE);
		}

		public static IFormattableTextComponent installedGames() {
			return new TranslationTextComponent(Keys.INSTALLED_GAMES);
		}

		public static IFormattableTextComponent gameInactive() {
			return new TranslationTextComponent(Keys.GAME_INACTIVE);
		}

		public static IFormattableTextComponent playerRange(int min, int max) {
			if (min == max) {
				return new TranslationTextComponent(Keys.GAME_PLAYER_COUNT, min);
			} else {
				return new TranslationTextComponent(Keys.GAME_PLAYER_RANGE, min, max);
			}
		}

		public static IFormattableTextComponent roleDescription(PlayerRole role) {
			return new TranslationTextComponent(role == PlayerRole.SPECTATOR ? Keys.SPECTATING : Keys.PARTICIPATING);
		}

		public static IFormattableTextComponent closeLobby() {
			return new TranslationTextComponent(Keys.CLOSE_LOBBY);
		}
	}
}
