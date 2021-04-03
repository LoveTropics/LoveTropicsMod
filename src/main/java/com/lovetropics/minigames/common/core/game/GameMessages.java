package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;

import static com.lovetropics.minigames.client.data.LoveTropicsLangKeys.*;

public final class GameMessages {
	private final IGameDefinition game;

	private GameMessages(IGameDefinition game) {
		this.game = game;
	}

	public static GameMessages forGame(IGameDefinition game) {
		return new GameMessages(game);
	}

	public static GameMessages forGame(IProtoGame game) {
		return new GameMessages(game.getDefinition());
	}

	private static IFormattableTextComponent formatGameName(IFormattableTextComponent name) {
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

	public ITextComponent gameName() {
		return formatGameName(game.getName().deepCopy());
	}

	public ITextComponent startPolling() {
		ITextComponent gameName = gameName();
		Style linkStyle = Style.EMPTY
				.setUnderlined(true)
				.setFormatting(TextFormatting.BLUE)
				.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game join"))
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Join ").appendSibling(gameName)));
		ITextComponent link = new StringTextComponent("/game join").setStyle(linkStyle);

		return formatGameLifecycle(new TranslationTextComponent(COMMAND_MINIGAME_POLLING, gameName, link));
	}

	public ITextComponent stopPolling() {
		return formatGameNegative(new TranslationTextComponent(COMMAND_MINIGAME_STOPPED_POLLING, gameName()));
	}

	public ITextComponent playerJoined(ServerPlayerEntity player, @Nullable PlayerRole role) {
		ITextComponent playerName = player.getDisplayName().deepCopy().mergeStyle(TextFormatting.GOLD);

		String message = role != PlayerRole.SPECTATOR ? "%s has joined the %s game!" : "%s has joined to spectate the %s game!";
		return formatGamePositive(new TranslationTextComponent(message, playerName, gameName()));
	}

	public ITextComponent enoughPlayers() {
		return formatGamePositive(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_ENOUGH_PLAYERS));
	}

	public ITextComponent noLongerEnoughPlayers() {
		return formatGameNegative(new TranslationTextComponent(COMMAND_NO_LONGER_ENOUGH_PLAYERS));
	}

	public ITextComponent finished() {
		return formatGameLifecycle(new TranslationTextComponent(COMMAND_FINISHED_MINIGAME, gameName()));
	}

	public ITextComponent registerSuccess() {
		return formatGamePositive(new TranslationTextComponent(COMMAND_REGISTERED_FOR_MINIGAME, gameName()));
	}

	public ITextComponent unregisterSuccess() {
		return formatGameNegative(new TranslationTextComponent(COMMAND_UNREGISTERED_MINIGAME, gameName()));
	}

	public ITextComponent startSuccess() {
		return formatGamePositive(new TranslationTextComponent(COMMAND_MINIGAME_STARTED));
	}

	public ITextComponent stopSuccess() {
		return formatGamePositive(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_STOPPED_MINIGAME, gameName()));
	}

	public ITextComponent stopPollSuccess() {
		return formatGamePositive(new TranslationTextComponent(COMMAND_STOP_POLL));
	}
}
