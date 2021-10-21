package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lovetropics.minigames.client.screen.DynamicLayoutGui;
import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.client.screen.flex.Flex.Unit;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.CompositeConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;

public class CompositeConfigWidget extends DynamicLayoutGui implements IConfigWidget {

	private final List<IConfigWidget> children = new ArrayList<>();

	public CompositeConfigWidget(Flex basis) {
		super(basis);
	}

	public static CompositeConfigWidget from(Screen screen, Flex basis, CompositeConfigData data) {
		CompositeConfigWidget ret = new CompositeConfigWidget(basis);
		for (Map.Entry<String, ConfigData> e : data.value().entrySet()) {
			Flex childBasis = basis.child().column().marginLeft(5).padding(3).width(1, Unit.PERCENT);
			ret.children.add(new ConfigDataUI(screen, childBasis, e.getKey(), e.getValue()));
		}
		return ret;
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}

	@Override
	public int getHeight() {
		return children.stream().mapToInt(IConfigWidget::getHeight).sum();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		for (IConfigWidget child : children) {
			child.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}
}
