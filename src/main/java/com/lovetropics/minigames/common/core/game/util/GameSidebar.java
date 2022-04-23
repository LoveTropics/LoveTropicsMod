package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.Objective;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.Arrays;

public final class GameSidebar implements GameWidget {
	private static final int SIDEBAR_SLOT = 1;
	private static final int ADD_OBJECTIVE = 0;
	private static final int REMOVE_OBJECTIVE = 1;

	private static final String OBJECTIVE_NAME = "lt_sidebar";

	private static final int MAX_WIDTH = 40;

	private static final String FORMATTING_CHARS = "abcdefghijklmnopqrstuvwxyz.!?*+-(){}|";
	private static final char[] AVAILABLE_FORMATTING_CODES;

	static {
		CharSet vanillaFormattingCodes = new CharOpenHashSet();
		for (ChatFormatting formatting : ChatFormatting.values()) {
			vanillaFormattingCodes.add(formatting.toString().charAt(1));
		}

		CharList availableFormattingCodes = new CharArrayList();
		for (int i = 0; i < FORMATTING_CHARS.length(); i++) {
			char code = FORMATTING_CHARS.charAt(i);
			if (!vanillaFormattingCodes.contains(code)) {
				availableFormattingCodes.add(code);
			}
		}

		AVAILABLE_FORMATTING_CODES = availableFormattingCodes.toCharArray();
	}

	private final MutablePlayerSet players;
	private final Component title;

	private String[] display = new String[0];

	public GameSidebar(MinecraftServer server, Component title) {
		this.players = new MutablePlayerSet(server);
		this.title = title;
	}

	public void set(String[] display) {
		if (Arrays.equals(this.display, display)) {
			return;
		}

		for (int i = 0; i < this.display.length; i++) {
			if (i >= display.length || !this.display[i].equals(display[i])) {
				this.players.sendPacket(new ClientboundSetScorePacket(
						ServerScoreboard.Method.REMOVE, null,
						makeLine(i, this.display[i]), -1
				));
			}
		}

		this.display = display;

		for (ServerPlayer player : this.players) {
			this.sendDisplay(player, display);
		}
	}

	@Override
	public void addPlayer(ServerPlayer player) {
		this.players.add(player);

		Objective objective = this.createDummyObjective();

		player.connection.send(new ClientboundSetObjectivePacket(objective, ADD_OBJECTIVE));
		player.connection.send(new ClientboundSetDisplayObjectivePacket(SIDEBAR_SLOT, objective));

		this.sendDisplay(player, this.display);
	}

	@Override
	public void removePlayer(ServerPlayer player) {
		this.players.remove(player);
		this.sendRemove(player);
	}

	private void sendRemove(ServerPlayer player) {
		Objective objective = this.createDummyObjective();
		player.connection.send(new ClientboundSetObjectivePacket(objective, REMOVE_OBJECTIVE));
	}

	private void sendDisplay(ServerPlayer player, String[] display) {
		for (int i = 0; i < display.length; i++) {
			int score = display.length - i;
			player.connection.send(new ClientboundSetScorePacket(
					ServerScoreboard.Method.CHANGE, OBJECTIVE_NAME,
					makeLine(i, display[i]), score
			));
		}
	}

	private Objective createDummyObjective() {
		return new Objective(
				null, OBJECTIVE_NAME,
				ObjectiveCriteria.DUMMY,
				this.title,
				ObjectiveCriteria.RenderType.INTEGER
		);
	}

	@Override
	public void close() {
		for (ServerPlayer player : this.players) {
			this.sendRemove(player);
		}
		this.players.clear();
	}

	private static String makeLine(int i, String line) {
		line = "\u00a7" + AVAILABLE_FORMATTING_CODES[i] + line;
		if (line.length() > MAX_WIDTH) {
			line = line.substring(0, MAX_WIDTH - 1);
		}
		return line;
	}
}
