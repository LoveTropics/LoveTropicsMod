package com.lovetropics.minigames.client.lobby;

import com.lovetropics.minigames.Constants;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class LobbyKeybinds {
	public static final KeyMapping JOIN = create("join", InputConstants.KEY_J, KeyModifier.CONTROL);
	public static final KeyMapping LEAVE = create("leave", InputConstants.KEY_L, KeyModifier.CONTROL);
	public static final KeyMapping MANAGE = create("manage", InputConstants.KEY_G, KeyModifier.CONTROL);

	public static void init() {
	}

	private static KeyMapping create(String id, int key, KeyModifier modifier) {
		return new KeyMapping("key." + Constants.MODID + "." + id, KeyConflictContext.IN_GAME, modifier, InputConstants.Type.KEYSYM.getOrCreate(key), "key.categories." + Constants.MODID + ".lobby");
	}
}
