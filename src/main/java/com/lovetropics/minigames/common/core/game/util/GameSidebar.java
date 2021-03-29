package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.game.PlayerSet;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SDisplayObjectivePacket;
import net.minecraft.network.play.server.SScoreboardObjectivePacket;
import net.minecraft.network.play.server.SUpdateScorePacket;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public final class GameSidebar implements PlayerSet.Listeners, AutoCloseable {
	private static final int SIDEBAR_SLOT = 1;
	private static final int ADD_OBJECTIVE = 0;
	private static final int REMOVE_OBJECTIVE = 1;

	private static final String OBJECTIVE_NAME = "lt_sidebar";

	private static final char[] AVAILABLE_FORMATTING_CODES;

	static {
		CharSet vanillaFormattingCodes = new CharOpenHashSet();
		for (TextFormatting formatting : TextFormatting.values()) {
			vanillaFormattingCodes.add(formatting.toString().charAt(1));
		}

		CharList availableFormattingCodes = new CharArrayList();
		for (char code = 'a'; code <= 'z'; code++) {
			if (!vanillaFormattingCodes.contains(code)) {
				availableFormattingCodes.add(code);
			}
		}

		AVAILABLE_FORMATTING_CODES = availableFormattingCodes.toCharArray();
	}

	private final PlayerSet players;
	private final ITextComponent title;

	private String[] display = new String[0];

	private GameSidebar(ITextComponent title, PlayerSet players) {
		this.players = players;
		this.title = title;
		this.players.addListener(this);
	}

	public static GameSidebar open(ITextComponent title, PlayerSet players) {
		GameSidebar widget = new GameSidebar(title, players);
		for (ServerPlayerEntity player : players) {
			widget.onAddPlayer(player);
		}

		return widget;
	}

	public void set(String[] display) {
		if (Arrays.equals(this.display, display)) {
			return;
		}

		for (int i = 0; i < this.display.length; i++) {
			if (i >= display.length || !this.display[i].equals(display[i])) {
				this.players.sendPacket(new SUpdateScorePacket(
						ServerScoreboard.Action.REMOVE, null,
						makeLine(i, this.display[i]), -1
				));
			}
		}

		this.display = display;

		for (ServerPlayerEntity player : this.players) {
			this.sendDisplay(player, display);
		}
	}

	@Override
	public void onAddPlayer(ServerPlayerEntity player) {
		ScoreObjective objective = this.createDummyObjective();

		player.connection.sendPacket(new SScoreboardObjectivePacket(objective, ADD_OBJECTIVE));
		player.connection.sendPacket(new SDisplayObjectivePacket(SIDEBAR_SLOT, objective));

		this.sendDisplay(player, this.display);
	}

	@Override
	public void onRemovePlayer(UUID id, @Nullable ServerPlayerEntity player) {
		if (player != null) {
			ScoreObjective objective = this.createDummyObjective();
			player.connection.sendPacket(new SScoreboardObjectivePacket(objective, REMOVE_OBJECTIVE));
		}
	}

	private void sendDisplay(ServerPlayerEntity player, String[] display) {
		for (int i = 0; i < display.length; i++) {
			int score = display.length - i;
			player.connection.sendPacket(new SUpdateScorePacket(
					ServerScoreboard.Action.CHANGE, OBJECTIVE_NAME,
					makeLine(i, display[i]), score
			));
		}
	}

	private ScoreObjective createDummyObjective() {
		return new ScoreObjective(
				null, OBJECTIVE_NAME,
				ScoreCriteria.DUMMY,
				this.title,
				ScoreCriteria.RenderType.INTEGER
		);
	}

	@Override
	public void close() {
		this.players.removeListener(this);

		for (ServerPlayerEntity player : this.players) {
			this.onRemovePlayer(player.getUniqueID(), player);
		}
	}

	private static String makeLine(int i, String line) {
		return "\u00a7" + AVAILABLE_FORMATTING_CODES[i] + line;
	}
}
