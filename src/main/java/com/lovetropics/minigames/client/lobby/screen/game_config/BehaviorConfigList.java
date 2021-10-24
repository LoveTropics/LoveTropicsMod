package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.common.core.game.behavior.config.BehaviorConfig;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;

public class BehaviorConfigList extends LayoutGui {

	private final ClientConfigList configList;

	private final List<ConfigDataUI> children = new ArrayList<>();

	public BehaviorConfigList(GameConfig parent, LayoutTree layout, ClientConfigList configs) {
		super();
		this.configList = configs;

		updateEntries(parent, layout);
	}

	public void updateEntries(GameConfig parent, LayoutTree ltree) {
		Map<BehaviorConfig<?>, ConfigData> configs = configList.configs;

		for (Map.Entry<BehaviorConfig<?>, ConfigData> e : configs.entrySet()) {
			ConfigDataUI listEntry = new ConfigDataUI(parent, ltree.child(0, 3), e.getKey().getName(), e.getValue());
			children.add(listEntry);
		}
		this.mainLayout = ltree.pop();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		for (ConfigDataUI child : children) {
			child.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}

	public int getHeight() {
		return children.stream().mapToInt(ConfigDataUI::getHeight).sum();
	}
	
	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}
	
}
