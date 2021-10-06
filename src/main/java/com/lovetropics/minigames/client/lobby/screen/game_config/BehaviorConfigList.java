package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.gui.ScrollPanel;

public class BehaviorConfigList extends LayoutGui {

	private final ClientConfigList configList;

	private final List<ConfigDataUI<?>> children = new ArrayList<>();

	public BehaviorConfigList(Screen screen, Layout layout, ClientConfigList configs) {
		super(layout);
		this.configList = configs;

		updateEntries(screen, layout);
	}

	public void updateEntries(Screen screen, Layout layout) {
		Map<String, ConfigData> configs = configList.configs;

		for (Map.Entry<String, ConfigData> e : configs.entrySet()) {
			// TODO narrow layout
			ConfigDataUI<?> listEntry = new ConfigDataUI<>(screen, layout, e.getKey(), e.getValue());
			children.add(listEntry);
		}
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		for (ConfigDataUI<?> child : children) {
			child.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}
	
	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}
	
}
