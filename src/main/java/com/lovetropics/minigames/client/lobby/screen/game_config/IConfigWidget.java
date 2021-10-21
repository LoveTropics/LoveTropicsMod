package com.lovetropics.minigames.client.lobby.screen.game_config;

import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.IRenderable;

public interface IConfigWidget extends INestedGuiEventHandler, IRenderable {
	
	public int getHeight();

}
