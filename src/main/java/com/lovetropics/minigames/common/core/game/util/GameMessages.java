package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;

import static com.lovetropics.minigames.client.data.LoveTropicsLangKeys.*;

// TODO: per-game messages too?
public final class GameMessages {
	private final IGameLobby lobby;

	private GameMessages(IGameLobby lobby) {
		this.lobby = lobby;
	}

	public static GameMessages forLobby(IGameLobby lobby) {
		return new GameMessages(lobby);
	}

	private static IFormattableTextComponent formatLobbyName(IFormattableTextComponent name) {
		return name.mergeStyle(TextFormatting.ITALIC, TextFormatting.GREEN);
	}

	private static IFormattableTextComponent formatGameLifecycle(IFormattableTextComponent message) {
		return message.mergeStyle(TextFormatting.GOLD);
	}

	private static IFormattableTextComponent formatGamePositive(IFormattableTextComponent message) {
		return message.mergeStyle(TextFormatting.AQUA);
	}

	private static IFormattableTextComponent formatGameNegative(IFormattableTextComponent message) {
		return message.mergeStyle(TextFormatting.RED);
	}

	public ITextComponent lobbyName() {
		return formatLobbyName(new StringTextComponent(lobby.getMetadata().name()));
	}

	// TODO: rename: open registrations?
	public ITextComponent startPolling() {
		// TODO: update message to not have a command written but rather be a 'click here'
		ITextComponent lobbyName = lobbyName();
		Style linkStyle = Style.EMPTY
				.setUnderlined(true)
				.setFormatting(TextFormatting.BLUE)
				.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game join " + lobby.getMetadata().commandId()))
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Join ").appendSibling(lobbyName)));
		ITextComponent link = new StringTextComponent("/game join").setStyle(linkStyle);

		return formatGameLifecycle(new TranslationTextComponent(COMMAND_MINIGAME_POLLING, lobbyName, link));
	}

	public ITextComponent stopPolling() {
		return formatGameNegative(new TranslationTextComponent(COMMAND_MINIGAME_STOPPED_POLLING, lobbyName()));
	}

	public ITextComponent playerJoined(ServerPlayerEntity player, @Nullable PlayerRole role) {
		ITextComponent playerName = player.getDisplayName().deepCopy().mergeStyle(TextFormatting.GOLD);

		String message = role != PlayerRole.SPECTATOR ? "%s has joined the %s game!" : "%s has joined to spectate the %s game!";
		return formatGamePositive(new TranslationTextComponent(message, playerName, lobbyName()));
	}

	public ITextComponent enoughPlayers() {
		return formatGamePositive(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_ENOUGH_PLAYERS));
	}

	public ITextComponent noLongerEnoughPlayers() {
		return formatGameNegative(new TranslationTextComponent(COMMAND_NO_LONGER_ENOUGH_PLAYERS));
	}

	public ITextComponent finished() {
		return formatGameLifecycle(new TranslationTextComponent(COMMAND_FINISHED_MINIGAME, lobbyName()));
	}

	public ITextComponent registerSuccess() {
		return formatGamePositive(new TranslationTextComponent(COMMAND_REGISTERED_FOR_MINIGAME, lobbyName()));
	}

	public ITextComponent unregisterSuccess() {
		return formatGameNegative(new TranslationTextComponent(COMMAND_UNREGISTERED_MINIGAME, lobbyName()));
	}

	public ITextComponent startSuccess() {
		return formatGamePositive(new TranslationTextComponent(COMMAND_MINIGAME_STARTED));
	}

	public ITextComponent stopSuccess() {
		return formatGamePositive(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_STOPPED_MINIGAME, lobbyName()));
	}

	public ITextComponent stopPollSuccess() {
		return formatGamePositive(new TranslationTextComponent(COMMAND_STOP_POLL));
	}
}
