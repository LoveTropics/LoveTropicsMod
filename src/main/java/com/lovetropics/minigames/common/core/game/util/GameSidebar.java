package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.MutablePlayerSet;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public final class GameSidebar implements AutoCloseable {
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

	private final MutablePlayerSet players;
	private final ITextComponent title;

	private String[] display = new String[0];

	private GameSidebar(MinecraftServer server, ITextComponent title) {
		this.players = new MutablePlayerSet(server);
		this.title = title;
	}

	public static GameSidebar open(IGameInstance game, ITextComponent title) {
		GameSidebar widget = new GameSidebar(game.getServer(), title);

		for (ServerPlayerEntity player : game.getPlayers()) {
			widget.addPlayer(player);
		}

		GameEventListeners events = game.events();
		events.listen(GamePlayerEvents.JOIN, (g, player, role) -> widget.addPlayer(player));
		events.listen(GamePlayerEvents.LEAVE, (g, player) -> widget.removePlayer(player));
		events.listen(GameLifecycleEvents.FINISH, g -> widget.close());

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

	private void addPlayer(ServerPlayerEntity player) {
		this.players.add(player);

		ScoreObjective objective = this.createDummyObjective();

		player.connection.sendPacket(new SScoreboardObjectivePacket(objective, ADD_OBJECTIVE));
		player.connection.sendPacket(new SDisplayObjectivePacket(SIDEBAR_SLOT, objective));

		this.sendDisplay(player, this.display);
	}

	private void removePlayer(ServerPlayerEntity player) {
		this.players.remove(player);
		this.sendRemove(player);
	}

	private void sendRemove(ServerPlayerEntity player) {
		ScoreObjective objective = this.createDummyObjective();
		player.connection.sendPacket(new SScoreboardObjectivePacket(objective, REMOVE_OBJECTIVE));
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
		for (ServerPlayerEntity player : this.players) {
			this.sendRemove(player);
		}
		this.players.clear();
	}

	private static String makeLine(int i, String line) {
		return "\u00a7" + AVAILABLE_FORMATTING_CODES[i] + line;
	}
}
