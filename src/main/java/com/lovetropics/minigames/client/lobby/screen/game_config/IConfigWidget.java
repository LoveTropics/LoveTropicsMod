package com.lovetropics.minigames.client.lobby.screen.game_config;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.Widget;

public interface IConfigWidget extends ContainerEventHandler, Widget {
	
	int getHeight();

}
