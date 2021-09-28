package com.lovetropics.minigames.common.core.game.impl;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Locale;
import java.util.Set;

public final class LobbyCommandIdManager {
	private final Set<String> acquired = new ObjectOpenHashSet<>();

	public String acquire(String name) {
		String sanitizedName = sanitizeName(name);

		String commandId = sanitizedName;
		while (this.acquired.contains(commandId)) {
			String salt = RandomStringUtils.randomNumeric(3);
			commandId = sanitizedName + "_" + salt;
		}

		this.acquired.add(commandId);

		return commandId;
	}

	public void release(String id) {
		this.acquired.remove(id);
	}

	private static String sanitizeName(String name) {
		return name.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_')
				.replaceAll("[^A-Za-z0-9_]", "");
	}
}
