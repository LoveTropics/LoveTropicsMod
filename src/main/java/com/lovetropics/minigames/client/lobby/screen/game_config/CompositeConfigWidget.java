package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.CompositeConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.IRenderable;

public class CompositeConfigWidget<T extends INestedGuiEventHandler & IRenderable> extends LayoutGui {

	private final List<T> children = new ArrayList<>();

	public CompositeConfigWidget(Layout mainLayout) {
		super(mainLayout);
	}

	public static <T extends INestedGuiEventHandler & IRenderable> CompositeConfigWidget<T> from(Layout layout, CompositeConfigData data) {
		CompositeConfigWidget<T> ret = new CompositeConfigWidget<>(layout);
		for (Map.Entry<String, ConfigData> e : data.value().entrySet()) {
			// TODO narrow layout
			ret.children.add(GameConfig.<T>createWidget(layout, e.getValue()));
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
