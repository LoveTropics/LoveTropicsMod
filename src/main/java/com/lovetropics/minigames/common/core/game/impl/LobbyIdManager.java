package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.lobby.GameLobbyId;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Locale;
import java.util.Set;

public final class LobbyIdManager {
	private final Set<String> acquired = new ObjectOpenHashSet<>();

	public GameLobbyId acquire(String name) {
		String commandId = this.generateCommandId(name);
		return GameLobbyId.create(name, commandId);
	}

	public void release(GameLobbyId id) {
		this.acquired.remove(id.getCommandId());
	}

	private String generateCommandId(String name) {
		String sanitizedName = sanitizeName(name);

		String uniqueId = sanitizedName;
		while (this.acquired.contains(uniqueId)) {
			String salt = RandomStringUtils.randomAlphabetic(3).toLowerCase(Locale.ROOT);
			uniqueId = sanitizedName + "_" + salt;
		}

		return uniqueId;
	}

	private static String sanitizeName(String name) {
		return name.toLowerCase(Locale.ROOT).replace(' ', '_')
				.replaceAll("\\P{InBasic_Latin}", "");
	}
}
