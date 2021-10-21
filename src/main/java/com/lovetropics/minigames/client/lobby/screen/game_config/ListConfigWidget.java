package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;

import com.lovetropics.minigames.client.screen.DynamicLayoutGui;
import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.client.screen.flex.Flex.Unit;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigType;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;

public class ListConfigWidget extends DynamicLayoutGui implements IConfigWidget {
	
	public ListConfigWidget(Flex basis) {
		super(basis);
	}

	private final List<IConfigWidget> children = new ArrayList<>();
	
	public static ListConfigWidget from(Screen screen, Flex basis, ListConfigData data) {
		ListConfigWidget ret = new ListConfigWidget(basis);
		if (data.type() == ConfigType.COMPOSITE) {
			for (Object val : data.value()) {
				Flex childBasis = basis.child().column().marginLeft(5).padding(3).width(1, Unit.PERCENT);
				ret.children.add(GameConfig.createWidget(screen, childBasis, (ConfigData) val));
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
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		for (IConfigWidget child : children) {
			child.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public int getHeight() {
		return children.stream().mapToInt(IConfigWidget::getHeight).sum();
	}
}
