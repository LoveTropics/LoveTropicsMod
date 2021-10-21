package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.DynamicLayoutGui;
import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.client.screen.flex.Flex.Unit;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;

public class BehaviorConfigList extends DynamicLayoutGui {

	private final ClientConfigList configList;

	private final List<ConfigDataUI> children = new ArrayList<>();

	public BehaviorConfigList(Screen screen, Flex basis, ClientConfigList configs) {
		super(basis);
		this.configList = configs;

		updateEntries(screen, basis);
	}

	public void updateEntries(Screen screen, Flex basis) {
		Map<String, ConfigData> configs = configList.configs;

		basis = basis.column();
		for (Map.Entry<String, ConfigData> e : configs.entrySet()) {
			Flex childBasis = basis.child().column().padding(3).width(1, Unit.PERCENT);
			ConfigDataUI listEntry = new ConfigDataUI(screen, childBasis, e.getKey(), e.getValue());
			children.add(listEntry);
		}
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
