package com.lovetropics.minigames.client.lobby.screen.game_config;

import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigType;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.IRenderable;

import java.util.ArrayList;
import java.util.List;

public class ListConfigWidget<T extends INestedGuiEventHandler & IRenderable> extends LayoutGui {
	
	public ListConfigWidget(Layout mainLayout) {
		super(mainLayout);
	}

	private final List<T> children = new ArrayList<>();
	
	public static <T extends INestedGuiEventHandler & IRenderable> ListConfigWidget<T> from(Layout layout, ListConfigData data) {
		ListConfigWidget<T> ret = new ListConfigWidget<>(layout);
		if (data.type() == ConfigType.COMPOSITE) {
			for (Object val : data.value()) {
				// TODO narrow layout
				ret.children.add(GameConfig.createWidget(layout, (ConfigData) val));
			}
		} else {
			// TODO others?
		}
		return ret;
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		for (T child : children) {
			child.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}
}
