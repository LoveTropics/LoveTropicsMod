package com.lovetropics.minigames.client.lobby.select_role;

import net.minecraft.client.Minecraft;

public final class ClientRoleSelection {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void openScreen() {
		CLIENT.displayGuiScreen(new SelectPlayerRoleScreen());
	}
}
