package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.Map;

import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.client.screen.list.AbstractLTList;
import com.lovetropics.minigames.client.screen.list.LTListEntry;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;

public class BehaviorConfigList extends AbstractLTList<BehaviorConfigList.Entry> {

	private final ClientConfigList configList;

	public BehaviorConfigList(Screen screen, Layout layout, ClientConfigList configs) {
		super(screen, layout, 0);
		this.configList = configs;
	}

	@Override
	public void updateEntries() {
		Map<String, ConfigData> configs = configList.configs;

		this.clearEntries();

		for (Map.Entry<String, ConfigData> e : configs.entrySet()) {
			Entry listEntry = new Entry(this, this.screen, e.getKey(), e.getValue());
			this.addEntry(listEntry);
		}
	}

	public class Entry extends LTListEntry<Entry> {
		
		private final String name;
		private final ConfigData data;

		public Entry(BehaviorConfigList list, Screen screen, String name, ConfigData data) {
			super(list, screen);
			this.name = name;
			this.data = data;
		}

		@Override
		public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
			// TODO Auto-generated method stub
			
		}
	}
}
