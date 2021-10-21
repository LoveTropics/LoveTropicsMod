package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;

import com.lovetropics.minigames.client.screen.DynamicLayoutGui;
import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.client.screen.flex.Flex.Unit;
import com.lovetropics.minigames.client.screen.flex.FlexSolver.Results;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;

public class ConfigDataUI extends DynamicLayoutGui implements IConfigWidget {
	
	private final Screen screen;
	private final String name;
	private final ConfigData configs;
	private final Flex textBasis;
	private Layout textLayout;
	
	private final List<IConfigWidget> children = new ArrayList<>();

	public ConfigDataUI(Screen screen, Flex basis, String name, ConfigData configs) {
		super(basis);
		this.screen = screen;
		this.name = name;
		this.configs = configs;
		
		basis.row();
		this.textBasis = basis.child().width(0.35F, Unit.PERCENT);
		children.add(GameConfig.createWidget(screen, basis.child().width(0.65F, Unit.PERCENT), configs));
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
		this.screen.getMinecraft().fontRenderer.drawString(matrixStack, name, textLayout.content().left(), textLayout.content().centerY(), -1);
		for (IConfigWidget child : children) {
			child.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void bake(Results solve) {
		super.bake(solve);
		this.textLayout = solve.layout(textBasis);
	}
}
