package com.lovetropics.minigames.client.lobby;

import com.lovetropics.minigames.Constants;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class LobbyKeybinds {
	public static final KeyMapping JOIN = create("join", InputConstants.KEY_J);
	public static final KeyMapping LEAVE = create("leave", InputConstants.KEY_L);

	public static void init() {
	}

	private static KeyMapping create(String id, int key) {
		return new KeyMapping("key." + Constants.MODID + "." + id, KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM.getOrCreate(key), "key.categories." + Constants.MODID + ".lobby");
	}
}
