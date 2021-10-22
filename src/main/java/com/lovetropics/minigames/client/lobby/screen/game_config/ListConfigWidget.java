package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;

import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigType;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;

public class ListConfigWidget extends LayoutGui implements IConfigWidget {
	
	public ListConfigWidget(LayoutTree ltree) {
		super();
	}

	private final List<IConfigWidget> children = new ArrayList<>();
	
	public static ListConfigWidget from(Screen screen, LayoutTree ltree, ListConfigData data) {
		ListConfigWidget ret = new ListConfigWidget(ltree);
		if (data.type() == ConfigType.COMPOSITE) {
			for (Object val : data.value()) {
				ltree.child(new Box(5, 0, 0, 0), new Box(3, 3, 3, 3));
				ret.children.add(GameConfig.createWidget(screen, ltree, (ConfigData) val));
			}
		} else {
			// TODO others?
		}
		ret.mainLayout = ltree.pop();
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
